package com.zhixian.mall.cart.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CartController {

    @RequestMapping("/cart.html")
    public String getCartList(Model model) {
        return "cartList";
    }

    @RequestMapping("/success.html")
    public String success() {
        return "success";
    }

    /**
     * 添加商品到购物车
     * RedirectAttributes.addFlashAttribute():将数据放在session中，可以在页面中取出，但是只能取一次
     * RedirectAttributes.addAttribute():将数据放在url后面
     * @return
     */
    @RequestMapping("/addCartItem")
    public String addCartItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes attributes) {
        return "redirect:http://cart.gulimall.com/addCartItemSuccess";
    }

    @RequestMapping("/addCartItemSuccess")
    public String addCartItemSuccess(@RequestParam("skuId") Long skuId,Model model) {
        return "success";
    }


    @RequestMapping("/checkCart")
    public String checkCart(@RequestParam("isChecked") Integer isChecked,@RequestParam("skuId")Long skuId) {
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @RequestMapping("/countItem")
    public String changeItemCount(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @RequestMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        return "redirect:http://cart.gulimall.com/cart.html";
    }


}
