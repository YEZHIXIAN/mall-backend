package com.zhixian.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    // 是否有库存
    Map<Long, Boolean> stocks;

    // 重量
    BigDecimal weight;

    public Integer getCount() {
        Integer count = 0;
        if (items != null && !items.isEmpty()) {
            for (OrderItemVo item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

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
