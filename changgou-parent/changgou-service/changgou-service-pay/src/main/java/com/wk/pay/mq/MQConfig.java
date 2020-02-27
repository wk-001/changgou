package com.wk.pay.mq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 消息队列配置
 */
@Configuration
public class MQConfig {

    /**
     * 读取配置文件中信息的对象
     */
    @Autowired
    private Environment env;

    /**
     * 创建队列，从yml文件中读取配置文件的内容设置队列的名字
     */
    @Bean
    public Queue orderQueue(){
        return new Queue(env.getProperty("mq.pay.queue.order"));
    }

    /**
     * 创建交换机
     */
    @Bean
    public Exchange orderExchange(){
        //参数：1、交换机名称；2、是否持久化；3、是否自动删除
        return new DirectExchange(env.getProperty("mq.pay.exchange.order"),true,false);
    }

    /**
     * 队列绑定到交换机
     */
    @Bean
    public Binding orderQueueExchange(Queue orderQueue,Exchange orderExchange){
        return BindingBuilder
                .bind(orderQueue)
                .to(orderExchange)
                .with(env.getProperty("mq.pay.routing.key"))
                .noargs();
    }

    /*-------------------------------秒杀订单队列-----------------------------------------*/

    /**
     * 创建队列，从yml文件中读取配置文件的内容设置队列的名字
     */
    @Bean
    public Queue seckillOrderQueue(){
        return new Queue(env.getProperty("mq.pay.queue.seckillorder"));
    }

    /**
     * 创建交换机
     */
    @Bean
    public Exchange seckillOrderExchange(){
        //参数：1、交换机名称；2、是否持久化；3、是否自动删除
        return new DirectExchange(env.getProperty("mq.pay.exchange.seckillorder"),true,false);
    }

    /**
     * 队列绑定到交换机
     */
    @Bean
    public Binding seckillOrderQueueExchange(Queue seckillOrderQueue,Exchange seckillOrderExchange){
        return BindingBuilder
                .bind(seckillOrderQueue)
                .to(seckillOrderExchange)
                .with(env.getProperty("mq.pay.routing.seckillkey"))
                .noargs();
    }

}
