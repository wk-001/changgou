package com.wk.order.mq.listener;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 监听Queue2中的过期消息，Queue1中的消息超过指定时间会自动发送到Queue2死信队列
 */
@Component
@RabbitListener(queues = "orderListenerQueue")      //指定监听队列
public class DelayMessageListener {

    /**
     * 延时队列监听
     * @param message
     */
    @RabbitHandler
    public void getDelayMessage(String message){
        String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.println("监听到消息的时间 = " + format);
        System.out.println("监听到的消息 = " + message);
    }

}
