package com.mall.service.impl;

import com.mall.entity.Order;
import com.mall.entity.PlatformProfit;
import com.mall.entity.User;
import com.mall.mapper.OrderMapper;
import com.mall.mapper.PlatformProfitMapper;
import com.mall.mapper.UserMapper;
import com.mall.service.PlatformProfitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 平台收益服务实现类
 */
@Service
public class PlatformProfitServiceImpl implements PlatformProfitService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private PlatformProfitMapper profitMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.03"); // 默认3%佣金
    
    @Override
    public void calculateOrderCommission(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 1) {
            return;
        }
        
        // 计算佣金金额
        BigDecimal commissionAmount = order.getTotalAmount()
            .multiply(DEFAULT_COMMISSION_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        
        // 创建收益记录
        PlatformProfit profit = new PlatformProfit();
        profit.setProfitType("commission");
        profit.setOrderId(orderId);
        profit.setMerchantId(order.getMerchantId());
        profit.setUserId(order.getUserId());
        profit.setProfitAmount(commissionAmount);
        profit.setOriginalAmount(order.getTotalAmount());
        profit.setRate(DEFAULT_COMMISSION_RATE.multiply(new BigDecimal("100")));
        profit.setDescription("订单佣金收入 - 订单号:" + order.getOrderNo());
        profit.setStatus(1);
        
        profitMapper.insert(profit);
        
        // 更新订单佣金信息
        order.setCommissionAmount(commissionAmount);
        order.setCommissionRate(DEFAULT_COMMISSION_RATE.multiply(new BigDecimal("100")));
        orderMapper.updateById(order);
    }
    
    @Override
    public BigDecimal calculateRechargeServiceFee(Long userId, BigDecimal amount) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return BigDecimal.ZERO;
        }
        
        // 根据会员等级计算服务费
        Integer memberLevel = user.getMemberLevel() != null ? user.getMemberLevel() : 0;
        BigDecimal serviceFeeRate = BigDecimal.ZERO;
        
        if (memberLevel == 1) { // VIP会员
            serviceFeeRate = new BigDecimal("0.01");
        } else if (memberLevel == 2) { // SVIP会员
            serviceFeeRate = new BigDecimal("0.005");
        }
        
        BigDecimal serviceFee = amount.multiply(serviceFeeRate)
            .setScale(2, RoundingMode.HALF_UP);
        
        return serviceFee;
    }
    
    @Override
    public void recordRechargeServiceFee(Long userId, Long rechargeId, BigDecimal amount, BigDecimal serviceFee) {
        if (serviceFee.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        
        // 创建收益记录
        PlatformProfit profit = new PlatformProfit();
        profit.setProfitType("service_fee");
        profit.setUserId(userId);
        profit.setProfitAmount(serviceFee);
        profit.setOriginalAmount(amount);
        profit.setRate(serviceFee.divide(amount, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
        profit.setDescription("充值服务费 - 充值ID:" + rechargeId);
        profit.setStatus(1);
        
        profitMapper.insert(profit);
    }
    
    @Override
    public Map<String, Object> getProfitStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> result = new HashMap<>();
        
        // 总收益
        BigDecimal totalProfit = profitMapper.selectTotalProfit(startDate, endDate);
        result.put("totalProfit", totalProfit != null ? totalProfit : BigDecimal.ZERO);
        
        // 按类型统计
        List<Map<String, Object>> profitByType = profitMapper.selectProfitByType(startDate, endDate);
        Map<String, BigDecimal> typeMap = new HashMap<>();
        for (Map<String, Object> item : profitByType) {
            String type = (String) item.get("profit_type");
            BigDecimal amount = (BigDecimal) item.get("amount");
            typeMap.put(type, amount);
        }
        result.put("profitByType", typeMap);
        
        // 按日期统计
        List<Map<String, Object>> profitByDate = profitMapper.selectProfitByDate(startDate, endDate);
        result.put("profitByDate", profitByDate);
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getProfitTrend(LocalDateTime startDate, LocalDateTime endDate) {
        return profitMapper.selectProfitTrend(startDate, endDate);
    }
}