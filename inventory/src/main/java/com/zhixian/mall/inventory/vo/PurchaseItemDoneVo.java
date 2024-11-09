package com.zhixian.mall.inventory.vo;

import lombok.Data;

@Data
public class PurchaseItemDoneVo {

    /**
     * 采购项id
     */
    private Long itemId;

    /**
     * 采购状态
     */
    private Integer status;

    /**
     * 原因
     */
    private String reason;
}
