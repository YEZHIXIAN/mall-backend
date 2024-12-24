package com.zhixian.mall.inventory.vo;

import lombok.Data;

@Data
public class LockStockResult {

    private Long skuId;

    private Integer num;

    private Boolean locked;

    private String orderSn;

    private String reason;
}
