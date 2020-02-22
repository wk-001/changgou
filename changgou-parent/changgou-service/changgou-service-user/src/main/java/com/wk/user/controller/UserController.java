package com.wk.user.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.wk.user.pojo.User;
import com.wk.user.service.UserService;
import entity.BCrypt;
import entity.JwtUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/****
 * @Author:admin
 * @Description:
 * @Date 2019/6/14 0:18
 *****/

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * @param username：用户名
     * @param password：密码
     * @return
     */
    @GetMapping("/login")
    public Result login(String username, String password, HttpServletResponse response){
        //查询用户信息
        User user = userService.findById(username);//username是主键
        //对比密码
        if(BCrypt.checkpw(password,user.getPassword())){
            //设置用户令牌信息
            Map<String,Object> info = new HashMap<String,Object>();
            info.put("role","USER");
            info.put("success","SUCCESS");
            info.put("username",username);
            String token = JwtUtil.createJWT(UUID.randomUUID().toString(), JSON.toJSONString(info), null);

            //令牌存入到cookie
            Cookie cookie = new Cookie("Authorization",token);
            cookie.setDomain("localhost");      //cookie所属的域名
            cookie.setPath("/");                //cookie存储到根路径下
            response.addCookie(cookie);

            //把令牌作为参数传给用户
            //密码匹配，登录成功
            return new Result(true,StatusCode.OK,"登录成功！",token);
        }
        //密码不匹配,登录失败
        return new Result(false,StatusCode.LOGINERROR,"用户名或密码有误！");
    }

    /***
     * User分页条件搜索实现
     * @param user
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@RequestBody(required = false)  User user, @PathVariable  int page, @PathVariable  int size){
        //调用UserService实现分页条件查询User
        PageInfo<User> pageInfo = userService.findPage(user, page, size);
        return new Result(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * User分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@PathVariable  int page, @PathVariable  int size){
        //调用UserService实现分页查询User
        PageInfo<User> pageInfo = userService.findPage(page, size);
        return new Result<PageInfo>(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * 多条件搜索品牌数据
     * @param user
     * @return
     */
    @PostMapping(value = "/search" )
    public Result<List<User>> findList(@RequestBody(required = false)  User user){
        //调用UserService实现条件查询User
        List<User> list = userService.findList(user);
        return new Result<List<User>>(true,StatusCode.OK,"查询成功",list);
    }

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable String id){
        //调用UserService实现根据主键删除
        userService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 修改User数据
     * @param user
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    public Result update(@RequestBody  User user,@PathVariable String id){
        //设置主键值
        user.setUsername(id);
        //调用UserService实现修改User
        userService.update(user);
        return new Result(true,StatusCode.OK,"修改成功");
    }

    /***
     * 新增User数据
     * @param user
     * @return
     */
    @PostMapping
    public Result add(@RequestBody   User user){
        //调用UserService实现添加User
        userService.add(user);
        return new Result(true,StatusCode.OK,"添加成功");
    }

    /***
     * 根据ID查询User数据
     * @param id
     * @return
     */
    @GetMapping({"/{id}","/load/{id}"})
    public Result<User> findById(@PathVariable String id){
        //调用UserService实现根据主键查询User
        User user = userService.findById(id);
        return new Result<User>(true,StatusCode.OK,"查询成功",user);
    }

    /***
     * 查询User全部数据
     * 设置访问权限，只有指定角色才能访问
     * @return
     */
    @PreAuthorize("hasAnyRole('admin')")
    @GetMapping
    public Result<List<User>> findAll(){
        //调用UserService实现查询所有User
        List<User> list = userService.findAll();
        return new Result<List<User>>(true, StatusCode.OK,"查询成功",list) ;
    }
}
