package com.zhixian.mall.search.controller;

import com.zhixian.mall.common.utils.R;
import com.zhixian.mall.search.model.EsSkuModel;
import com.zhixian.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/search")
@RestController
@Slf4j
public class ElasticSaveController {

    @Autowired
    ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<EsSkuModel> skuModelList) {
        boolean b = productSaveService.productStatusUp(skuModelList);
        if (b) {
            log.info("商品上架成功");
            return R.ok();
        } else {
            log.error("商品上架失败");
            return R.error();
        }
    }
}
