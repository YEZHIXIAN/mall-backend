package com.zhixian.mall.common.to.mq;

public class StockDetailTo {

    /**
     * id
     */
    private Long id;

    /**
     * sku_id
     */
    private Long skuId;

    /**
     * sku_name
     */
    private String skuName;

    /**
     * 购买个数
     */
    private Integer skuNum;

    /**
     * 工作单id
     */
    private Long taskId;

    /**
     * 仓库id
     */
    private Long wareId;

    /**
     * 1锁定 2解锁 3已扣减
     */
    private Integer lockStatus;

}
