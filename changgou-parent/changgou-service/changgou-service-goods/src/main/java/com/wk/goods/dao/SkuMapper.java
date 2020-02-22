package com.wk.goods.dao;
import com.wk.goods.pojo.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:admin
 * @Description:Sku的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface SkuMapper extends Mapper<Sku> {

    @Update("UPDATE tb_sku set num=num-#{num} where id=#{skuId} and num>=#{num}")
    int decrCount(@Param("skuId") String skuId, @Param("num") Integer num);
}
