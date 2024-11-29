package com.zhixian.mall.order.feign;

import com.zhixian.mall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("cart")
public interface CartFeignService {

    @GetMapping("/currentUserItems")
    List<OrderItemVo> getCurrentUserItems();
}
