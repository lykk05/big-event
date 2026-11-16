package com.itheima.service.impl;

import com.itheima.exception.BizException;
import com.itheima.mapper.ArticleMapper;
import com.itheima.pojo.Article;
import com.itheima.pojo.ArticleDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {

    @Mock
    private ArticleMapper articleMapper;

    @InjectMocks
    private ArticleServiceImpl articleService;

    @Test
    void updateRejectsOtherUsersArticle() {
        Article db = new Article();
        db.setId(1);
        db.setCreateUser(2);
        when(articleMapper.findById(1)).thenReturn(db);

        BizException ex = assertThrows(BizException.class, () -> articleService.update(dto(1), 1));

        assertEquals(403, ex.getCode());
        verify(articleMapper, never()).update(any());
    }

    @Test
    void updateAllowsOwner() {
        Article db = new Article();
        db.setId(1);
        db.setCreateUser(1);
        when(articleMapper.findById(1)).thenReturn(db);

        articleService.update(dto(1), 1);

        verify(articleMapper).update(any(Article.class));
    }

    @Test
    void updateRejectsMissingArticle() {
        when(articleMapper.findById(1)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> articleService.update(dto(1), 1));

        assertEquals(404, ex.getCode());
        verify(articleMapper, never()).update(any());
    }

    @Test
    void deleteRejectsOtherUsersArticle() {
        Article db = new Article();
        db.setId(1);
        db.setCreateUser(2);
        when(articleMapper.findById(1)).thenReturn(db);

        BizException ex = assertThrows(BizException.class, () -> articleService.delete(1, 1));

        assertEquals(403, ex.getCode());
        verify(articleMapper, never()).delete(anyInt(), anyInt());
    }

    @Test
    void findByIdRejectsArticleOfAnotherUser() {
        when(articleMapper.findOwnedById(1, 1)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> articleService.findById(1, 1));

        assertEquals(404, ex.getCode());
    }

    private ArticleDTO dto(Integer id) {
        ArticleDTO dto = new ArticleDTO();
        dto.setId(id);
        dto.setTitle("title");
        dto.setContent("content");
        dto.setCoverImg("https://example.com/a.png");
        dto.setState("草稿");
        dto.setCategoryId(1);
        return dto;
    }
}
