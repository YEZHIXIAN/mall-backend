package com.zhixian.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zhixian.mall.common.enume.OrderStatusEnum;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.Query;
import com.zhixian.mall.common.utils.R;
import com.zhixian.mall.common.vo.MemberResponseVo;
import com.zhixian.mall.common.vo.SkuHasStockVo;
import com.zhixian.mall.order.constant.OrderConstant;
import com.zhixian.mall.order.dao.OrderDao;
import com.zhixian.mall.order.entity.OrderEntity;
import com.zhixian.mall.order.entity.OrderItemEntity;
import com.zhixian.mall.order.feign.CartFeignService;
import com.zhixian.mall.order.feign.InventoryFeignService;
import com.zhixian.mall.order.feign.ProductFeignService;
import com.zhixian.mall.order.feign.UserFeignService;
import com.zhixian.mall.order.interceptor.LoginUserInterceptor;
import com.zhixian.mall.order.service.OrderService;
import com.zhixian.mall.order.to.OrderCreatTo;
import com.zhixian.mall.order.vo.*;
import com.zhixian.mall.order.web.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    UserFeignService userFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    InventoryFeignService inventoryFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    ThreadLocal<OrderSubmitVo> submitVoThreadLocal = new ThreadLocal<>();

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        Long id = memberResponseVo.getId();

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // 1. 远程查询用户收货地址列表
        CompletableFuture<Void> getAddressTask = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = userFeignService.getAddress(id);
            confirmVo.setAddress(address);
        });

        // 2. 远程查询购物车选中的商品
        CompletableFuture<Void> getOrderItemsTask = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = cartFeignService.getCurrentUserItems();
            confirmVo.setItems(items);
        }).thenRunAsync(() -> {

            // 3. 查询商品库存
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R skusHasStock = inventoryFeignService.getSkusHasStock(skuIds);
            List<SkuHasStockVo> stocks = skusHasStock.getData(new TypeReference<>() {});
            if (stocks != null) {
                Map<Long, Boolean> map = stocks.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
                confirmVo.setStocks(map);
            }
        });

        // 4. 获取当前用户的积分
        CompletableFuture<Void> getIntegrationTask = CompletableFuture.runAsync(() -> {
            Integer integration = memberResponseVo.getIntegration();
            confirmVo.setIntegration(integration);
        });

        // 5. 防重令牌
        String orderToken = UUID.randomUUID().toString().replace("-", "");
        confirmVo.setOrderToken(orderToken);
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN + id, orderToken, 30, TimeUnit.MINUTES);

        // 6. 等待所有任务完成
        CompletableFuture.allOf(getAddressTask, getOrderItemsTask, getIntegrationTask).join();


        return confirmVo;
    }

    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo) {

        submitVoThreadLocal.set(orderSubmitVo);

        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();

        // 验证防重令牌
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();

        String orderToken = orderSubmitVo.getOrderToken();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), List.of(OrderConstant.USER_ORDER_TOKEN + memberResponseVo.getId()), orderToken);
        if (result == 0) {
            // 令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        }
        else {
            // 令牌验证成功
            OrderCreatTo order = createOrder();
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = orderSubmitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // 价格验证成功
                responseVo.setCode(0);
                responseVo.setOrder(order.getOrder());
            }
            else {
                // 价格验证失败
                responseVo.setCode(2);
            }
        }
        return responseVo;
    }

    /**
     * 创建订单
     * @return 订单
     */
    private OrderCreatTo createOrder() {
        OrderCreatTo orderCreatTo = new OrderCreatTo();

        // 1. 生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSn);

        // 2. 获取订单项
        List<OrderItemEntity> orderItems = buildOrderItems(orderSn);

        // 3. 计算价格
        computePrice(orderEntity, orderItems);

        orderCreatTo.setOrder(orderEntity);
        orderCreatTo.setOrderItems(orderItems);

        return orderCreatTo;
    }

    /**
     * 计算价格
     * @param orderEntity 订单
     * @param orderItems 订单项
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItems) {
        BigDecimal total = new BigDecimal(0);
        BigDecimal promotion = new BigDecimal(0);
        BigDecimal integration = new BigDecimal(0);
        BigDecimal coupon = new BigDecimal(0);
        BigDecimal gift = new BigDecimal(0);
        BigDecimal growth = new BigDecimal(0);


        for (OrderItemEntity orderItem : orderItems) {
            total = total.add(orderItem.getRealAmount());
            promotion = promotion.add(orderItem.getPromotionAmount());
            integration = integration.add(orderItem.getIntegrationAmount());
            coupon = coupon.add(orderItem.getCouponAmount());
            gift = gift.add(new BigDecimal(orderItem.getGiftIntegration()));
            growth = growth.add(new BigDecimal(orderItem.getGiftGrowth()));
        }

        orderEntity.setTotalAmount(total);
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setUseIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());
        orderEntity.setDeleteStatus(0);
    }

    /**
     * 构建订单
     * @param orderSn 订单号
     * @return 订单
     */
    private OrderEntity buildOrder(String orderSn) {

        OrderEntity orderEntity = new OrderEntity();

        // 1. 获取收货地址
        OrderSubmitVo orderSubmitVo = submitVoThreadLocal.get();
        R fareRes = inventoryFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareResp = fareRes.getData(new TypeReference<>() {});
        BigDecimal fare = fareResp.getFare();

        // 2. 设置订单信息
        orderEntity.setFreightAmount(fare);
        orderEntity.setReceiverCity(fareResp.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareResp.getAddress().getName());
        orderEntity.setReceiverPhone(fareResp.getAddress().getPhone());
        orderEntity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        orderEntity.setReceiverProvince(fareResp.getAddress().getProvince());
        orderEntity.setReceiverRegion(fareResp.getAddress().getRegion());

        // 3. 设置订单号
        orderEntity.setOrderSn(orderSn);

        // 4. 设置订单状态
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);

        return orderEntity;
    }

    /**
     * 构建订单项
     * @return 订单项
     */
    public List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> items = cartFeignService.getCurrentUserItems();

        if (items != null && !items.isEmpty()) {
            return items.stream()
                    .map(
                            item -> {
                                OrderItemEntity orderItemEntity = buildOrderItem(item);
                                orderItemEntity.setOrderSn(orderSn);
                                return orderItemEntity;
                            }
                    )
                    .collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 构建订单项
     * @param orderItemVo 订单项
     * @return 订单项
     */
    private OrderItemEntity buildOrderItem(OrderItemVo orderItemVo) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();

        // 1. 设置sku信息
        orderItemEntity.setSkuId(orderItemVo.getSkuId());
        orderItemEntity.setSkuName(orderItemVo.getTitle());
        orderItemEntity.setSkuPic(orderItemVo.getImage());
        orderItemEntity.setSkuPrice(orderItemVo.getPrice());
        orderItemEntity.setSkuQuantity(orderItemVo.getCount());
        String skuAttr = StringUtils.collectionToDelimitedString(orderItemVo.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttr);

        // 2. 设置积分信息
        orderItemEntity.setGiftGrowth(orderItemEntity.getSkuPrice().intValue());
        orderItemEntity.setGiftIntegration(orderItemEntity.getSkuPrice().intValue());

        // 3. 设置spu信息
        R spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(orderItemVo.getSkuId());
        SpuInfoVo spuInfo = spuInfoBySkuId.getData(new TypeReference<>() {});
        orderItemEntity.setSpuId(spuInfo.getId());
        orderItemEntity.setSpuBrand(spuInfo.getBrandId().toString());
        orderItemEntity.setSpuName(spuInfo.getSpuName());
        orderItemEntity.setCategoryId(spuInfo.getCatalogId());

        // 4. 订单项价格信息
        BigDecimal realAmount = orderItemEntity.getSkuPrice().multiply(
                new BigDecimal(orderItemEntity.getSkuQuantity())
        );
        realAmount = realAmount.subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(realAmount);

        orderItemEntity.setPromotionAmount(new BigDecimal(0));
        orderItemEntity.setCouponAmount(new BigDecimal(0));
        orderItemEntity.setGiftGrowth(realAmount.intValue());
        orderItemEntity.setGiftIntegration(orderItemEntity.getSkuPrice().intValue());

        return orderItemEntity;
    }


}