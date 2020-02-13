package com.wk.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wk.goods.dao.BrandMapper;
import com.wk.goods.dao.CategoryMapper;
import com.wk.goods.dao.SkuMapper;
import com.wk.goods.dao.SpuMapper;
import com.wk.goods.pojo.*;
import com.wk.goods.service.SpuService;
import entity.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/****
 * @Author:admin
 * @Description:Spu业务层接口实现类
 * @Date 2019/6/14 0:16
 *****/
@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private IdWorker idWorker;      //ID生成器

    @Override
    public void batchPut(Long[] ids) {
        //update tb_sku set isMarketable=1 where id in(ids)and isdelete=0 and status=1
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        // id in(ids)
        criteria.andIn("id", Arrays.asList(ids));

        //isDelete=0
        criteria.andEqualTo("isDelete",0);

        //status=1
        criteria.andEqualTo("status",1);

        //准备修改的数据
        Spu spu = new Spu();
        spu.setIsMarketable("1");
        //spu：要修改的结果，example：修改的条件
        spuMapper.updateByExampleSelective(spu,example);
    }

    @Override
    public void put(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //检查是否删除的商品
        if(spu.getIsDelete().equals("1")){
            throw new RuntimeException("此商品已删除！");
        }
        if(!spu.getStatus().equals("1")){
            throw new RuntimeException("此商品未通过审核！");
        }
        //上架状态
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Override
    public void pull(Long spuId) {
        //查询商品
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //判断商品是否已经删除
        if(spu.getIsDelete().equals("1")){
            throw new RuntimeException("该商品已删除！");
        }
        //下架商品
        spu.setIsMarketable("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Override
    public void audit(Long spuId) {
        //查询商品
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //判断商品是否已经删除
        if(spu.getIsDelete().equals("1")){
            throw new RuntimeException("该商品已删除！");
        }
        //修改审批状态
        spu.setStatus("1");
        //上架商品
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 根据spuID查询goods的数据
     * @param id
     * @return
     */
    @Override
    public Goods findGoodsById(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skus = skuMapper.select(sku);
        return new Goods(spu,skus);
    }

    /**
     * 添加/修改商品信息
     * @param goods
     */
    @Override
    public void addGoods(Goods goods) {
        //spu是一个对象
        Spu spu = goods.getSpu();

        //判断spu的ID是否为空，空新增，非空修改
        if (spu.getId() == null) {
            spu.setId(idWorker.nextId());
            spuMapper.insertSelective(spu);
        }else {
            //先修改spu
            spuMapper.updateByPrimaryKeySelective(spu);
            //删除之前的List<sku>
            Sku sku = new Sku();
            sku.setSpuId(spu.getId());
            skuMapper.delete(sku);
        }




        //根据3级分类ID获取3级分类名称
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());

        //根据brandID获取品牌名称
        Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());

        //sku是一个集合
        List<Sku> skuList = goods.getSkuList();
        Date date = new Date();
        for (Sku sku : skuList) {
            sku.setId(idWorker.nextId());
            StringBuilder skuName = new StringBuilder(spu.getName());

            //防止spec是null
            if(StringUtils.isEmpty(sku.getSpec())){
                sku.setSpec("{}");
            }

            //获取Spec的值，将前台传入的json值转成map，再获取map的value，拼接到一起
            Map<String,String> map = JSON.parseObject(sku.getSpec(), Map.class);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                skuName.append(entry.getValue());
            }
            sku.setName(skuName.toString());                 //spu.name+规格信息，需要获取Spec的值
            sku.setSpuId(spu.getId());
            sku.setCreateTime(date);
            sku.setUpdateTime(date);
            sku.setCategoryId(spu.getCategory3Id());        //分类ID -> 3级分类ID
            sku.setCategoryName(category.getName());      //分类名字 -> 3级分类名字
            sku.setBrandName(brand.getName());         //品牌名字
            skuMapper.insertSelective(sku);
        }
    }

    /**
     * Spu条件+分页查询
     * @param spu 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Spu> findPage(Spu spu, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(spu);
        //执行搜索
        return new PageInfo<Spu>(spuMapper.selectByExample(example));
    }

    /**
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Spu> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<Spu>(spuMapper.selectAll());
    }

    /**
     * Spu条件查询
     * @param spu
     * @return
     */
    @Override
    public List<Spu> findList(Spu spu){
        //构建查询条件
        Example example = createExample(spu);
        //根据构建的条件查询数据
        return spuMapper.selectByExample(example);
    }


    /**
     * Spu构建查询对象
     * @param spu
     * @return
     */
    public Example createExample(Spu spu){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(spu!=null){
            // 主键
            if(!StringUtils.isEmpty(spu.getId())){
                    criteria.andEqualTo("id",spu.getId());
            }
            // 货号
            if(!StringUtils.isEmpty(spu.getSn())){
                    criteria.andEqualTo("sn",spu.getSn());
            }
            // SPU名
            if(!StringUtils.isEmpty(spu.getName())){
                    criteria.andLike("name","%"+spu.getName()+"%");
            }
            // 副标题
            if(!StringUtils.isEmpty(spu.getCaption())){
                    criteria.andEqualTo("caption",spu.getCaption());
            }
            // 品牌ID
            if(!StringUtils.isEmpty(spu.getBrandId())){
                    criteria.andEqualTo("brandId",spu.getBrandId());
            }
            // 一级分类
            if(!StringUtils.isEmpty(spu.getCategory1Id())){
                    criteria.andEqualTo("category1Id",spu.getCategory1Id());
            }
            // 二级分类
            if(!StringUtils.isEmpty(spu.getCategory2Id())){
                    criteria.andEqualTo("category2Id",spu.getCategory2Id());
            }
            // 三级分类
            if(!StringUtils.isEmpty(spu.getCategory3Id())){
                    criteria.andEqualTo("category3Id",spu.getCategory3Id());
            }
            // 模板ID
            if(!StringUtils.isEmpty(spu.getTemplateId())){
                    criteria.andEqualTo("templateId",spu.getTemplateId());
            }
            // 运费模板id
            if(!StringUtils.isEmpty(spu.getFreightId())){
                    criteria.andEqualTo("freightId",spu.getFreightId());
            }
            // 图片
            if(!StringUtils.isEmpty(spu.getImage())){
                    criteria.andEqualTo("image",spu.getImage());
            }
            // 图片列表
            if(!StringUtils.isEmpty(spu.getImages())){
                    criteria.andEqualTo("images",spu.getImages());
            }
            // 售后服务
            if(!StringUtils.isEmpty(spu.getSaleService())){
                    criteria.andEqualTo("saleService",spu.getSaleService());
            }
            // 介绍
            if(!StringUtils.isEmpty(spu.getIntroduction())){
                    criteria.andEqualTo("introduction",spu.getIntroduction());
            }
            // 规格列表
            if(!StringUtils.isEmpty(spu.getSpecItems())){
                    criteria.andEqualTo("specItems",spu.getSpecItems());
            }
            // 参数列表
            if(!StringUtils.isEmpty(spu.getParaItems())){
                    criteria.andEqualTo("paraItems",spu.getParaItems());
            }
            // 销量
            if(!StringUtils.isEmpty(spu.getSaleNum())){
                    criteria.andEqualTo("saleNum",spu.getSaleNum());
            }
            // 评论数
            if(!StringUtils.isEmpty(spu.getCommentNum())){
                    criteria.andEqualTo("commentNum",spu.getCommentNum());
            }
            // 是否上架,0已下架，1已上架
            if(!StringUtils.isEmpty(spu.getIsMarketable())){
                    criteria.andEqualTo("isMarketable",spu.getIsMarketable());
            }
            // 是否启用规格
            if(!StringUtils.isEmpty(spu.getIsEnableSpec())){
                    criteria.andEqualTo("isEnableSpec",spu.getIsEnableSpec());
            }
            // 是否删除,0:未删除，1：已删除
            if(!StringUtils.isEmpty(spu.getIsDelete())){
                    criteria.andEqualTo("isDelete",spu.getIsDelete());
            }
            // 审核状态，0：未审核，1：已审核，2：审核不通过
            if(!StringUtils.isEmpty(spu.getStatus())){
                    criteria.andEqualTo("status",spu.getStatus());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Long id){
        spuMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改Spu
     * @param spu
     */
    @Override
    public void update(Spu spu){
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 增加Spu
     * @param spu
     */
    @Override
    public void add(Spu spu){
        spuMapper.insert(spu);
    }

    /**
     * 根据ID查询Spu
     * @param id
     * @return
     */
    @Override
    public Spu findById(Long id){
        return  spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Spu全部数据
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }
}
