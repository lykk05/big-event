package com.itheima.mapper;

import com.itheima.pojo.Article;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ArticleMapper {
    // 新增文章
    @Insert("insert into article(title,content,cover_img,state,category_id,create_user,create_time,update_time)" +
            " values(#{title},#{content},#{coverImg},#{state},#{categoryId},#{createUser},#{createTime},#{updateTime})")
    void add(Article article);

    List<Article> list(Integer id, Integer categoryId, String state);

    // 更新文章：带归属条件，防止越权修改
    @Update("update article set title=#{title},content=#{content},cover_img=#{coverImg},state=#{state},category_id=#{categoryId},update_time=#{updateTime} where id=#{id} and create_user=#{createUser}")
    void update(Article article);

    // 根据ID查询文章（仅用于存在性与归属校验）
    @Select("select * from article where id=#{id}")
    Article findById(Integer id);

    // 根据ID查询文章：带归属条件，防止越权读取
    @Select("select * from article where id=#{id} and create_user=#{createUser}")
    Article findOwnedById(@Param("id") Integer id, @Param("createUser") Integer createUser);

    // 删除文章：带归属条件，防止越权删除
    @Delete("delete from article where id=#{id} and create_user=#{createUser}")
    void delete(@Param("id") Integer id, @Param("createUser") Integer createUser);
}
