package com.zhixian.mall.product.vo;

import com.zhixian.mall.product.entity.SkuImagesEntity;
import com.zhixian.mall.product.entity.SkuInfoEntity;
import com.zhixian.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {

    // sku基本信息
    SkuInfoEntity info;

    // sku图片信息
    List<SkuImagesEntity> images;

    // sku销售属性信息
    List<SkuItemSaleAttrVo> saleAttr;

    // spu介绍
    SpuInfoDescEntity desc;

    // spu规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;

    // sku销售属性信息
    @Data
    public static class SkuItemSaleAttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValues;
    }

    // spu基本属性信息
    @Data
    public static class SpuItemAttrGroupVo {
        private String groupName;
        private List<SpuBaseAttrVo> attrs;
    }

    @Data
    public static class SpuBaseAttrVo {
        private String attrName;
        private String attrValue;
    }

}
