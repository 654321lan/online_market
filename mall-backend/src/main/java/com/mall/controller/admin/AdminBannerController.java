package com.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.result.Result;
import com.mall.entity.Banner;
import com.mall.mapper.BannerMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员端 - 轮播图管理接口
 */
@RestController
@RequestMapping("/api/admin/banner")
@Tag(name = "管理员端-轮播图管理")
public class AdminBannerController {

    @Autowired
    private BannerMapper bannerMapper;

    @GetMapping("/list")
    @Operation(summary = "轮播图列表")
    public Result<List<Banner>> getBannerList() {
        List<Banner> list = bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>().orderByAsc(Banner::getSort));
        return Result.success(list);
    }

    @PostMapping("/add")
    @Operation(summary = "添加轮播图")
    public Result<String> addBanner(@RequestBody Banner banner) {
        banner.setStatus(1);
        bannerMapper.insert(banner);
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    @Operation(summary = "更新轮播图")
    public Result<String> updateBanner(@RequestBody Banner banner) {
        bannerMapper.updateById(banner);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除轮播图")
    public Result<String> deleteBanner(@PathVariable Long id) {
        bannerMapper.deleteById(id);
        return Result.success("删除成功");
    }
}

