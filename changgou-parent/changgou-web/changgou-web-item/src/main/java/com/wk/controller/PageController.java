package com.wk.controller;

import com.wk.goods.feign.CategoryFeign;
import com.wk.goods.feign.SKUFeign;
import com.wk.goods.feign.SpuFeign;
import com.wk.goods.pojo.Spu;
import com.wk.service.PageService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("page")
public class PageController {

    @Autowired
    private PageService pageService;

    /**
     * 生成静态页面
     * @param spuId
     * @return
     */
    @RequestMapping("/createHtml/{id}")
    public Result createHtml(@PathVariable(name="id") Long spuId){
        pageService.createStaticHtml(spuId);
        return new Result(true, StatusCode.OK,"ok");
    }
}
