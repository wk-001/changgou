package com.wk.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.wk.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 监听秒杀订单的状态，并作出相应处理
 */
@Component
@RabbitListener(queues = "${mq.pay.queue.seckillorder}")          //指定监听队列
public class SeckillMessageListener {

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 消息监听
     * @param message
     */
    @RabbitHandler
    public void getMessage(String message){
        try {
            System.out.println("监听到的消息 = " + message);

            //将支付信息转成map
            Map<String, String> resultMap = JSON.parseObject(message,Map.class);

            String outTradeNo = resultMap.get("out_trade_no");      //订单号

            String attach = resultMap.get("attach");      //自定义数据
            Map<String,String> attachMap = JSON.parseObject(attach, Map.class);
            String username = attachMap.get("username");

            //return_code 通信标识 如果是success则继续
            if("SUCCESS".equals(resultMap.get("return_code"))){
                /*result_code 业务结果 如果是success则修改订单状态为已支付，
                如果是fail，删除订单（真实工作中将订单状态改为未完成，并存储到MySQL），回滚库存*/
                if("SUCCESS".equals(resultMap.get("result_code"))){
                    //修改订单状态为已支付并清理用户排队信息
                    seckillOrderService.updatePayStatus(
                            username
                            ,resultMap.get("transaction_id")
                            ,resultMap.get("time_end"));
                }else {
                    //删除订单（真实工作中将订单状态改为未完成，并存储到MySQL），回滚库存
                    seckillOrderService.deleteOrder(username);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
