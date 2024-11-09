package com.zhixian.mall.search.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zhixian.mall.common.utils.R;
import com.zhixian.mall.search.constant.EsConstant;
import com.zhixian.mall.search.feign.ProductFeignService;
import com.zhixian.mall.search.model.EsSkuModel;
import com.zhixian.mall.search.service.MallSearchService;
import com.zhixian.mall.search.vo.AttrResponseVo;
import com.zhixian.mall.search.vo.BrandVo;
import com.zhixian.mall.search.vo.SearchParam;
import com.zhixian.mall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private ProductFeignService productFeignService;

    /**
     * 构建检索条件
     */
    private static BoolQueryBuilder getBoolQueryBuilder(SearchParam param) {

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 检索关键字
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        // 检索三级分类
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }

        // 检索品牌
        if (param.getBrandId() != null && !param.getBrandId().isEmpty()) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        // 检索库存
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        // 检索价格区间
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] prices = param.getSkuPrice().split("_");
            if (prices.length == 2) {
                boolQuery.filter(rangeQuery.gte(prices[0]).lte(prices[1]));
            }
            else if (prices.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    boolQuery.filter(rangeQuery.lte(prices[0]));
                }
                if (param.getSkuPrice().endsWith("_")) {
                    boolQuery.filter(rangeQuery.gte(prices[0]));
                }
            }
        }

        // 检索属性
        if (param.getAttrs() != null && !param.getAttrs().isEmpty()) {
            for (String attr : param.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
        return boolQuery;
    }

    private static FieldSortBuilder getFieldSortBuilder(SearchParam param) {
        FieldSortBuilder fieldSort = null;
        if (!StringUtils.isEmpty(param.getSort())) {
            String[] s = param.getSort().split("_");
            String sortField = s[0];
            String sortType = s[1];
            SortOrder sortOrder = sortType.equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            fieldSort = SortBuilders.fieldSort(sortField).order(sortOrder);
        }
        return fieldSort;
    }

    private static String replaceQueryString(SearchParam param, String value, String key) {
        String encode = URLEncoder.encode(value, StandardCharsets.UTF_8);
        encode = encode.replace("+", "%20");
        return param.get_queryString().replace("&" + key + "=" + encode, "");
    }

    @Override
    public SearchResult search(SearchParam param) {

        SearchResult result = new SearchResult();

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        // 构建检索条件
        BoolQueryBuilder boolQuery = getBoolQueryBuilder(param);
        queryBuilder.withQuery(boolQuery);

        // 构建排序条件
        FieldSortBuilder fieldSort = getFieldSortBuilder(param);
        if (fieldSort != null) {
            queryBuilder.withSorts(fieldSort);
        }


        // 构建分页条件
        Pageable pageable = PageRequest.of(param.getPageNum() - 1, EsConstant.PRODUCT_PAGESIZE);
        queryBuilder.withPageable(pageable);

        // 构建高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            queryBuilder.withHighlightBuilder(highlightBuilder);
        }

        // 构建聚合
        // 1. 品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandId");
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));

        // 2. 分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(10));

        // 3. 属性聚合
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(20);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attrAgg.subAggregation(attrIdAgg);

        queryBuilder.withAggregations(brandAgg);
        queryBuilder.withAggregations(catalogAgg);
        queryBuilder.withAggregations(attrAgg);

        // 执行检索
        SearchHits<EsSkuModel> search = template.search(queryBuilder.build(), EsSkuModel.class);

        return buildSearchResult(search, param);
    }

    private SearchResult buildSearchResult(SearchHits<EsSkuModel> search, SearchParam param) {

        SearchResult result = new SearchResult();

        // 分页
        long total = search.getTotalHits();
        result.setTotal(total);
        int totalPages = (int) Math.ceil((double) total / EsConstant.PRODUCT_PAGESIZE);
        result.setTotalPages(totalPages);
        result.setPageNum(param.getPageNum());

        // 产品
        List<SearchHit<EsSkuModel>> searchHits = search.getSearchHits();
        List<EsSkuModel> esSkuModels = searchHits.stream()
                .map(
                        searchHit -> {
                            EsSkuModel source = searchHit.getContent();
                            if (!StringUtils.isEmpty(param.getKeyword())) {
                                String skuTitle = searchHit.getHighlightField("skuTitle").get(0);
                                source.setSkuTitle(skuTitle);
                            }
                            return source;
                        }
                )
                .collect(Collectors.toList());
        result.setProduct(esSkuModels);


        // 获取聚合信息
        Aggregations aggregations = (Aggregations) Objects.requireNonNull(search.getAggregations()).aggregations();

        // 分类
        Aggregation catalogAgg = aggregations.get("catalog_agg");
        if (catalogAgg instanceof Terms) {
            Terms catalogTerms = (Terms) catalogAgg;
            List<SearchResult.CatalogVo> catalogs = catalogTerms.getBuckets().stream()
                    .map(bucket -> {
                        SearchResult.CatalogVo catalog = new SearchResult.CatalogVo();
                        catalog.setCatalogId(Long.parseLong(bucket.getKeyAsString()));
                        catalog.setCatalogName(((Terms) bucket.getAggregations().get("catalog_name_agg")).getBuckets().get(0).getKeyAsString());
                        return catalog;
                    })
                    .collect(Collectors.toList());
            result.setCatalogs(catalogs);
        }

        // 品牌
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        Terms brandTerms = aggregations.get("brand_agg");
        for (Terms.Bucket bucket : brandTerms.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
            brandVo.setBrandName(((Terms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString());
            brandVo.setBrandImg(((Terms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString());
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        // 属性
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        Nested attrAgg = aggregations.get("attr_agg");
        Terms attrIdTerms = attrAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdTerms.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            attrVo.setAttrId(bucket.getKeyAsNumber().longValue());
            attrVo.setAttrName(((Terms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString());
            List<String> attrValues = ((Terms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream()
                    .map(Terms.Bucket::getKeyAsString)
                    .collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);

        // 面包屑导航
        if (param.getAttrs() != null && !param.getAttrs().isEmpty()) {
            List<SearchResult.NavVo> collect = param.getAttrs().stream().map(
                    attr -> {
                        SearchResult.NavVo navVo = new SearchResult.NavVo();
                        String[] s = attr.split("_");
                        navVo.setNavValue(s[1]);
                        R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                        if (r.getCode() == 0) {
                            AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                            });
                            navVo.setNavName(data.getAttrName());
                        } else {
                            navVo.setNavName(s[0]);
                        }
                        String replace = replaceQueryString(param, attr, "attrs");
                        navVo.setLink("http://search.mall.com/search.html?" + replace);
                        return navVo;
                    }
            ).collect(Collectors.toList());


            result.setNavs(collect);
        }

        // 品牌导航
        if (param.getBrandId() != null && !param.getBrandId().isEmpty()) {
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            R r = productFeignService.brandInfo(param.getBrandId());
            if (r.getCode() == 0) {
                List<BrandVo> brand = r.getData("brand", new TypeReference<>() {});
                StringBuilder sb = new StringBuilder();
                String replace = "";
                for (BrandVo brandVo : brand) {
                    sb.append(brandVo.getBrandName()).append(";");
                    replace = replaceQueryString(param, brandVo.getBrandId() + "", "brandId");
                }
                navVo.setNavValue(sb.toString());
                navVo.setLink("http://search.mall.com/search.html?" + replace);

            }
            navs.add(navVo);
        }

        return result;

    }
}
