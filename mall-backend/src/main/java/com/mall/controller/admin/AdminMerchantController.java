package com.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.result.Result;
import com.mall.entity.Merchant;
import com.mall.mapper.MerchantMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端 - 商家管理接口
 */
@RestController
@RequestMapping("/api/admin/merchant")
@Tag(name = "管理员端-商家管理")
public class AdminMerchantController {

    @Autowired
    private MerchantMapper merchantMapper;

    @GetMapping("/list")
    @Operation(summary = "商家列表")
    public Result<IPage<Merchant>> getMerchantList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        Page<Merchant> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Merchant::getShopName, keyword)
                    .or().like(Merchant::getUsername, keyword);
        }

        wrapper.orderByDesc(Merchant::getCreateTime);
        IPage<Merchant> result = merchantMapper.selectPage(pageParam, wrapper);

        // 清空密码
        result.getRecords().forEach(merchant -> merchant.setPassword(null));
        return Result.success(result);
    }

    @PostMapping("/add")
    @Operation(summary = "添加商家")
    public Result<String> addMerchant(@RequestBody Merchant merchant) {
        // 检查用户名是否已存在
        Merchant existMerchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getUsername, merchant.getUsername()));
        if (existMerchant != null) {
            return Result.error("用户名已存在");
        }

        merchant.setStatus(1);
        merchantMapper.insert(merchant);
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    @Operation(summary = "更新商家")
    public Result<String> updateMerchant(@RequestBody Merchant merchant) {
        merchant.setPassword(null); // 不允许修改密码
        merchantMapper.updateById(merchant);
        return Result.success("更新成功");
    }

    @PutMapping("/status/{id}")
    @Operation(summary = "启用/禁用商家")
    public Result<String> updateMerchantStatus(@PathVariable Long id, @RequestParam Integer status) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            return Result.error("商家不存在");
        }
        merchant.setStatus(status);
        merchantMapper.updateById(merchant);
        return Result.success(status == 1 ? "启用成功" : "禁用成功");
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除商家")
    public Result<String> deleteMerchant(@PathVariable Long id) {
        merchantMapper.deleteById(id);
        return Result.success("删除成功");
    }
}

