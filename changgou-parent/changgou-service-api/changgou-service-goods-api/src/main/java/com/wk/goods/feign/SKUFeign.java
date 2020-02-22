package com.wk.goods.feign;

import com.wk.goods.pojo.Sku;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name="goods")      //调用goods微服务中的方法
@RequestMapping("/sku")         //对应@RequestMapping("/sku")
public interface SKUFeign {

    /**
     * 商品库存数量递减
     * @param decrMap key：需要递减数量的商品ID；value：递减数量
     * @return
     */
    @GetMapping("/decr/count")
    Result decrCount(@RequestParam Map<String,Integer> decrMap);

    /***
     * 查询Sku全部数据，如果数据量大建议分批查询
     * 调用goods微服务中的SkuController.findAll方法
     * @return
     */
    @GetMapping
    Result<List<Sku>> findAll();

    /**
     * 根据条件搜索
     * @param sku
     * @return
     */
    @PostMapping(value = "/search" )
    Result<List<Sku>> findList(@RequestBody(required = false) Sku sku);

    /***
     * 根据ID查询Sku数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Sku> findById(@PathVariable Long id);
}
