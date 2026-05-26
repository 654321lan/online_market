package com.mall.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.result.Result;
import com.mall.entity.Review;
import com.mall.entity.User;
import com.mall.mapper.ReviewMapper;
import com.mall.mapper.UserMapper;
import com.mall.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户端 - 评价接口
 */
@RestController
@RequestMapping("/api/review")
@Tag(name = "用户端-评价接口")
public class ReviewController {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/add")
    @Operation(summary = "发表评价")
    public Result<String> addReview(@RequestBody Review review) {
        // 检查是否已经评价过该订单的该商品
        Long count = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, review.getOrderId())
                .eq(Review::getProductId, review.getProductId())
                .eq(Review::getUserId, review.getUserId()));
        
        if (count > 0) {
            return Result.error("您已经评价过该商品了");
        }
        
        reviewMapper.insert(review);
        return Result.success("评价成功");
    }

    @GetMapping("/list")
    @Operation(summary = "我的评价")
    public Result<List<Review>> getMyReviews(@RequestParam Long userId) {
        List<Review> list = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, userId)
                .orderByDesc(Review::getCreateTime));
        return Result.success(list);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "商品评价列表")
    public Result<List<ReviewVO>> getProductReviews(@PathVariable Long productId) {
        List<Review> list = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getProductId, productId)
                .orderByDesc(Review::getCreateTime));
        
        // 转换为VO并填充用户信息
        List<ReviewVO> voList = new ArrayList<>();
        for (Review review : list) {
            ReviewVO vo = new ReviewVO();
            BeanUtils.copyProperties(review, vo);
            
            // 查询用户信息
            User user = userMapper.selectById(review.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
            
            voList.add(vo);
        }
        
        return Result.success(voList);
    }
}

