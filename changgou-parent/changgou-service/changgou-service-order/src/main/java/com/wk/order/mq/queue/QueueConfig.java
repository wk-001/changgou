package com.wk.order.mq.queue;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 延时队列
 * 用户下订单后向队列1发送一个消息，30分钟后队列1的消息发送到队列2，监听队列2，间接实现延时队列
 */
@Configuration
public class QueueConfig {

    /**
     * 创建Queue1 延时队列，会过期，过期后的数据发送给Queue2
     * @return
     */
    @Bean
    public Queue orderDelayQueue(){
        return QueueBuilder
                .durable("orderDelayQueue")
                /*死信队列：消息队列中的数据超出一定时间没有被读取到，被放弃读取的数据
                Queue1的消息超时后进入死信队列，绑定到这个死信队列交换机，
                死信队列中的数据绑定到orderListenerExchange交换机*/
                .withArgument("x-dead-letter-exchange","orderListenerExchange")
                //死信队列中的数据路由到指定的routing-key 也就是自己
                .withArgument("x-dead-letter-routing-key","orderListenerQueue")
                .build();

    }

    /**
     * 创建Queue2
     * @return
     */
    @Bean
    public Queue orderListenerQueue(){
        //参数：队列名字、是否持久化
        return new Queue("orderListenerQueue",true);
    }

    /**
     * 创建交换机
     * @return
     */
    @Bean
    public Exchange orderListenerExchange(){
        return new DirectExchange("orderListenerExchange");
    }

    /**
     * Queue2绑定Exchange
     */
    @Bean
    public Binding orderListenerBinding(Queue orderListenerQueue,Exchange orderListenerExchange){
        return BindingBuilder
                .bind(orderListenerQueue)
                .to(orderListenerExchange)
                .with("orderListenerQueue")
                .noargs();
    }
}
