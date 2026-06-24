package com.itheima.service;

import com.itheima.pojo.Category;

import java.util.List;

public interface CategoryService {

    // 添加文章分类
    void add(Category category);

    // 查询文章分类
    List<Category> list();

    // 根据id查询文章分类
    Category fingById(Integer id);

    // 修改文章分类
    void update(Category category);

    // 删除文章分类
    void delete(Integer id);
}
