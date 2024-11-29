package com.zhixian.mall.cart.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.zhixian.mall.cart.feign.ProductFeignService;
import com.zhixian.mall.cart.interceptor.CartInterceptor;
import com.zhixian.mall.cart.service.CartService;
import com.zhixian.mall.cart.vo.Cart;
import com.zhixian.mall.cart.vo.CartItem;
import com.zhixian.mall.cart.vo.SkuInfoVo;
import com.zhixian.mall.cart.vo.UserInfoTo;
import com.zhixian.mall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    private static final String CART_PREFIX = "mall:cart:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;


    /**
     * 添加商品到购物车
     *
     * @param skuId 商品id
     * @param num   数量
     */
    @Override
    public void addToCart(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String o = (String) cartOps.get(skuId.toString());


        CartItem cartItem;
        if (StringUtils.isEmpty(o)) {
            // 购物车中没有当前商品，添加到购物车
            cartItem = new CartItem();

            // 异步编排
            // 获取sku信息
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {

                R skuInfo = productFeignService.info(skuId);
                SkuInfoVo skuInfoVo = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });

                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setSkuId(skuId);
                cartItem.setImage(skuInfoVo.getSkuDefaultImg());
                cartItem.setTitle(skuInfoVo.getSkuTitle());
                cartItem.setPrice(skuInfoVo.getPrice());
                cartItem.setSkuId(skuInfoVo.getSkuId());
            });

            // 获取sku的销售属性
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> values = productFeignService.getSkuSaleAttrValuesAsStringList(skuId);
                cartItem.setSkuAttr(values);
            });

            Gson gson = new Gson();
            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValues).join();

            // 将数据放入redis
            cartOps.put(skuId.toString(), gson.toJson(cartItem));

        }
        else {
            // 购物车中有当前商品，修改数量
            cartItem = new Gson().fromJson(o, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartOps.put(skuId.toString(), new Gson().toJson(cartItem));
        }
    }

    /**
     * 获取购物车商品
     * @param skuId 商品id
     * @return 购物车商品
     */
    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String o = (String) cartOps.get(skuId.toString());
        return new Gson().fromJson(o, CartItem.class);
    }

    /**
     * 绑定购物车redis操作
     * @return 操作
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        String cartKey;

        if (!StringUtils.isEmpty(userInfoTo.getUserId())) {
            // 如果用户已登录，使用用户id作为购物车key
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            // 如果用户未登录，使用用户临时id作为购物车key
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        return redisTemplate.boundHashOps(cartKey);
    }

    /**
     * 获取购物车
     * @return 购物车
     */
    @Override
    public Cart getCart() {

        Cart cart = new Cart();

        // 获取购物车商品
        cart.setItems(getItems());

        // 如果用户已登录，将临时购物车数据合并到购物车中
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            // 获取临时购物车数据
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            BoundHashOperations<String, Object, Object> tempCartOps = redisTemplate.boundHashOps(cartKey);
            List<Object> tempValues = tempCartOps.values();
            if (tempValues != null && !tempValues.isEmpty()) {
                List<CartItem> tempCartItems = tempValues.stream()
                        .map(o -> new Gson().fromJson((String) o, CartItem.class))
                        .collect(Collectors.toList());

                // 合并购物车
                cart.getItems().addAll(tempCartItems);
            }

            // 清除临时购物车数据
            redisTemplate.delete(cartKey);
        }
        return cart;
    }

    private List<CartItem> getItems() {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        List<Object> values = cartOps.values();
        if (values != null && !values.isEmpty()) {
            return values.stream()
                    .map(o -> new Gson().fromJson((String) o, CartItem.class))
                    .collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 选中/不选中购物车商品
     * @param skuId 商品id
     * @param check 是否选中
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), new Gson().toJson(cartItem));
    }

    /**
     * 修改购物车商品数量
     * @param skuId 商品id
     * @param num 数量
     */
    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), new Gson().toJson(cartItem));
    }

    /**
     * 删除购物车商品
     * @param skuId 商品id
     */
    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    /**
     * 获取用户购物车商品
     * @return 购物车商品
     */
    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            List<CartItem> items = getItems();
            if (items == null) {
                return null;
            }
            return items.stream()
                    .filter(CartItem::getCheck)
                    .peek(item -> {
                        // 设置sku的库存信息
                        item.setPrice(productFeignService.getPrice(item.getSkuId()));
                    })
                    .collect(Collectors.toList());
        }
        return null;
    }
}
