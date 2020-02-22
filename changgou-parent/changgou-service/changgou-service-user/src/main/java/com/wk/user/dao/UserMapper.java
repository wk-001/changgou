package com.wk.user.dao;
import com.wk.user.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:admin
 * @Description:User的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface UserMapper extends Mapper<User> {

    @Update("update tb_user set points=points+#{points} where username=#{username}")
    void addUserPoints(@Param("username") String username,@Param("points") Integer points);
}
