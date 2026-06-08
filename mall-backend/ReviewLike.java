package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价点赞实体
 */
@Data
@TableName("tb_review_like")
public class ReviewLike {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("review_id")
    private Long reviewId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("create_time")
    private LocalDateTime createTime;
}