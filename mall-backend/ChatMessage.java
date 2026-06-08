package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_chat_message")
public class ChatMessage {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long sessionId;
    
    private Integer senderType;
    
    private Long senderId;
    
    private Integer receiverType;
    
    private Long receiverId;
    
    private Integer messageType;
    
    private String content;
    
    private String dataSource;
    
    private String relatedData;
    
    private String attachmentUrl;
    
    private Integer isRead;
    
    private LocalDateTime readTime;
    
    private BigDecimal confidenceScore;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
