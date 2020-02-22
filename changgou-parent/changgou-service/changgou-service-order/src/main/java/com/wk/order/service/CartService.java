package com.wk.order.service;

import com.wk.order.pojo.OrderItem;

import java.util.List;

public interface CartService {

    /**
     * 商品添加到购物车
     * @param num 商品个数
     * @param id    skuID
     * @param username  用户登录名，一个用户只有一个购物车
     */
    void add(Long id, Integer num, String username);

    /**
     * 通过用户登录名查询购物车集合
     * @param username 用户登录名
     * @return
     */
    List<OrderItem> list(String username);
}
