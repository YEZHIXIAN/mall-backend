package com.zhixian.mall.product.vo;

import com.zhixian.mall.product.entity.SkuImagesEntity;
import com.zhixian.mall.product.entity.SkuInfoEntity;
import com.zhixian.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class SkuItemVo {

    // sku基本信息
    SkuInfoEntity info;
    // sku图片信息
    List<SkuImagesEntity> images;
    // spu介绍
    SpuInfoDescEntity desc;
    // spu规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;
    // sku销售属性信息
    List<SkuItemSaleAttrVo> saleAttr;
    // 秒杀商品的优惠信息
    SeckillSkuVo seckillSkuVo;
    // 是否有库存
    private boolean hasStock = true;

    // sku销售属性信息
    @Data
    @ToString
    public static class SkuItemSaleAttrVo {
        private Long attrId;
        private String attrName;
        private List<AttrValueWithSkuIdVo> attrValues;
    }

    @Data
    public static class AttrValueWithSkuIdVo {

        private String attrValue;
        private String skuIds;

    }

    // spu基本属性信息
    @Data
    @ToString
    public static class SpuItemAttrGroupVo {
        private String groupName;
        private List<SpuBaseAttrVo> attrs;
    }

    @Data
    @ToString
    public static class SpuBaseAttrVo {
        private String attrName;
        private String attrValue;
    }

}
