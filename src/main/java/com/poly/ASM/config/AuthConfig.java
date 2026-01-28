package com.poly.ASM.config;

import com.poly.ASM.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AuthConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/home/**",
                        "/product/**",
                        "/auth/**",
                        "/account/sign-up",
                        "/account/forgot-password",
                        "/images/**",
                        "/css/**",
                        "/js/**",
                        "/error"
                );
    }
}
