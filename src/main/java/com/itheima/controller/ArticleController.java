package com.itheima.controller;

import com.itheima.pojo.ArticleDTO;
import com.itheima.pojo.ArticleVO;
import com.itheima.pojo.PageBean;
import com.itheima.pojo.Result;
import com.itheima.service.ArticleService;
import com.itheima.utils.ThreadLocalUtil;
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
    public Result add(@RequestBody @Validated ArticleDTO articleDTO) {
        articleService.add(articleDTO, currentUserId());
        return Result.success();
    }

    // 该接口用于根据条件查询文章,带分页
    @GetMapping
    public Result<PageBean<ArticleVO>> list(
            Integer pageNum,
            Integer pageSize,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String state
    ) {
        PageBean<ArticleVO> pageBean = articleService.list(pageNum, pageSize, categoryId, state, currentUserId());
        return Result.success(pageBean);
    }

    // 该接口用于更新文章信息
    @PutMapping
    public Result update(@RequestBody @Validated ArticleDTO articleDTO) {
        articleService.update(articleDTO, currentUserId());
        return Result.success();
    }

    // 该接口用于根据ID获取文章详细信息
    @GetMapping("/detail")
    public Result<ArticleVO> detail(@RequestParam Integer id) {
        return Result.success(articleService.findById(id, currentUserId()));
    }

    // 该接口用于根据ID删除文章
    @DeleteMapping
    public Result delete(@RequestParam Integer id) {
        articleService.delete(id, currentUserId());
        return Result.success();
    }

    private Integer currentUserId() {
        Map<String, Object> map = ThreadLocalUtil.get();
        return (Integer) map.get("id");
    }
}
