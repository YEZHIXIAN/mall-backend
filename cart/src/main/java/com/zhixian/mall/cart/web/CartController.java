package com.zhixian.mall.cart.web;

import com.zhixian.mall.cart.service.CartService;
import com.zhixian.mall.cart.vo.Cart;
import com.zhixian.mall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 购物车列表页
     * @return 购物车列表页
     */
    @GetMapping("/cart.html")
    public String cartListPage(HttpSession session, Model model) {

        Cart cart = new Cart();
        model.addAttribute("cart", cart);

        return "cartList";
    }

    /**
     * 添加购物车
     * @return 添加购物车
     */
    @GetMapping("/addCartItem")
    public String addCartItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, Model model) {

        CartItem cartItem = cartService.addToCart(skuId, num);
        model.addAttribute("item", cartItem);

        return "success";
    }
}
