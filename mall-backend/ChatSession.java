package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_chat_session")
public class ChatSession {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long merchantId;
    
    private Long productId;
    
    private Long orderId;
    
    private Integer sessionType;
    
    private String lastMessage;
    
    private LocalDateTime lastMessageTime;
    
    private Integer userUnreadCount;
    
    private Integer merchantUnreadCount;
    
    private Integer aiEnabled;
    
    private Integer aiConsecutiveReplies;
    
    private Integer needHumanIntervention;
    
    private Integer humanReplyCount;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}