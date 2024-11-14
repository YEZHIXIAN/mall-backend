package com.zhixian.mall.auth.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("third-party")
public interface thirdPartyFeignService {

    @PostMapping("/send")
    String sendSms(@RequestParam(required = false) String toPhoneNumber,
                   @RequestParam String message);
}
