package com.zhixian.mall.product.feign;

import com.zhixian.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("inventory")
public interface InventoryFeignService {

    /**
     * 上架商品
     */
    @PostMapping("inventory/waresku/hasStock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
