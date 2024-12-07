package com.zhixian.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitVo {

    private Long addrId;

    private Integer payType;

    private String orderToken;

    private String note;

    private BigDecimal payPrice;
}
