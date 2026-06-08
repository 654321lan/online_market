package com.mall.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品评价视图对象（包含用户信息）
 */
@Data
public class ReviewVO {
    
    private Long id;
    
    private Long orderId;
    
    private Long productId;
    
    private Long userId;
    
    private Integer rating;
    
    private String content;
    
    private Integer likeCount;
    
    private String merchantReply;
    
    private LocalDateTime replyTime;
    
    private Boolean replied;
    
    private LocalDateTime createTime;
    
    // 用户信息
    private String username;
    
    private String nickname;
    
    private String avatar;
    
    // 新增字段
    private java.util.List<String> images;
    
    private Boolean isLiked;
}