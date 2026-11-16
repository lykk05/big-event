package com.itheima.convert;

import com.itheima.pojo.Article;
import com.itheima.pojo.ArticleDTO;
import com.itheima.pojo.ArticleVO;

/**
 * Article 的 DTO / 实体 / VO 转换。字段少，手写比引入 MapStruct 更轻。
 */
public final class ArticleConverter {

    private ArticleConverter() {
    }

    public static Article toEntity(ArticleDTO dto) {
        Article article = new Article();
        article.setId(dto.getId());
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setCoverImg(dto.getCoverImg());
        article.setState(dto.getState());
        article.setCategoryId(dto.getCategoryId());
        return article;
    }

    public static ArticleVO toVO(Article article) {
        if (article == null) {
            return null;
        }
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setContent(article.getContent());
        vo.setCoverImg(article.getCoverImg());
        vo.setState(article.getState());
        vo.setCategoryId(article.getCategoryId());
        vo.setCreateTime(article.getCreateTime());
        vo.setUpdateTime(article.getUpdateTime());
        return vo;
    }
}
