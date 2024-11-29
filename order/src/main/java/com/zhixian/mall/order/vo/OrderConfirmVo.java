package com.zhixian.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderConfirmVo {


    // 收货地址列表
    List<MemberAddressVo> address;

    // 购物项
    List<OrderItemVo> items;

    // 优惠券信息
    Integer integration;

    // 订单总额
    BigDecimal total;

    // 应付总额
    BigDecimal payPrice;

    // 防重令牌
    String orderToken;

    /**
     * 获取订单总额
     * @return 订单总额
     */
    public BigDecimal getTotal() {
        total = new BigDecimal("0");
        if (items != null && !items.isEmpty()) {
            for (OrderItemVo item : items) {
                total = total.add(item.getTotalPrice());
            }
        }
        return total;
    }

    /**
     * 获取应付总额
     * @return 应付总额
     */
    public BigDecimal getPayPrice() {
        if (integration != null) {
            return getTotal().subtract(new BigDecimal(integration));
        }
        return getTotal();
    }
}
