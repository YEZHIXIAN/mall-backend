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
                        // 放行所有请求，除了 Google 登录按钮触发的认证路径
                        .antMatchers("/oauth2/authorization/google").authenticated()
                        .anyRequest().permitAll() // 其他请求放行
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/home", true) // 登录成功后跳转
                        .failureUrl("/login.html?error=true") // 登录失败后跳转
                )
                .csrf().disable(); // 可选：禁用 CSRF 防护（适用于开发环境）
        return http.build();
    }
}
