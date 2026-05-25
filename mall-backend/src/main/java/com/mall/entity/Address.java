package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收货地址实体
 */
@Data
@TableName("tb_address")
public class Address {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String receiverName;
    
    private String receiverPhone;
    
    private String province;
    
    private String city;
    
    private String district;
    
    private String detail;
    
    private Integer isDefault;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

