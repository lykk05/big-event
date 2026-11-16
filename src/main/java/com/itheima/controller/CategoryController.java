package com.itheima.controller;

import com.itheima.pojo.Category;
import com.itheima.pojo.Result;
import com.itheima.service.CategoryService;
import com.itheima.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 该接口用于新增文章分类
    @PostMapping
    public Result add(@RequestBody @Validated(Category.Add.class) Category category) {
        categoryService.add(category, currentUserId());
        return Result.success();
    }

    // 该接口用于获取当前已登录用户创建的所有文章分类
    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryService.list(currentUserId()));
    }

    // 该接口用于根据ID获取文章分类详情
    @GetMapping("/detail")
    public Result<Category> detail(@RequestParam Integer id) {
        return Result.success(categoryService.findById(id, currentUserId()));
    }

    // 该接口用于更新文章分类
    @PutMapping
    public Result update(@RequestBody @Validated(Category.Update.class) Category category) {
        categoryService.update(category, currentUserId());
        return Result.success();
    }

    // 该接口用于删除文章分类
    @DeleteMapping
    public Result delete(@RequestParam Integer id) {
        categoryService.delete(id, currentUserId());
        return Result.success();
    }

    private Integer currentUserId() {
        Map<String, Object> map = ThreadLocalUtil.get();
        return (Integer) map.get("id");
    }
}
