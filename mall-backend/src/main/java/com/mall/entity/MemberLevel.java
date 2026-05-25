package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员等级实体
 */
@Data
@TableName("tb_member_level")
public class MemberLevel {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private Integer level;
    
    private BigDecimal minAmount;
    
    private BigDecimal discount;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
