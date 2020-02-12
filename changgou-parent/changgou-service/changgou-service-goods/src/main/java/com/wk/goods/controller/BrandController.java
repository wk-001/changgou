package com.wk.goods.controller;

import com.github.pagehelper.PageInfo;
import com.wk.goods.pojo.Brand;
import com.wk.goods.service.BrandService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
@CrossOrigin        //跨域：A域名访问B域名的数据。域名、请求端口、协议不一致时，就发生跨域
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * 带条件的分页查询品牌
     * @param page：当前页
     * @param size：每页显示多少条数据
     */
    @PostMapping("/search/{page}/{size}")
    public Result<PageInfo<Brand>> findPage(@RequestBody Brand brand,@PathVariable("page")Integer page,@PathVariable("size")Integer size){
        int i = 10/0;
        return new Result(true, StatusCode.OK,"分页查询品牌成功", brandService.findPage(brand,page,size));
    }

    /**
     * 分页查询品牌
     * @param page：当前页
     * @param size：每页显示多少条数据
     */
    @GetMapping("/search/{page}/{size}")
    public Result<PageInfo<Brand>> findPage(@PathVariable("page")Integer page,@PathVariable("size")Integer size){
        return new Result(true, StatusCode.OK,"分页查询品牌成功", brandService.findPage(page,size));
    }

    /**
     * 根据条件查询
     * 需要接受前端的参数，所以用PostMapping
     */
    @PostMapping("search")
    public Result<List<Brand>> findList(@RequestBody Brand brand){
        return new Result(true, StatusCode.OK,"根据条件查询品牌成功",brandService.findList(brand));
    }

    /**
     * 查询全部品牌
     */
    @GetMapping
    public Result<List<Brand>> findAll(){
        //封装响应结果
        return new Result(true, StatusCode.OK,"查询所有品牌成功",brandService.findAll());
    }

    /**
     * 根据主键查询品牌
     */
    @GetMapping("/{id}")
    public Result<Brand> findById(@PathVariable("id") Integer id){
        return new Result<>(true,StatusCode.OK,"根据ID："+id+"查询品牌成功",brandService.findById(id));
    }

    /**
     * 添加品牌
     */
    @PostMapping
    public Result addBrand(@RequestBody Brand brand){
        brandService.addBrand(brand);
        return new Result<>(true,StatusCode.OK,"增加品牌成功");
    }

    /**
     * 根据ID修改品牌
     *
    @PutMapping("/{id}")
    public Result updateBrand(@PathVariable("id") Integer id,@RequestBody Brand brand){
        brand.setId(id);
        brandService.updateBrand(brand);
        return new Result<>(true,StatusCode.OK,"修改品牌成功");
    }*/
    @PutMapping
    public Result updateBrand(@RequestBody Brand brand){
        brandService.updateBrand(brand);
        return new Result<>(true,StatusCode.OK,"修改品牌成功");
    }

    /**
     * 根据ID删除品牌
     */
    @DeleteMapping("/{id}")
    public Result deleteBrand(@PathVariable("id") Integer id){
        brandService.deleteBrand(id);
        return new Result<>(true,StatusCode.OK,"删除品牌成功");
    }
}
