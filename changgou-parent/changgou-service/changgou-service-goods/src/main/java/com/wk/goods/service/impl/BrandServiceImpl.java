package com.wk.goods.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wk.goods.dao.BrandMapper;
import com.wk.goods.pojo.Brand;
import com.wk.goods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public PageInfo<Brand> findPage(Brand brand, Integer page, Integer size) {
        //分页实现，后面必须查询集合；page：当前页；size：每页显示多少条数据
        PageHelper.startPage(page,size);
        Example example = createExample(brand);
        //将查询后的结果封装到PageInfo对象的list中
        return new PageInfo<>(brandMapper.selectByExample(example));
    }

    @Override
    public PageInfo<Brand> findPage(Integer page, Integer size) {
        //分页实现，后面必须查询集合；page：当前页；size：每页显示多少条数据
        PageHelper.startPage(page,size);
        //将查询后的结果封装到PageInfo对象的list中
        return new PageInfo<>(brandMapper.selectAll());
    }

    @Override
    public List<Brand> findList(Brand brand) {
        Example example = createExample(brand);
        return brandMapper.selectByExample(example);
    }

    /**
     * 构建品牌条件
     * @param brand
     * @return
     */
    private Example createExample(Brand brand) {
        //通用Mapper自定义条件搜索对象Example
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();   //条件构造器

        if (brand != null) {
            //name!=null 根据name模糊搜索
            if(!StringUtil.isEmpty(brand.getName())){
                //参数1：brand的属性名，2：占位符参数，搜索的条件
                criteria.andLike("name","%"+brand.getName()+"%");
            }
            //letter!=null 根据首字母搜索
            if(!StringUtil.isEmpty(brand.getLetter())){
                //参数1：brand的属性名，2：占位符参数，搜索的条件
                criteria.andEqualTo("letter",brand.getLetter());
            }
        }
        return example;
    }

    @Override
    public List<Brand> findAll() {
        return brandMapper.selectAll();
    }

    @Override
    public Brand findById(Integer id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    @Override
    public void addBrand(Brand brand) {
        //带有Selective的方法会忽略传入参数中的null
        brandMapper.insertSelective(brand);
    }

    @Override
    public void updateBrand(Brand brand) {
        brandMapper.updateByPrimaryKeySelective(brand);
    }

    @Override
    public void deleteBrand(Integer id) {
        brandMapper.deleteByPrimaryKey(id);
    }
}
