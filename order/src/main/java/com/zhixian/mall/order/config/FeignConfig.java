package com.zhixian.mall.order.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Controller
public class FeignConfig {

    @Bean
    public RequestInterceptor feignInterceptor() {
        return template -> {
            System.out.println("feign interceptor");
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) {
                return;
            }
            HttpServletRequest request = requestAttributes.getRequest();
            String cookie = request.getHeader("Cookie");
            template.header("Cookie", cookie);
        };
    }
}
