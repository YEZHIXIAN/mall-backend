package com.zhixian.mall.seckill.to;

import com.zhixian.mall.seckill.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillSkuRedisTo {

    /**
     * id
     */
    private Long id;

    /**
     * 活动id
     */
    private Long promotionId;

    /**
     * 活动场次id
     */
    private Long promotionSessionId;

    /**
     * 商品id
     */
    private Long skuId;

    /**
     * 秒杀随机码
     */
    private String randomCode;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;

    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;

    /**
     * 排序
     */
    private Integer seckillSort;

    /**
     * sku详细信息
     */
    private SkuInfoVo skuInfo;

    /**
     * 活动开始时间
     */
    private Long startTime;

    /**
     * 活动结束时间
     */
    private Long endTime;

}
