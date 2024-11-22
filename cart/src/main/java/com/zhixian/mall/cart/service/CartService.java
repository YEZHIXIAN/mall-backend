package com.zhixian.mall.cart.service;

import com.zhixian.mall.cart.vo.CartItem;

public interface CartService {
    CartItem addToCart(Long skuId, Integer num);
}
