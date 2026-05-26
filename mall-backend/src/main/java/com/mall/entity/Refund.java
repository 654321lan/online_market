package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退换货申请实体
 */
@Data
@TableName("tb_refund")
public class Refund {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long orderId;
    
    private String orderNo;
    
    private Long userId;
    
    private Long merchantId;
    
    private Integer type;
    
    private String reason;
    
    private String description;
    
    private Integer status;
    
    private String merchantReply;
    
    private BigDecimal refundAmount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
