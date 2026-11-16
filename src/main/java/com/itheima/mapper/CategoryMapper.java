package com.itheima.mapper;

import com.itheima.pojo.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    // 新增文章分类
    @Insert("insert into category(category_name, category_alias, create_user, create_time, update_time) " +
            "values(#{categoryName},#{categoryAlias},#{createUser},#{createTime},#{updateTime})")
    void add(Category category);

    // 查询文章分类
    @Select("select * from category where create_user = #{userId}")
    List<Category> list(Integer userId);

    // 根据id查询文章分类（带归属条件）
    @Select("select * from category where id = #{id} and create_user = #{userId}")
    Category findById(@Param("id") Integer id, @Param("userId") Integer userId);

    // 更新文章分类（带归属条件）
    @Update("update category set category_name = #{categoryName}, category_alias = #{categoryAlias}, " +
            "update_time = #{updateTime} where id = #{id} and create_user = #{createUser}")
    void update(Category category);

    // 删除文章分类（带归属条件）
    @Delete("delete from category where id = #{id} and create_user = #{createUser}")
    void delete(@Param("id") Integer id, @Param("createUser") Integer userId);
}
