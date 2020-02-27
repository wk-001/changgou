package com.wk.seckill.task;

import com.alibaba.fastjson.JSON;
import com.wk.seckill.dao.SeckillGoodsMapper;
import com.wk.seckill.pojo.SeckillGoods;
import com.wk.seckill.pojo.SeckillOrder;
import entity.IdWorker;
import entity.SeckillStatus;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class MultiThreadingCreateOrder {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 多线程下单
     * @Async  该方法会异步执行，底层由多线程实现
     */
    @Async
    public void createOrder(){
        try {
            Thread.sleep(10000);

            //从Redis中获取秒杀商品的排队信息，队列右边取出数据
            SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();

            if (seckillStatus == null) {
                return ;
            }

            String time = seckillStatus.getTime();
            Long goodsId = seckillStatus.getGoodsId();
            String userName = seckillStatus.getUsername();

            //先尝试在Redis的SeckillGoodsCountList_队列中获取数据，
            // 如果能获取到，证明有库存，可以下单，获取不到，证明已售罄，删除该商品的所有队列信息*/
            Object count = redisTemplate.boundListOps("SeckillGoodsCountList_" + goodsId).rightPop();
            if (count == null) {
                //清理排队信息
                clearUserQueue(userName);
                return;
            }

            //从Redis中获取秒杀商品信息
            String key = "SeckillGoods_" + time;
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(key).get(goodsId);
            //判断是否有库存
            if (seckillGoods == null || seckillGoods.getStockCount()<=0) {
                throw new RuntimeException("秒杀商品已售罄！");
            }

            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(idWorker.nextId());      //订单ID
            seckillOrder.setSeckillId(goodsId);     //秒杀商品ID
            seckillOrder.setMoney(seckillGoods.getCostPrice());     //支付金额，没人只能秒杀一个
            seckillOrder.setUserId(userName);       //用户名
            seckillOrder.setCreateTime(new Date());     //订单创建时间
            seckillOrder.setStatus("0");        //创建订单默认未支付

            /*存储订单对象
             * 一个用户只允许有一个未支付的秒杀订单
             * 订单存入Redis，用hash类型*/
            redisTemplate.boundHashOps("SeckillOrder").put(userName,seckillOrder);

            seckillGoods.setStockCount(seckillGoods.getStockCount()-1);     //库存递减

            //获取该商品的剩余库存
            Long size = redisTemplate.boundListOps("SeckillGoodsCountList_" + goodsId).size();

            //如果秒杀商品库存递减后总库存为0，则删除Redis中的商品信息，并将Redis中的数据同步到MySQL
            //if (seckillGoods.getStockCount()<=0) {
            if (size<=0) {      //判断商品队列的剩余库存，解决多线程下单库存数据不精准的问题
                seckillGoods.setStockCount(size.intValue());
                //同步到MySQL
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);

                //删除Redis中的数据
                redisTemplate.boundHashOps(key).delete(goodsId);
            }else{
                //数据同步到redis
                redisTemplate.boundHashOps(key).put(goodsId,seckillGoods);
            }

            //更新订单状态
            seckillStatus.setOrderId(seckillOrder.getId());     //订单ID
            seckillStatus.setMoney(Float.valueOf(seckillGoods.getCostPrice()));     //支付金额
            seckillStatus.setStatus(2);         //待付款

            //用户抢单成功后更新订单状态
            redisTemplate.boundHashOps("UserQueueStatus").put(userName,seckillStatus);

            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            System.out.println("秒杀商品下单时间 " + now);

            //发送消息给延时队列,用于取消超时未支付的订单；1：要发送到的延时队列名字；2：要发送的数据
            rabbitTemplate.convertAndSend("delaySeckillQueue", (Object) JSON.toJSONString(seckillStatus), new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    message.getMessageProperties().setExpiration("10000");      //设置发送到队列中消息的有效期，这里为了方便测试设置了20秒
                    return message;
                }
            });

            System.out.println("下单成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清理用户排队抢单信息
     */
    public void clearUserQueue(String userName){
        //记录用户排队次数，用户名对应的值自增1
        redisTemplate.boundHashOps("UserQueueCount").delete(userName);
        //用户排队抢单状态，用于查询
        redisTemplate.boundHashOps("UserQueueStatus").delete(userName);
    }
}
