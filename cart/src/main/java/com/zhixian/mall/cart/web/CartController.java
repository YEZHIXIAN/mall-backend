package com.zhixian.mall.cart.web;

import com.zhixian.mall.cart.interceptor.CartInterceptor;
import com.zhixian.mall.cart.vo.UserInfoTo;
import com.zhixian.mall.common.constant.AuthServerConstant;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class CartController {

    /**
     * 购物车列表页
     * @return 购物车列表页
     */
    @GetMapping("/cart.html")
    public String cartListPage(HttpSession session) {

        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute == null) {
            return "redirect:http://auth.mall.com/login.html";
        }

        return "cartList";
    }
}
