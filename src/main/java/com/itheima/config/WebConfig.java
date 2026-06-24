package com.itheima.config;

import com.itheima.interceptors.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;
    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 不能拦截所有的请求 比如 登录 注册
        registry.addInterceptor(loginInterceptor).excludePathPatterns("/user/login", "/user/register");
    }


}
