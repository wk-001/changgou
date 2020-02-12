package com.wk.goods.dao;
import com.wk.goods.pojo.Brand;
import com.wk.goods.pojo.Category;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/****
 * @Author:admin
 * @Description:Brand的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface BrandMapper extends Mapper<Brand> {

    /**
     *
     * 根据分类ID查询品牌集合
     * @param categoryId：分类ID
     */
    @Select("SELECT b.* from tb_brand b,tb_category_brand cb where b.id = cb.brand_id and cb.category_id = #{categoryId}")
    List<Brand> findByCategoryId(Integer categoryId);
}
