package com.itheima.controller;

import com.itheima.pojo.Article;
import com.itheima.pojo.PageBean;
import com.itheima.pojo.Result;
import com.itheima.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;
    // 新增文章
    @PostMapping
    public Result add(@RequestBody @Validated Article article){
        articleService.add(article);
        return Result.success();
    }

    // 该接口用于根据条件查询文章,带分页
    @GetMapping
    public Result<PageBean<Article>> list(
            Integer pageNum,
            Integer pageSize,
            @RequestParam (required = false) Integer categoryId,
            @RequestParam (required = false)String state
    ){
        PageBean<Article> pb = articleService.list(pageNum, pageSize, categoryId, state);
        return Result.success(pb);
    }

    // 该接口用于更新文章信息
    @PutMapping
    public Result update(@RequestBody @Validated Article article){
        articleService.update(article);
        return Result.success();
    }

    // 该接口用于根据ID获取文章详细信息
    @GetMapping("/detail")
    public Result<Article> detail(@RequestParam Integer id){
        Article article = articleService.findById(id);
        return Result.success(article);
    }

    // 该接口用于根据ID删除文章
    @DeleteMapping
    public Result delete(@RequestParam Integer id){
        articleService.delete(id);
        return Result.success();
    }

}
