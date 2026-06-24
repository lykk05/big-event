package com.itheima.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.mapper.ArticleMapper;
import com.itheima.pojo.Article;
import com.itheima.pojo.PageBean;
import com.itheima.service.ArticleService;
import com.itheima.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;


    @Override
    public void add(Article article) {
        // 补充属性值
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer id = (Integer)map.get("id");
        article.setCreateUser(id);
        articleMapper.add(article);
    }

    @Override
    public PageBean<Article> list(Integer pageNum, Integer pageSize, Integer categoryId, String state) {
        // 1.创建PageBean对象
        PageBean<Article> pb = new PageBean<>();

        // 2.开启分页查询 PageHelper
        PageHelper.startPage(pageNum, pageSize);

        // 3.调用mapper
        // 获取ID
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer id = (Integer)map.get("id");
        List<Article> as = articleMapper.list(id, categoryId, state);
        // Page中提供了方法，可以获取PageHelper分页查询后得到的总记录条数和当前页数据
        Page<Article> p = (Page<Article>) as;

        // 最终封装PageBean对象
        pb.setTotal(p.getTotal());
        pb.setItems(p.getResult());
        return pb;
    }

    @Override
    public void update(Article article) {
        // 补充属性值
        article.setUpdateTime(LocalDateTime.now());
        Map<String, Object> map =ThreadLocalUtil.get() ;
        Integer id = (Integer)map.get("id");
        article.setCreateUser(id);
        articleMapper.update(article);
    }

    @Override
    public Article findById(Integer id) {
        Article c = articleMapper.findById(id);
        return c;
    }

    @Override
    public void delete(Integer id) {
        articleMapper.delete(id);
    }
}
