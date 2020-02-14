package com.wk.canal;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.wk.content.feign.ContentFeign;
import com.wk.content.pojo.Content;
import com.xpand.starter.canal.annotation.*;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * 对MySQL数据库表增删改操作的监听
 */
@CanalEventListener
public class CanalDataEventListener {

    @Autowired
    private ContentFeign contentFeign;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 自定义监听对数据库的增删改操作
     */
    /*@ListenPoint(
            destination = "example"               //指定实例地址
            ,schema = "changgou_content"          //指定要监听哪个数据库
            ,table = {"tb_content","tb_content_category"}    //指定要监听哪个表，不指定就监听所有的表
            ,eventType = {
                    CanalEntry.EventType.DELETE
                    ,CanalEntry.EventType.UPDATE
                    ,CanalEntry.EventType.INSERT
            }      //指定监听类型
    )*/

    @ListenPoint(
            eventType = {CanalEntry.EventType.DELETE, CanalEntry.EventType.UPDATE,CanalEntry.EventType.INSERT}      //指定监听类型
            ,schema = {"changgou_content"}          //指定要监听哪个数据库
            ,table = {"tb_content","tb_content_category"}         //指定要监听哪个表，不指定就监听所有的表
            ,destination = "example"                //指定实例地址
    )
    public void onEventCustomUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        //获取列名为category_id的值
        String categoryId = getCategoryId(eventType, rowData);

        //调用feign 获取该分类下的所有的广告集合
        Result<List<Content>> categoryResult = contentFeign.findByCategory(Long.valueOf(categoryId));
        List<Content> contentList = categoryResult.getData();

        //结果转成JSON，再使用redisTemplate存储到redis中
        redisTemplate.boundValueOps("content_"+categoryId).set(JSON.toJSONString(contentList));
    }

    public String getCategoryId(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        String categoryId = "";
        //如果是删除操作，获取操作前的数据
        if (eventType == CanalEntry.EventType.DELETE) {
            for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
                if("category_id".equalsIgnoreCase(column.getName())){
                    categoryId = column.getValue();
                    return categoryId;
                }
            }
        }else {
            //如果是新增、修改，获取操作后的数据
            for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
                if("category_id".equalsIgnoreCase(column.getName())){
                    categoryId = column.getValue();
                    return categoryId;
                }
            }
        }
        return categoryId;
    }

}
