package com.wk.search.controller;

import com.wk.search.feign.SKUFeign;
import com.wk.search.pojo.SKUInfo;
import entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("search")
public class SKUController {

    @Autowired
    private SKUFeign skuFeign;

    /**
     * 前台调用搜索微服务
     * @param searchMap 前端传过来的搜索条件，可以为空
     * @return
     */
    @GetMapping("list")
    public String search(@RequestParam(required = false)Map<String,String> searchMap, Model model){
        //通过feign调用changgou-service-search搜索微服务
        Map<String,Object> resultMap = skuFeign.search(searchMap);
        model.addAttribute("resultMap",resultMap);

        //计算分页；总记录数、当前页(从0开始，所以+1)、每页多少条
        Page<SKUInfo> pageInfo = new Page<>(
                Long.parseLong(resultMap.get("total").toString())
                ,Integer.parseInt(resultMap.get("pageNumber").toString())+1
                ,Integer.parseInt(resultMap.get("pageSize").toString())
        );
        model.addAttribute("pageInfo",pageInfo);

        //存储搜索条件，用于回显
        model.addAttribute("searchMap",searchMap);

        //获取上次请求的地址
        String[] urls = getUrl(searchMap);
        model.addAttribute("url",urls[0]);
        model.addAttribute("sortUrl",urls[1]);

        //url需要两个，一个带排序参数，一个不带排序参数

        return "search";
    }

    /**
     * 拼接组装用户请求的URL地址
     * 获取用户每次请求的地址，搜索条件需要在请求地址的基础上添加其他的搜索条件
     * 第一次：http://localhost:18086/search/list
     * 第二次：http://localhost:18086/search/list?category=笔记本
     * searchMap:搜索条件
     * 搜索条件分为两种，一种有排序：sortUrl，一种没有排序url，
     * 没有排序的情况下，两种URL一样，
     * 如果有排序，sortUrl只保留除排序条件外的所有条件,避免第一次排序影响第二次排序
     * @return
     */
    public String[] getUrl(Map<String,String> searchMap){
        StringBuffer url=new StringBuffer("/search/list");       //初始化地址
        StringBuffer sortUrl=new StringBuffer("/search/list");       //排序地址
        if (searchMap != null && searchMap.size()>0) {
            url.append("?");
            sortUrl.append("?");
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                //分页URL不带任何搜索条件，跳过分页参数
                if(key.equalsIgnoreCase("pageNum")){
                    continue;
                }

                url.append(key+"="+value+"&");
                //如果前台传入排序参数，跳过
                if(key.equalsIgnoreCase("sortField")||key.equalsIgnoreCase("sortRule")){
                    continue;
                }
                sortUrl.append(key+"="+value+"&");
            }
            //去掉最后一个"&"
            url = new StringBuffer(url.substring(0, url.length() - 1));
            sortUrl = new StringBuffer(sortUrl.substring(0, sortUrl.length() - 1));
        }

        return new String[]{url.toString(),sortUrl.toString()};
    }
}
