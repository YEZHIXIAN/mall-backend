package com.zhixian.mall.order.vo;

import lombok.Data;

import java.util.List;

@Data
public class SkuWareHasStock {

    private Long skuId;

    private List<Long> wareIds;
}