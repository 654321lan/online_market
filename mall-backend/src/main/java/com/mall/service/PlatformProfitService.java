package com.mall.service;

import com.mall.entity.PlatformProfit;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 平台收益服务接口
 */
public interface PlatformProfitService {
    
    /**
     * 计算并记录订单佣金
     */
    void calculateOrderCommission(Long orderId);
    
    /**
     * 计算充值服务费
     */
    BigDecimal calculateRechargeServiceFee(Long userId, BigDecimal amount);
    
    /**
     * 记录充值服务费
     */
    void recordRechargeServiceFee(Long userId, Long rechargeId, BigDecimal amount, BigDecimal serviceFee);
    
    /**
     * 获取平台收益统计
     */
    Map<String, Object> getProfitStatistics(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 获取平台收益趋势
     */
    List<Map<String, Object>> getProfitTrend(LocalDateTime startDate, LocalDateTime endDate);
}