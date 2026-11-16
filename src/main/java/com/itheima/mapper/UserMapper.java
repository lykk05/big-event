package com.itheima.mapper;

import com.itheima.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {
    // 根据用户名查询用户 --> 这里仅做练习，实际开发中不要用*
    @Select("select * from user where username = #{username}")
    User findByUsername(String username);

    // 添加用户
    @Insert("insert into user(username, password, create_time, update_time)" +
            " values (#{username}, #{password}, now(), now())")
    void add(String username, String password);

    // 更新用户
    @Update("update user set nickname = #{nickname}, email = #{email}, update_time = #{updateTime} where id = #{id}")
    void update(User user);

    @Update("update user set user_pic = #{avatarUrl}, update_time = now() where id = #{id}")
    void updateAvatar(String avatarUrl, Integer id);

    @Update("update user set password = #{passwordHash}, update_time = now() where id = #{id}")
    void updatePwd(@Param("passwordHash") String passwordHash, @Param("id") Integer id);
}
