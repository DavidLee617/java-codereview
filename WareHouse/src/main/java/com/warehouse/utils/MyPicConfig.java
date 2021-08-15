package com.warehouse.utils;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyPicConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**").addResourceLocations("file:F:/WareHouse/src/main/resources/static/images/");
        registry.addResourceHandler("/pages/member/img/**").addResourceLocations("file:F:/WareHouse/src/main/resources/static/pages/member/img/");
    }
}
