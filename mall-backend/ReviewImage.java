package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价图片实体
 */
@Data
@TableName("tb_review_image")
public class ReviewImage {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("review_id")
    private Long reviewId;
    
    @TableField("image_url")
    private String imageUrl;
    
    @TableField("sort_order")
    private Integer sortOrder;
    
    @TableField("create_time")
    private LocalDateTime createTime;
}