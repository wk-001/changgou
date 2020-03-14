package com.wk;

import com.alibaba.druid.pool.DruidDataSource;
import entity.FeignInterceptor;
import entity.IdWorker;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import tk.mybatis.spring.annotation.MapperScan;

import javax.sql.DataSource;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages = {"com.wk.goods.feign","com.wk.user.feign"})
@MapperScan(basePackages = {"com.wk.order.dao"})
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class,args);
    }

    /**
     * 将feign调用的拦截器注入到容器中
     * @return
     */
    @Bean
    public FeignInterceptor feignInterceptor(){
        return new FeignInterceptor();
    }

    /**
     * ID生成器，用于生成订单ID，注入到Spring容器中
     */
    @Bean
    public IdWorker idWorker(){
        return new IdWorker();
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
