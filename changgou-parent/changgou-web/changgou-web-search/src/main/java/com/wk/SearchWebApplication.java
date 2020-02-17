package com.wk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages = "com.wk.search.feign")
public class SearchWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearchWebApplication.class,args);
    }
}
