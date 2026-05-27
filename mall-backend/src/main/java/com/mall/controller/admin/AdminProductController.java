package com.mall.controller.admin;

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
 * 管理员端 - 商品管理接口
 */
@RestController
@RequestMapping("/api/admin/product")
@Tag(name = "管理员端-商品管理")
public class AdminProductController {

    @Autowired
    private ProductMapper productMapper;

    @GetMapping("/list")
    @Operation(summary = "商品列表")
    public Result<IPage<Product>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getName, keyword);
        }

        wrapper.orderByDesc(Product::getCreateTime);
        IPage<Product> result = productMapper.selectPage(pageParam, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "商品详情")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        return Result.success(product);
    }

    @PutMapping("/status/{id}")
    @Operation(summary = "商品上下架")
    public Result<String> updateProductStatus(@PathVariable Long id, @RequestParam Integer status) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        product.setStatus(status);
        productMapper.updateById(product);
        return Result.success(status == 1 ? "上架成功" : "下架成功");
    }
}

