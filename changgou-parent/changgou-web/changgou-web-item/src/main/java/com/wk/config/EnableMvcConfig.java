package com.wk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ControllerAdvice
@Configuration
public class EnableMvcConfig implements WebMvcConfigurer {

    /***
     * 静态资源放行，相当于xml文件中配置的<mvc:resources mapping="" location=""/>
     * mapping=""：请求路径的映射
     * location=""：本地查找路径
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/items/**")        //当前请求路径的映射规则
                .addResourceLocations("classpath:/templates/items/");   //设定要放行的路径
    }

}
