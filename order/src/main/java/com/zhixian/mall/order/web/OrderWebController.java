package com.zhixian.mall.order.web;

import com.zhixian.mall.order.service.OrderService;
import com.zhixian.mall.order.vo.OrderConfirmVo;
import com.zhixian.mall.order.vo.OrderSubmitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes redirectAttributes) {
        SubmitOrderResponseVo submitOrderResponseVo = orderService.submitOrder(orderSubmitVo);
        if (submitOrderResponseVo.getCode() == 0) {
            model.addAttribute("submitOrderResp", submitOrderResponseVo);
            return "pay";
        } else {
            switch (submitOrderResponseVo.getCode()) {
                case 1:
                    redirectAttributes.addFlashAttribute("msg", "订单过期，请刷新再次提交");
                    break;
                case 2:
                    redirectAttributes.addFlashAttribute("msg", "订单商品价格发生变化，请确认后再次提交");
                    break;
                case 3:
                    redirectAttributes.addFlashAttribute("msg", "库存锁定失败，商品库存不足");
                    break;
            }
            return "redirect:http://order.mall.com/toTrade";
        }
    }


}
