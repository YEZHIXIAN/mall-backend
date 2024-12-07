package com.zhixian.mall.order.feign;

import com.zhixian.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("inventory")
public interface InventoryFeignService {

    @PostMapping("/inventory/waresku/hasStock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("/fare")
    R getFare(@RequestParam("addrId") Long addrId);
}
