package com.zhixian.mall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/**").permitAll() // Publicly accessible
                .anyRequest().authenticated() // Secure other endpoints
                .and()
                .anonymous()
                .and()
                .oauth2Login()
                .defaultSuccessUrl("/home", true) // Redirect to /home after successful login
                .and()
                .csrf().disable(); // Optional: Disable CSRF if working with APIs
    }

}
