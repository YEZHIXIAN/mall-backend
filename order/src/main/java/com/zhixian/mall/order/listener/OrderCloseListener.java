package com.zhixian.mall.order.listener;

import com.rabbitmq.client.Channel;
import com.zhixian.mall.order.entity.OrderEntity;
import com.zhixian.mall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "order.release.queue")
public class OrderCloseListener {

    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = "order.release.queue")
    public void handleOrderRelease(OrderEntity orderEntity, Channel channel, Message message) throws IOException {

        try {
            orderService.closeOrder(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
        catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}
