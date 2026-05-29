package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品评价实体
 */
@Data
@TableName("tb_review")
public class Review {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderId;
    
    private Long productId;
    
    private Long userId;
    
    private Integer rating;
    
    private String content;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

