package com.wk.goods;

import entity.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableEurekaClient             //开启Eureka客户端，注册自己的服务到注册中心
@MapperScan("com.wk.goods.dao")     //开启通用Mapper的包扫描 该注解是tk.mybatis.spring.annotation.MapperScan包下的
public class GoodsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoodsApplication.class,args);
    }

    /**
     * ID生成器，将IdWorker交给Spring容器
     */
    @Bean
    public IdWorker idWorker(){
        return new IdWorker(0,0);
    }
}
