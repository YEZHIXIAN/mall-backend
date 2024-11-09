package com.zhixian.mall.common.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SkuHasStockVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long skuId;

    private Boolean hasStock;
}
