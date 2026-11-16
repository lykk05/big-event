package com.itheima.service;

import com.itheima.pojo.Category;

import java.util.List;

public interface CategoryService {

    // 添加文章分类
    void add(Category category, Integer userId);

    // 查询文章分类
    List<Category> list(Integer userId);

    // 根据id查询文章分类
    Category findById(Integer id, Integer userId);

    // 修改文章分类
    void update(Category category, Integer userId);

    // 删除文章分类
    void delete(Integer id, Integer userId);
}
