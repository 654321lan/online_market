package com.mall.controller.merchant;

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
 * 商家端 - 商品管理接口
 */
@RestController
@RequestMapping("/api/merchant/product")
@Tag(name = "商家端-商品管理")
public class MerchantProductController {

    @Autowired
    private ProductMapper productMapper;

    @PostMapping("/add")
    @Operation(summary = "发布商品")
    public Result<String> addProduct(@RequestBody Product product) {
        product.setStatus(1); // 默认上架
        product.setSales(0);
        productMapper.insert(product);
        return Result.success("发布成功");
    }

    @GetMapping("/list")
    @Operation(summary = "商品列表")
    public Result<IPage<Product>> getProductList(
            @RequestParam Long merchantId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getMerchantId, merchantId);

        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
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

    @PutMapping("/update")
    @Operation(summary = "更新商品")
    public Result<String> updateProduct(@RequestBody Product product) {
        productMapper.updateById(product);
        return Result.success("更新成功");
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

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除商品")
    public Result<String> deleteProduct(@PathVariable Long id) {
        productMapper.deleteById(id);
        return Result.success("删除成功");
    }
}

