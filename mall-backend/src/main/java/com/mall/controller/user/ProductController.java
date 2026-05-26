package com.mall.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.result.Result;
import com.mall.entity.Product;
import com.mall.mapper.ProductMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端 - 商品接口
 */
@RestController
@RequestMapping("/api/product")
@Tag(name = "用户端-商品接口")
public class ProductController {

    @Autowired
    private ProductMapper productMapper;

    @GetMapping("/list")
    @Operation(summary = "商品列表")
    public Result<IPage<Product>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort) {
        
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询上架的商品
        wrapper.eq(Product::getStatus, 1);
        
        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        
        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getName, keyword);
        }
        
        // 排序
        if ("price".equals(sort)) {
            wrapper.orderByAsc(Product::getPrice);
        } else if ("sales".equals(sort)) {
            wrapper.orderByDesc(Product::getSales);
        } else {
            wrapper.orderByDesc(Product::getCreateTime);
        }
        
        IPage<Product> result = productMapper.selectPage(pageParam, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "商品详情")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }
}

