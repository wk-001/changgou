package com.wk.order.mq.listener;

import com.alibaba.fastjson.JSON;
import com.wk.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Map;

@Component
@RabbitListener(queues = "${mq.pay.queue.order}")        //指定监听队列
public class OrderMessageListener {

    @Autowired
    private OrderService orderService;

    /**
     * 支付结果监听
     */
    @RabbitHandler
    public void getMessage(String message) throws ParseException {
        //支付结果
        Map<String,String> map = JSON.parseObject(message, Map.class);
        System.out.println("监听到的支付消息 = " + map);

        //通信标识  return_code
        if ("SUCCESS".equals(map.get("return_code"))) {

            //订单号   out_trade_no
            String out_trade_no = map.get("out_trade_no");

            //业务结果  result_code
            if ("SUCCESS".equals(map.get("result_code"))) {
                //支付成功 修改订单状态
                /*微信支付订单号	transaction_id
                * 支付完成时间：time_end*/

                orderService.updateStatus(out_trade_no,map.get("time_end"),map.get("transaction_id"));
            }else {
                /*支付失败，关闭支付，取消订单，回滚库存
                * 关闭订单参考：https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=9_3*/
                orderService.deleteOrder(out_trade_no);
            }

        }
    }

}
