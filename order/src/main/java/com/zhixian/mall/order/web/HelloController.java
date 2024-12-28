package com.zhixian.mall.order.web;

import com.zhixian.mall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
public class HelloController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @ResponseBody
    @GetMapping("/createOrder")
    private String createOrder(){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn("123456");
        orderEntity.setModifyTime(new Date());
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create", orderEntity);
        return "ok";
    }

    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page") String page){
        return page;
    }
}
