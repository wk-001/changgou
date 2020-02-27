package com.wk.seckill.mq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 处理秒杀订单超时未支付的问题
 * 声明两个队列，下单后发送延时数据到Queue1，Queue1负责数据暂时存储，数据在Queue1的有效期为30分钟
 * 如果超过30分钟后数据没有被消费，会发送到Queue2死信队列，监听Queue2，如果出现死信队列，删除对应订单，回滚库存
 */
@Configuration
public class QueueConfig {

    /**
     * 延时队列 Queue1
     */
    @Bean
    public Queue delaySeckillQueue(){
        return QueueBuilder.durable("delaySeckillQueue")
                .withArgument("x-dead-letter-exchange","seckillExchange")         //当前队列的消息一旦过期，则进入到死信队列交换机
                .withArgument("x-dead-letter-routing-key","seckillQueue")         //将死信队列中的数据路由到指定的队列中(路由到Queue2)
                .build();
    }

    /**
     * 真正监听的消息队列 Queue2
     */
    @Bean
    public Queue seckillQueue(){
        return new Queue("seckillQueue");
    }

    /**
     * 秒杀队列死信交换机
     */
    @Bean
    public Exchange seckillExchange(){
        return new DirectExchange("seckillExchange");
    }

    /**
     * 队列绑定交换机，交换机的数据发送到绑定的队列中
     */
    @Bean
    public Binding bindingSeckillExchange(Queue seckillQueue,Exchange seckillExchange){
        return BindingBuilder.bind(seckillQueue).to(seckillExchange).with("seckillQueue").noargs();
    }
}
