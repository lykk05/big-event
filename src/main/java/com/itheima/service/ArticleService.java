package com.itheima.service;

import com.itheima.pojo.ArticleDTO;
import com.itheima.pojo.ArticleVO;
import com.itheima.pojo.PageBean;

public interface ArticleService {
    // 新增文章
    void add(ArticleDTO articleDTO, Integer userId);

    // 条件分页列表查询
    PageBean<ArticleVO> list(Integer pageNum, Integer pageSize, Integer categoryId, String state, Integer userId);

    // 更新文章
    void update(ArticleDTO articleDTO, Integer userId);

    // 根据 ID 查询文章
    ArticleVO findById(Integer id, Integer userId);

    // 删除文章
    void delete(Integer id, Integer userId);
}
