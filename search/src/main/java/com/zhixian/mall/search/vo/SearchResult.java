package com.zhixian.mall.search.vo;

import com.zhixian.mall.search.model.EsSkuModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResult {

    // 查询到的所有商品信息
    private List<EsSkuModel> product;

    // 页码
    private Integer pageNum;

    // 当前查询结果涉及到的所有品牌
    private Long total;

    // 总页码
    private Integer totalPages;

    // 当前查询结果涉及到的所有品牌
    private List<BrandVo> brands;

    // 当前查询结果涉及到的所有属性
    private List<AttrVo> attrs;

    // 当前查询结果涉及到的所有分类
    private List<CatalogVo> catalogs;

    // 当前页码的导航页码
    private List<Integer> pageNavs;

    // 面包屑导航数据
    private List<NavVo> navs = new ArrayList<>();

    // 当前查询结果涉及到的所有属性
    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
}
