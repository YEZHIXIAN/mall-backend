package com.zhixian.mall.order.web;

import com.zhixian.mall.order.service.OrderService;
import com.zhixian.mall.order.vo.OrderConfirmVo;
import com.zhixian.mall.order.vo.OrderSubmitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    /**
     * 去结算
     * @param model 页面数据
     * @return 结算页
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", confirmVo);

        return "confirm";
    }


    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo) {
        SubmitOrderResponseVo submitOrderResponseVo = orderService.submitOrder(orderSubmitVo);
        if (submitOrderResponseVo.getCode() == 0) {
            return "pay";
        } else {
            return "redirect:http://order.mall.com/toTrade";
        }
    }


}
