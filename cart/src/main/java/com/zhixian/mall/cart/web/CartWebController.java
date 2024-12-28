package com.zhixian.mall.cart.web;

import com.zhixian.mall.cart.service.CartService;
import com.zhixian.mall.cart.vo.Cart;
import com.zhixian.mall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
public class CartWebController {

    @Autowired
    private CartService cartService;


    /**
     *  删除购物车商品
     * @param skuId 商品id
     * @return 购物车列表页
     */
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.mall.com/cart.html";
    }


    /**
     * 更改购物车商品数量
     * @param skuId 商品id
     * @param num 商品数量
     * @return 购物车列表页
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num) {
        cartService.changeItemCount(skuId, num);
        return "redirect:http://cart.mall.com/cart.html";
    }

    /**
     * 购物车列表页
     * @param check 是否选中
     * @param skuId 商品id
     * @return 购物车列表页
     */
    @GetMapping("/checkItem.html")
    public String checkCart(@RequestParam("check") Integer check,
                            @RequestParam("skuId") Long skuId) {
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.mall.com/cart.html";
    }

    /**
     * 购物车列表页
     * @return 购物车列表页
     */
    @GetMapping("/cart.html")
    public String cartListPage(HttpSession session, Model model) {

        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);

        return "cartList";
    }

    /**
     * 添加购物车
     * @return 添加购物车
     */
    @GetMapping("/addCartItem")
    public String addCartItem(@RequestParam("skuId") Long skuId,
                              @RequestParam("num") Integer num,
                              RedirectAttributes attributes) {

        cartService.addToCart(skuId, num);
        attributes.addAttribute("skuId", skuId);

        return "redirect:http://cart.mall.com/addCartItemSuccess";
    }

    /**
     * 添加购物车成功页
     * @return 添加购物车成功页
     */
    @GetMapping("/addCartItemSuccess")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId,
                                       Model model) {
        // 跳转到成功页
        CartItem item = cartService.getCartItem(skuId);
        model.addAttribute("item", item);
        return "success";
    }
}
