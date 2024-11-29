package com.zhixian.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.Query;
import com.zhixian.mall.common.vo.MemberResponseVo;
import com.zhixian.mall.order.dao.OrderDao;
import com.zhixian.mall.order.entity.OrderEntity;
import com.zhixian.mall.order.feign.CartFeignService;
import com.zhixian.mall.order.feign.UserFeignService;
import com.zhixian.mall.order.interceptor.LoginUserInterceptor;
import com.zhixian.mall.order.service.OrderService;
import com.zhixian.mall.order.vo.MemberAddressVo;
import com.zhixian.mall.order.vo.OrderConfirmVo;
import com.zhixian.mall.order.vo.OrderItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    UserFeignService userFeignService;

    @Autowired
    CartFeignService cartFeignService;

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
        });

        // 3. 获取当前用户的积分
        CompletableFuture<Void> getIntegrationTask = CompletableFuture.runAsync(() -> {
            Integer integration = memberResponseVo.getIntegration();
            confirmVo.setIntegration(integration);
        });

        // 4. 等待所有任务完成
        CompletableFuture.allOf(getAddressTask, getOrderItemsTask, getIntegrationTask).join();


        return confirmVo;
    }

}