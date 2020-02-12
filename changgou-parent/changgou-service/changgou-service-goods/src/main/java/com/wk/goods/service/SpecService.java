package com.wk.goods.service;

import com.github.pagehelper.PageInfo;
import com.wk.goods.pojo.Spec;

import java.util.List;

/****
 * @Author:admin
 * @Description:Spec业务层接口
 * @Date 2019/6/14 0:16
 *****/
public interface SpecService {

    /**
     * 根据分类ID查询template_id，再用template_id查询规格集合
     * @param categoryId：分类ID
     * @return
     */
    List<Spec> findByCategoryId(Integer categoryId);

    /***
     * Spec多条件分页查询
     * @param spec
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spec> findPage(Spec spec, int page, int size);

    /***
     * Spec分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spec> findPage(int page, int size);

    /***
     * Spec多条件搜索方法
     * @param spec
     * @return
     */
    List<Spec> findList(Spec spec);

    /***
     * 删除Spec
     * @param id
     */
    void delete(Integer id);

    /***
     * 修改Spec数据
     * @param spec
     */
    void update(Spec spec);

    /***
     * 新增Spec
     * @param spec
     */
    void add(Spec spec);

    /**
     * 根据ID查询Spec
     * @param id
     * @return
     */
     Spec findById(Integer id);

    /***
     * 查询所有Spec
     * @return
     */
    List<Spec> findAll();
}
