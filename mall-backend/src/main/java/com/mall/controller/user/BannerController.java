package com.mall.controller.user;

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
 * 用户端 - 轮播图接口
 */
@RestController
@RequestMapping("/api/banner")
@Tag(name = "用户端-轮播图接口")
public class BannerController {

    @Autowired
    private BannerMapper bannerMapper;

    @GetMapping("/list")
    @Operation(summary = "轮播图列表")
    public Result<List<Banner>> getBannerList() {
        List<Banner> list = bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                .eq(Banner::getStatus, 1)
                .orderByAsc(Banner::getSort));
        return Result.success(list);
    }
}

