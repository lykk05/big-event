package com.itheima.service;


import com.itheima.pojo.Article;
import com.itheima.pojo.PageBean;

public interface ArticleService {
    // 新增文章
    void add(Article article);

    // 该接口用于根据条件查询文章,带分页(条件分页列表查询)
    PageBean<Article> list(Integer pageNum, Integer pageSize, Integer categoryId, String state);

    // 该接口用于更新文章
    void update(Article article);

    // 该接口用于根据ID查询文章
    Article findById(Integer id);

    // 该接口用于删除文章
    void delete(Integer id);
}
