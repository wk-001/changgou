package com.wk.search.controller;

import com.wk.search.service.SKUService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("search")
@CrossOrigin
public class SKUController {

    @Autowired
    private SKUService skuService;

    /**
     * 调用搜索实现，参数允许为空
     */
    @GetMapping
    public Map search(@RequestParam(required = false) Map<String,String> searchMap){
        return skuService.search(searchMap);
    }

    /**
     * 数据导入到ES
     */
    @GetMapping("import")
    public Result importData(){
        skuService.importData();
        return new Result(true, StatusCode.OK,"数据导入ES成功");
    }
}
