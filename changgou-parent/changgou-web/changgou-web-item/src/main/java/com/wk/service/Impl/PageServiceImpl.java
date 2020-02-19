package com.wk.service.Impl;

import com.alibaba.fastjson.JSON;
import com.wk.goods.feign.CategoryFeign;
import com.wk.goods.feign.SKUFeign;
import com.wk.goods.feign.SpuFeign;
import com.wk.goods.pojo.Category;
import com.wk.goods.pojo.Sku;
import com.wk.goods.pojo.Spu;
import com.wk.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class PageServiceImpl implements PageService {

    //thymeleaf模板引擎
    @Autowired
    private TemplateEngine templateEngine;

    //根据spuId查询spu
    @Autowired
    private SpuFeign spuFeign;

    //根据spuId查询对应的sku集合
    @Autowired
    private SKUFeign skuFeign;

    //查询sku对应的1、2、3、级分类对象
    @Autowired
    private CategoryFeign categoryFeign;

    //生成静态页存储路径
    @Value("${pagepath}")
    private String pagepath;

    /**
     * 查询spu、List<sku>、三个分类信息，构建数据模型
     * @param spuId
     * @return
     */
    private Map<String,Object> buildDataModel(Long spuId){
        //根据spuId查询spu信息
        Spu spu = spuFeign.findById(spuId).getData();

        //根据spu对象的三个categoryId查询1、2、3、级分类对象
        Category category1 = categoryFeign.findById(spu.getCategory1Id()).getData();
        Category category2 = categoryFeign.findById(spu.getCategory2Id()).getData();
        Category category3 = categoryFeign.findById(spu.getCategory3Id()).getData();

        //根据spuId查询sku集合
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuFeign.findList(sku).getData();

        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("spu",spu);
        dataMap.put("category1",category1);
        dataMap.put("category2",category2);
        dataMap.put("category3",category3);
        dataMap.put("skuList",skuList);

        //处理图片，将数据库中的多个图片链接用逗号隔开，在前台遍历
        dataMap.put("images",spu.getImages().split(","));

        //spec_items，将数据库中的规格json串转成map类型
        dataMap.put("specificationList", JSON.parseObject(spu.getSpecItems(),Map.class));
        return dataMap;
    }

    @Override
    public void createStaticHtml(Long spuId) {
        try {
            //创建一个容器对象，用于存储页面所需的变是信息 Context：存储数据显示到页面
            Context context = new Context();

            //查询页面所需数据
            Map<String, Object> dataMap = buildDataModel(spuId);
            context.setVariables(dataMap);

            //获取项目编译后的路径，需要在EnableMvcConfig.addResourceHandlers设置资源放行
            String path = PageServiceImpl.class.getResource("/").getPath()+"templates/items/";

            //判断要生成静态页面的路径是否存在，如不存在则创建
//            File dirFile = new File(pagepath);
            File dirFile = new File(path);
            if(!dirFile.exists()){
                dirFile.mkdirs();
            }

            //创建一个writer对象，并指定生成的静态页文件全路径
//            FileWriter fileWriter = new FileWriter("E:/ideaspace/changgou/changgou-parent/changgou-web/changgou-web-item/src/main/resources/templates/items/"+spuId+".html");
//            FileWriter fileWriter = new FileWriter(pagepath+spuId+".html");
            FileWriter fileWriter = new FileWriter(path+spuId+".html");

            /**
             * 执行生成操作
             * 参数：1、指定模板，根据item.html生成；
             * 2、模板所需的数据模型；
             * 3、输出文件对象（文件生成到那里去）
             */
            templateEngine.process("item",context,fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
