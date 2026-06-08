package com.mall.common.config;

import com.mall.common.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired(required = false)
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (jwtInterceptor != null) {
            registry.addInterceptor(jwtInterceptor)
                    .addPathPatterns("/api/user/**")
                    .addPathPatterns("/api/cart/**")
                    .addPathPatterns("/api/order/**")
                    .addPathPatterns("/api/review/**")
                    .addPathPatterns("/api/refund/**")
                    .addPathPatterns("/api/address/**")
                    .addPathPatterns("/api/member/**")
                    .excludePathPatterns("/api/user/login",
                                       "/api/user/login-with-token",
                                       "/api/user/register",
                                       "/api/user/register-with-email",
                                       "/api/user/send-email-code",
                                       "/api/user/send-reset-code",
                                       "/api/user/reset-password",
                                       "/api/user/verify-token",
                                       "/api/user/refresh-token");
        }
    }
}