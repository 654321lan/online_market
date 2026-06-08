// 文件路径: d:\sw1\web\期末大作业\online_market\mall\mall\mall-backend\src\main\java\com\mall\entity\ConsumptionRecord.java

package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费记录实体
 */
@Data
@TableName("tb_consumption_record")
public class ConsumptionRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long orderId;
    
    private BigDecimal amount;
    
    private String paymentMethod;
    
    private BigDecimal balancePayment;
    
    private BigDecimal thirdPartyPayment;
    
    private BigDecimal remainingBalance;
    
    private LocalDateTime createTime;
}