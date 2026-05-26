package com.mall.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.result.Result;
import com.mall.entity.Category;
import com.mall.mapper.CategoryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端 - 分类接口
 */
@RestController
@RequestMapping("/api/category")
@Tag(name = "用户端-分类接口")
public class CategoryController {

    @Autowired
    private CategoryMapper categoryMapper;

    @GetMapping("/list")
    @Operation(summary = "分类列表")
    public Result<List<Category>> getCategoryList(@RequestParam(required = false) Long parentId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1);
        if (parentId != null) {
            wrapper.eq(Category::getParentId, parentId);
        } else {
            wrapper.eq(Category::getParentId, 0);
        }
        wrapper.orderByAsc(Category::getSort);
        
        List<Category> list = categoryMapper.selectList(wrapper);
        return Result.success(list);
    }
}

