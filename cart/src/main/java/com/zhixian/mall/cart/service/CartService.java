package com.zhixian.mall.cart.service;

import com.zhixian.mall.cart.vo.Cart;
import com.zhixian.mall.cart.vo.CartItem;

import java.util.List;

public interface CartService {
    void addToCart(Long skuId, Integer num);

    CartItem getCartItem(Long skuId);

    Cart getCart();

    void checkItem(Long skuId, Integer check);

    void changeItemCount(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItem> getUserCartItems();
}
