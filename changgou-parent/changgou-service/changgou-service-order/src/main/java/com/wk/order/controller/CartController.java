package com.wk.order.controller;

import com.wk.order.pojo.OrderItem;
import com.wk.order.service.CartService;
import entity.Result;
import entity.StatusCode;
import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 购物车操作
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 在Redis中查询购物车列表
     * @return
     */
    @GetMapping("list")
    public Result<List<OrderItem>> list(){
        //用户的令牌信息->解析令牌信息，获取当前用户的所有信息，获取username
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");
        return new Result(true, StatusCode.OK,"购物车列表查询成功！",cartService.list(username));
    }

    /**
     * 商品加入购物车 放入Redis缓存中
     * @param num：加入购物车的数量
     * @param id：商品ID
     * @return
     */
    @GetMapping("/add")
    public Result add(Integer num, Long id){
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");
        cartService.add(id, num, username);
        return new Result(true, StatusCode.OK,"商品添加购物车成功！");
    }
}
