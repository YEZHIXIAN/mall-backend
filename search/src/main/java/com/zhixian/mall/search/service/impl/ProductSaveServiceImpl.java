package com.zhixian.mall.search.service.impl;

import com.zhixian.mall.search.model.EsSkuModel;
import com.zhixian.mall.search.service.ProductSaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public boolean productStatusUp(List<EsSkuModel> skuModelList) {
        Iterable<EsSkuModel> save = elasticsearchRestTemplate.save(skuModelList);

        List<EsSkuModel> resList = new ArrayList<>();
        save.forEach(resList::add);
        return resList.size() == skuModelList.size();
    }
}
