package com.wk.seckill.service;

import com.github.pagehelper.PageInfo;
import com.wk.seckill.pojo.SeckillOrder;
import entity.SeckillStatus;

import java.util.List;

/****
 * @Author:admin
 * @Description:SeckillOrder业务层接口
 * @Date 2019/6/14 0:16
 *****/
public interface SeckillOrderService {

    /**
     * 删除30分钟内未支付的订单，并回滚库存
     * @param username
     */
    void deleteOrder(String username);

    /**
     * 修改秒杀订单支付状态
     * @param username
     * @param transactionId 交易流水号
     * @param endTime   支付交易时间
     */
    void updatePayStatus(String username,String transactionId,String endTime);

    /**
     * 每个用户只能有一个秒杀订单，根据用户名查询秒杀订单状态
     * @param userName
     * @return
     */
    SeckillStatus getStatus(String userName);

    /***
     * SeckillOrder多条件分页查询
     * @param seckillOrder
     * @param page
     * @param size
     * @return
     */
    PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size);

    /***
     * SeckillOrder分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<SeckillOrder> findPage(int page, int size);

    /***
     * SeckillOrder多条件搜索方法
     * @param seckillOrder
     * @return
     */
    List<SeckillOrder> findList(SeckillOrder seckillOrder);

    /***
     * 删除SeckillOrder
     * @param id
     */
    void delete(Long id);

    /***
     * 修改SeckillOrder数据
     * @param seckillOrder
     */
    void update(SeckillOrder seckillOrder);

    /***
     * 新增SeckillOrder
     * @param seckillOrder
     */
    void add(SeckillOrder seckillOrder);

    /**
     * 根据ID查询SeckillOrder
     * @param id
     * @return
     */
     SeckillOrder findById(Long id);

    /***
     * 查询所有SeckillOrder
     * @return
     */
    List<SeckillOrder> findAll();

    /**
     * 添加秒杀订单
     * @param time
     * @param goodsId
     * @param userName
     */
    Boolean addOrder(String time, Long goodsId, String userName);
}
