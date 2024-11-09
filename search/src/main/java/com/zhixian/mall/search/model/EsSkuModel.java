package com.zhixian.mall.search.model;

import com.zhixian.mall.common.to.es.SkuModel;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "product")
public class EsSkuModel extends SkuModel {

    @Id
    private Long skuId;
}
