package com.zhixian.mall.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated() // 所有请求需要登录认证
            )
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/home", true) // 登录成功后跳转
                .failureUrl("/login?error=true") // 登录失败后跳转
            );
        return http.build();
    }
}
