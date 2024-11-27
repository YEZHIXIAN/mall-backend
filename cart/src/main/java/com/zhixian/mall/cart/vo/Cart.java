package com.zhixian.mall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class Cart {

    private List<CartItem> items = new ArrayList<>();

    private Integer countNum;

    private Integer countType;

    private BigDecimal totalAmount;

    private BigDecimal reduce = new BigDecimal("0.00");

    public Integer getCountNum() {
        if (this.items != null && !this.items.isEmpty()) {
            this.countNum = this.items.stream().mapToInt(CartItem::getCount).sum();
        } else {
            this.countNum = 0;
        }
        return countNum;
    }

    public BigDecimal getTotalAmount() {
        if (this.items != null && !this.items.isEmpty()) {
            this.totalAmount = this.items.stream().map(CartItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            this.totalAmount = new BigDecimal("0.00");
        }

        this.totalAmount = this.totalAmount.subtract(this.reduce);

        return this.totalAmount;
    }

}
