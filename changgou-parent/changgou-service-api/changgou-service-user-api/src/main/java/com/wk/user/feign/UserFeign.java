package com.wk.user.feign;

import com.wk.user.pojo.User;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("user")
@RequestMapping("user")
public interface UserFeign {

    /***
     * 根据ID查询User数据
     * @param id
     * @return
     */
    @GetMapping({"/load/{id}"})
    Result<User> findById(@PathVariable String id);

}
