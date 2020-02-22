package com.wk.order.service.impl;

import com.wk.goods.feign.SKUFeign;
import com.wk.goods.feign.SpuFeign;
import com.wk.goods.pojo.Sku;
import com.wk.goods.pojo.Spu;
import com.wk.order.pojo.OrderItem;
import com.wk.order.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SKUFeign skuFeign;

    @Autowired
    private SpuFeign spuFeign;

    /**
     * 通过用户登录名查询购物车集合
     * @param username 用户登录名
     * @return
     */
    @Override
    public List<OrderItem> list(String username) {
        //获取指定命名空间的所有的数据
        return redisTemplate.boundHashOps("Cart_"+username).values();
    }

    /**
     * 添加商品到购物车
     * @param num 商品个数
     * @param id    skuID
     * @param username  用户登录名，一个用户只有一个购物车
     */
    @Override
    public void add(Long id, Integer num, String username) {
        //当添加到购物车的商品数量<=0，则移除该商品信息
        if (num <= 0) {
            redisTemplate.boundHashOps("Cart_"+username).delete(id);
            Long size = redisTemplate.boundHashOps("Cart_" + username).size();
            //如果购物车中无商品，则连购物车一起删除
            if (size == null || size<=0) {
                redisTemplate.delete("Cart_" + username);
            }
            return;
        }

        //查询sku详情
        Sku sku = skuFeign.findById(id).getData();
        if (sku != null) {
            //查询spu详情
            Spu spu = spuFeign.findById(sku.getSpuId()).getData();
            //根据spu和sku的信息创建OrderItem对象
            OrderItem orderItem = createOrderItem(num, id, sku, spu);
            //将购物车数据存入到Redis，namespace:username
            redisTemplate.boundHashOps("Cart_"+username).put(id,orderItem);
        }
    }


    /**
     * 根据spu和sku的信息创建OrderItem对象
     * @param num
     * @param id
     * @param sku
     * @param spu
     * @return
     */
    private OrderItem createOrderItem(Integer num, Long id, Sku sku, Spu spu) {
        //将加入购物车的商品信息封装成orderItem对象
        OrderItem orderItem = new OrderItem();
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSpuId(spu.getId());
        orderItem.setSkuId(id);
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(num*orderItem.getPrice());
        orderItem.setImage(spu.getImage());
        return orderItem;
    }
}
