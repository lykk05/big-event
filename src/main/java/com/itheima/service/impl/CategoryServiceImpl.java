package com.itheima.service.impl;

import com.itheima.exception.BizException;
import com.itheima.mapper.CategoryMapper;
import com.itheima.pojo.Category;
import com.itheima.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Category category, Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        category.setCreateTime(now);
        category.setUpdateTime(now);
        category.setCreateUser(userId);
        categoryMapper.add(category);
    }

    @Override
    public List<Category> list(Integer userId) {
        return categoryMapper.list(userId);
    }

    @Override
    public Category findById(Integer id, Integer userId) {
        Category category = categoryMapper.findById(id, userId);
        if (category == null) {
            throw new BizException(404, "分类不存在或无权访问");
        }
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Category category, Integer userId) {
        if (category.getId() == null) {
            throw new BizException(400, "分类id不能为空");
        }
        if (categoryMapper.findById(category.getId(), userId) == null) {
            throw new BizException(404, "分类不存在或无权操作");
        }
        category.setCreateUser(userId);
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.update(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id, Integer userId) {
        if (categoryMapper.findById(id, userId) == null) {
            throw new BizException(404, "分类不存在或无权操作");
        }
        categoryMapper.delete(id, userId);
    }
}
