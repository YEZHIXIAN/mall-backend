package com.zhixian.mall.cart.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.zhixian.mall.cart.feign.ProductFeignService;
import com.zhixian.mall.cart.interceptor.CartInterceptor;
import com.zhixian.mall.cart.service.CartService;
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
     * @param skuId 商品id
     * @param num 数量
     * @return 购物车商品
     */
    @Override
    public CartItem addToCart(Long skuId, Integer num) {
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
                cartItem.setCount(1);
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
        return cartItem;
    }

    /**
     * 绑定购物车redis操作
     * @return 操作
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        return redisTemplate.boundHashOps(cartKey);
    }
}
