package com.wk.goods.service;

import com.github.pagehelper.PageInfo;
import com.wk.goods.pojo.Brand;

import java.util.List;

public interface BrandService {

    /**
     * 带条件的分页查询
     * @param brand：搜索条件
     * @param page：当前页
     * @param size：每页多少条数据
     */
    PageInfo<Brand> findPage(Brand brand,Integer page,Integer size);

    /**
     * 分页查询
     * @param page：当前页
     * @param size：每页多少条数据
     */
    PageInfo<Brand> findPage(Integer page,Integer size);

    /**
     * 根据品牌信息多条件搜索
     */
    List<Brand> findList(Brand brand);

    /**
     * 查询所有品牌
     */
    List<Brand> findAll();

    /**
     * 根据主键查询
     */
    Brand findById(Integer id);

    /**
     * 增加品牌
     */
    void addBrand(Brand brand);

    /**
     * 修改品牌
     */
    void updateBrand(Brand brand);

    /**
     * 删除品牌
     */
    void deleteBrand(Integer id);
}
