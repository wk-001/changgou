package com.wk.seckill.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wk.seckill.dao.SeckillGoodsMapper;
import com.wk.seckill.dao.SeckillOrderMapper;
import com.wk.seckill.pojo.SeckillGoods;
import com.wk.seckill.pojo.SeckillOrder;
import com.wk.seckill.service.SeckillOrderService;
import com.wk.seckill.task.MultiThreadingCreateOrder;
import entity.SeckillStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/****
 * @Author:admin
 * @Description:SeckillOrder业务层接口实现类
 * @Date 2019/6/14 0:16
 *****/
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private MultiThreadingCreateOrder createOrder;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 下单失败，删除订单，并回滚库存
     * @param username
     */
    @Override
    public void deleteOrder(String username) {
        //删除Redis中的订单信息
        redisTemplate.boundHashOps("SeckillOrder").delete(username);

        //查询用户排队信息
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);

        //删除排队信息
        clearUserQueue(username);

        //回滚库存,Redis中的商品库存递增，Redis中的对应商品可能为空
        //从Redis中获取秒杀商品信息
        String key = "SeckillGoods_" + seckillStatus.getTime();
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(key).get(seckillStatus.getGoodsId());
        if (seckillGoods == null) {
            //如果Redis中没有对应的秒杀商品信息，说明该商品已售罄，已经从Redis中删除，只能从数据库中查询秒杀商品的信息
            seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillStatus.getGoodsId());
            //更新数据库的秒杀商品库存，如果Redis中没有商品缓存，说明该商品库存为0，库存回滚直接设置库存为1
            seckillGoods.setStockCount(1);
            seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
        }else {
            seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
        }

        //回滚后的秒杀商品同步到Redis
        redisTemplate.boundHashOps(key).put(seckillStatus.getGoodsId(),seckillGoods);

        //商品库存队列+1
        redisTemplate.boundListOps("SeckillGoodsCountList_"+seckillGoods.getId()).leftPush(seckillGoods.getId());
    }

    /**
     * 修改秒杀订单支付状态
     * @param username
     * @param transactionId 交易流水号
     * @param endTime   支付交易时间
     */
    @Override
    public void updatePayStatus(String username, String transactionId, String endTime) {
        //根据用户名从Redis中获取订单信息
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);

        if (seckillOrder != null) {
            try {
                //修改订单状态信息
                seckillOrder.setStatus("1");
                seckillOrder.setTransactionId(transactionId);
                Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(endTime);
                seckillOrder.setPayTime(date);

                //订单信息同步到数据
                seckillOrderMapper.insertSelective(seckillOrder);

                //删除Redis中的订单信息
                redisTemplate.boundHashOps("SeckillOrder").delete(username);

                //删除用户排队信息
                clearUserQueue(username);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取用户排队信息
     * @param userName
     * @return
     */
    @Override
    public SeckillStatus getStatus(String userName) {
        return (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(userName);
    }

    /**
     * 添加秒杀订单
     * @param time
     * @param goodsId
     * @param userName
     * @return
     */
    @Override
    public Boolean addOrder(String time, Long goodsId, String userName) {

        //记录用户排队次数，用户名对应的值自增1
        Long count = redisTemplate.boundHashOps("UserQueueCount").increment(userName, 1);
        //如果值大于1，证明用户重复提交，Redis是单线程，所以多线程情况下不会出现同时==1的情况
        if (count > 1) {
            throw new RuntimeException("100");
        }

        //创建排队对象
        SeckillStatus seckillStatus = new SeckillStatus(userName,new Date(),1,goodsId,time);

        //将秒杀抢单信息存入到Redis中，从队列左边存入
        redisTemplate.boundListOps("SeckillOrderQueue").leftPush(seckillStatus);

        //用户排队抢单状态，用于查询
        redisTemplate.boundHashOps("UserQueueStatus").put(userName,seckillStatus);

        //异步执行
        createOrder.createOrder();
        return true;
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

    /**
     * SeckillOrder条件+分页查询
     * @param seckillOrder 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(seckillOrder);
        //执行搜索
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectByExample(example));
    }

    /**
     * SeckillOrder分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<SeckillOrder> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectAll());
    }

    /**
     * SeckillOrder条件查询
     * @param seckillOrder
     * @return
     */
    @Override
    public List<SeckillOrder> findList(SeckillOrder seckillOrder){
        //构建查询条件
        Example example = createExample(seckillOrder);
        //根据构建的条件查询数据
        return seckillOrderMapper.selectByExample(example);
    }


    /**
     * SeckillOrder构建查询对象
     * @param seckillOrder
     * @return
     */
    public Example createExample(SeckillOrder seckillOrder){
        Example example=new Example(SeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        if(seckillOrder!=null){
            // 主键
            if(!StringUtils.isEmpty(seckillOrder.getId())){
                    criteria.andEqualTo("id",seckillOrder.getId());
            }
            // 秒杀商品ID
            if(!StringUtils.isEmpty(seckillOrder.getSeckillId())){
                    criteria.andEqualTo("seckillId",seckillOrder.getSeckillId());
            }
            // 支付金额
            if(!StringUtils.isEmpty(seckillOrder.getMoney())){
                    criteria.andEqualTo("money",seckillOrder.getMoney());
            }
            // 用户
            if(!StringUtils.isEmpty(seckillOrder.getUserId())){
                    criteria.andEqualTo("userId",seckillOrder.getUserId());
            }
            // 创建时间
            if(!StringUtils.isEmpty(seckillOrder.getCreateTime())){
                    criteria.andEqualTo("createTime",seckillOrder.getCreateTime());
            }
            // 支付时间
            if(!StringUtils.isEmpty(seckillOrder.getPayTime())){
                    criteria.andEqualTo("payTime",seckillOrder.getPayTime());
            }
            // 状态，0未支付，1已支付
            if(!StringUtils.isEmpty(seckillOrder.getStatus())){
                    criteria.andEqualTo("status",seckillOrder.getStatus());
            }
            // 收货人地址
            if(!StringUtils.isEmpty(seckillOrder.getReceiverAddress())){
                    criteria.andEqualTo("receiverAddress",seckillOrder.getReceiverAddress());
            }
            // 收货人电话
            if(!StringUtils.isEmpty(seckillOrder.getReceiverMobile())){
                    criteria.andEqualTo("receiverMobile",seckillOrder.getReceiverMobile());
            }
            // 收货人
            if(!StringUtils.isEmpty(seckillOrder.getReceiver())){
                    criteria.andEqualTo("receiver",seckillOrder.getReceiver());
            }
            // 交易流水
            if(!StringUtils.isEmpty(seckillOrder.getTransactionId())){
                    criteria.andEqualTo("transactionId",seckillOrder.getTransactionId());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Long id){
        seckillOrderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder){
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 增加SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void add(SeckillOrder seckillOrder){
        seckillOrderMapper.insert(seckillOrder);
    }

    /**
     * 根据ID查询SeckillOrder
     * @param id
     * @return
     */
    @Override
    public SeckillOrder findById(Long id){
        return  seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询SeckillOrder全部数据
     * @return
     */
    @Override
    public List<SeckillOrder> findAll() {
        return seckillOrderMapper.selectAll();
    }

}
