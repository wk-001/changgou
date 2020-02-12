package com.wk.goods.service;

import com.github.pagehelper.PageInfo;
import com.wk.goods.pojo.Para;

import java.util.List;

/****
 * @Author:admin
 * @Description:Para业务层接口
 * @Date 2019/6/14 0:16
 *****/
public interface ParaService {

    /**
     * 根据分类ID查询template_id，再用template_id查询参数集合
     * @param categoryId
     * @return
     */
    List<Para> findByCategoryId(Integer categoryId);

    /***
     * Para多条件分页查询
     * @param para
     * @param page
     * @param size
     * @return
     */
    PageInfo<Para> findPage(Para para, int page, int size);

    /***
     * Para分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Para> findPage(int page, int size);

    /***
     * Para多条件搜索方法
     * @param para
     * @return
     */
    List<Para> findList(Para para);

    /***
     * 删除Para
     * @param id
     */
    void delete(Integer id);

    /***
     * 修改Para数据
     * @param para
     */
    void update(Para para);

    /***
     * 新增Para
     * @param para
     */
    void add(Para para);

    /**
     * 根据ID查询Para
     * @param id
     * @return
     */
     Para findById(Integer id);

    /***
     * 查询所有Para
     * @return
     */
    List<Para> findAll();
}
