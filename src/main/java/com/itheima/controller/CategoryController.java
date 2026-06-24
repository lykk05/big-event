package com.itheima.controller;

import com.itheima.pojo.Category;
import com.itheima.pojo.Result;
import com.itheima.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 该接口用于新增文章分类
    @PostMapping
    public Result add(@RequestBody @Validated(Category.Add.class) Category  category){
        categoryService.add(category);
        return Result.success();
    }

    // 该接口用于获取当前已登录用户创建的所有文章分类
    @GetMapping
    public Result<List<Category>> list(){
        List<Category> cs = categoryService.list();
        return Result.success(cs);
    }

    // 该接口用于根据ID获取文章分类详情
    @GetMapping("/detail")
    public Result<Category> detail(@RequestParam Integer id){
        Category c = categoryService.fingById(id);
        return Result.success(c);
    }


    // 该接口用于更新文章分类

    // @RequestBody 的作用是：
    //告诉 Spring：这个参数 category 的数据不是从 URL 参数里拿，而是从请求体 body 里的 JSON 数据中拿，然后转换成 Java 对象。
    @PutMapping
    public Result update(@RequestBody @Validated(Category.Update.class) Category category){
        categoryService.update(category);
        return Result.success();
    }


    // 该接口用于删除文章分类
    @DeleteMapping
    public Result delete(@RequestParam Integer id){
        categoryService.delete(id);
        return Result.success();
    }
}
