package com.wk.goods.feign;

import com.wk.goods.pojo.Category;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name="goods")      //调用goods微服务中的方法
@RequestMapping("/category")    //对应@RequestMapping("/category")
public interface CategoryFeign {

    /***
     * 根据分类ID查询Category分类信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Category> findById(@PathVariable Integer id);
}
