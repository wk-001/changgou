package com.wk.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.wk.seckill.service.SeckillOrderService;
import entity.SeckillStatus;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 监听超时未支付的秒杀订单
 */
@Component
@RabbitListener(queues = "seckillQueue")          //指定监听队列 Queue2
public class DelaySeckillMessageListener {

    @Autowired
    private SeckillOrderService seckillOrderService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 消息监听
     * @param message
     */
    @RabbitHandler
    public void getMessage(String message){
        try {
            System.out.println("监听到的消息 = " + message);

            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            System.out.println("秒杀商品回滚时间 " + now);

            //获取用户排队状态信息，将信息转成map
            SeckillStatus seckillStatus = JSON.parseObject(message, SeckillStatus.class);

            //如果此时Redis中有用户的排队信息，证明该用户没有完成支付，需要关闭微信支付，删除订单，回滚库存
            Object userQueueStatus = redisTemplate.boundHashOps("UserQueueStatus").get(seckillStatus.getUsername());
            if (userQueueStatus != null) {
                //关闭微信支付

                //删除订单
                seckillOrderService.deleteOrder(seckillStatus.getUsername());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
