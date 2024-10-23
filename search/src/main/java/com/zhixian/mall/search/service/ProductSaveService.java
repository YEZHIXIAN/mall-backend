package com.zhixian.mall.search.service;

import com.zhixian.mall.search.model.EsSkuModel;

import java.util.List;

public interface ProductSaveService {
    boolean productStatusUp(List<EsSkuModel> skuModelList);
}
