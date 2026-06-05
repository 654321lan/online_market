package com.mall.controller.admin;

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
 * 管理员端 - 分类管理接口
 */
@RestController
@RequestMapping("/api/admin/category")
@Tag(name = "管理员端-分类管理")
public class AdminCategoryController {

    @Autowired
    private CategoryMapper categoryMapper;

    @GetMapping("/list")
    @Operation(summary = "分类列表")
    public Result<List<Category>> getCategoryList() {
        List<Category> list = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));
        return Result.success(list);
    }

    @PostMapping("/add")
    @Operation(summary = "添加分类")
    public Result<String> addCategory(@RequestBody Category category) {
        category.setStatus(1);
        categoryMapper.insert(category);
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    @Operation(summary = "更新分类")
    public Result<String> updateCategory(@RequestBody Category category) {
        categoryMapper.updateById(category);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除分类")
    public Result<String> deleteCategory(@PathVariable Long id) {
        // 检查是否有子分类
        Long count = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>().eq(Category::getParentId, id));
        if (count > 0) {
            return Result.error("请先删除子分类");
        }

        categoryMapper.deleteById(id);
        return Result.success("删除成功");
    }
}

