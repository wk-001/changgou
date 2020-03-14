package com.wk;

import com.alibaba.druid.pool.DruidDataSource;
import entity.IdWorker;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import tk.mybatis.spring.annotation.MapperScan;

import javax.sql.DataSource;

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

    //分布式事务设置
    //普通数据源；spring.datasource会把当前每个微服务中配置的spring.datasource注入进来
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return new DruidDataSource();
    }

    //代理数据源，绑定undo_log的操作
    @Primary //当IOC中存在多个数据源优先使用这个
    @Bean
    public DataSourceProxy dataSourceProxy(DataSource dataSource) {
        return new DataSourceProxy(dataSource);
    }

}
