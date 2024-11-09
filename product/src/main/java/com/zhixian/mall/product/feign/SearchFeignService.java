package com.zhixian.mall.product.feign;

import com.zhixian.mall.common.to.es.SkuModel;
import com.zhixian.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("search")
public interface SearchFeignService {

    /**
     * 上架商品
     */
    @PostMapping("/search//product")
    R productStatusUp(@RequestBody List<SkuModel> skuModelList);
}
