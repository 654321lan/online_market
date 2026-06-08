package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_login_log")
public class LoginLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String username;
    
    private String loginIp;
    
    private String userAgent;
    
    private String loginStatus;
    
    private String failureReason;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
