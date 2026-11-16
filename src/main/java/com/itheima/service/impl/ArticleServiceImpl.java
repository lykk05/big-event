package com.itheima.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.convert.ArticleConverter;
import com.itheima.exception.BizException;
import com.itheima.mapper.ArticleMapper;
import com.itheima.pojo.Article;
import com.itheima.pojo.ArticleDTO;
import com.itheima.pojo.ArticleVO;
import com.itheima.pojo.PageBean;
import com.itheima.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(ArticleDTO articleDTO, Integer userId) {
        Article article = ArticleConverter.toEntity(articleDTO);
        LocalDateTime now = LocalDateTime.now();
        article.setCreateTime(now);
        article.setUpdateTime(now);
        article.setCreateUser(userId);
        articleMapper.add(article);
    }

    @Override
    public PageBean<ArticleVO> list(Integer pageNum, Integer pageSize, Integer categoryId, String state, Integer userId) {
        PageHelper.startPage(pageNum, pageSize);

        List<Article> articles = articleMapper.list(userId, categoryId, state);
        // Page 中提供了分页后的总记录数与当前页数据
        Page<Article> page = (Page<Article>) articles;
        List<ArticleVO> items = page.getResult().stream()
                .map(ArticleConverter::toVO)
                .toList();

        PageBean<ArticleVO> pageBean = new PageBean<>();
        pageBean.setTotal(page.getTotal());
        pageBean.setItems(items);
        return pageBean;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ArticleDTO articleDTO, Integer userId) {
        if (articleDTO.getId() == null) {
            throw new BizException(400, "文章id不能为空");
        }

        // 先判存在、再判归属，给出明确错误而不是静默失败
        Article db = articleMapper.findById(articleDTO.getId());
        if (db == null) {
            throw new BizException(404, "文章不存在");
        }
        if (!userId.equals(db.getCreateUser())) {
            throw new BizException(403, "无权操作该文章");
        }

        Article article = ArticleConverter.toEntity(articleDTO);
        article.setId(articleDTO.getId());
        article.setCreateUser(userId);
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.update(article);
    }

    @Override
    public ArticleVO findById(Integer id, Integer userId) {
        Article article = articleMapper.findOwnedById(id, userId);
        if (article == null) {
            throw new BizException(404, "文章不存在或无权访问");
        }
        return ArticleConverter.toVO(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id, Integer userId) {
        Article db = articleMapper.findById(id);
        if (db == null) {
            throw new BizException(404, "文章不存在");
        }
        if (!userId.equals(db.getCreateUser())) {
            throw new BizException(403, "无权操作该文章");
        }
        articleMapper.delete(id, userId);
    }
}
