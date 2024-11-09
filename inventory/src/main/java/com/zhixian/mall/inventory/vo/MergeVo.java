package com.zhixian.mall.inventory.vo;

import lombok.Data;

import java.util.List;

@Data
public class MergeVo {

    /**
     * 采购id
     */
    private Long purchaseId;

    /**
     * 物品清单
     */
    private List<Long> items;
}
