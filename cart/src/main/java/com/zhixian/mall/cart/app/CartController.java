package com.zhixian.mall.cart.app;

import com.zhixian.mall.cart.service.CartService;
import com.zhixian.mall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     *  获取当前用户购物车商品
     * @return 购物车商品列表
     */
    @GetMapping("/currentUserItems")
    public List<CartItem> getCurrentUserItems() {
        return cartService.getUserCartItems();
    }
}
