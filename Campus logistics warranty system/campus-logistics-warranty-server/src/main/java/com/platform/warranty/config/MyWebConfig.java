package com.platform.warranty.config;

import com.platform.warranty.interceptor.TokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.platform.warranty.controller.FileUploadController.UPLOAD_FILE;


@Configuration
@RequiredArgsConstructor
public class MyWebConfig implements WebMvcConfigurer {

    private final TokenInterceptor tokenInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
         registry.addResourceHandler("/upload/**")
            .addResourceLocations("file:"+UPLOAD_FILE); // 注意这里的 "file:" 前缀
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/register",
                        "/**/login",
                        "/notice/list",
                        "/resource/list",
                        "/**/getById/**",
                        "/**/all"
                );
    }
}
