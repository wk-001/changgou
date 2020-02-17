package com.wk.search.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "search")       //服务的名字
@RequestMapping("search")           //服务的controller
public interface SKUFeign {

    /**
     * 调用搜索实现，参数允许为空
     */
    @GetMapping
    Map search(@RequestParam(required = false) Map<String,String> searchMap);

}
