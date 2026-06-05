package com.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.result.Result;
import com.mall.entity.Merchant;
import com.mall.entity.Order;
import com.mall.entity.Product;
import com.mall.entity.User;
import com.mall.mapper.MerchantMapper;
import com.mall.mapper.OrderMapper;
import com.mall.mapper.ProductMapper;
import com.mall.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员端 - 数据统计接口
 */
@RestController
@RequestMapping("/api/admin/statistics")
@Tag(name = "管理员端-数据统计")
public class AdminStatisticsController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @GetMapping("/overview")
    @Operation(summary = "平台数据概览")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> result = new HashMap<>();

        // 用户总数
        Long userCount = userMapper.selectCount(null);
        result.put("userCount", userCount);

        // 商家总数
        Long merchantCount = merchantMapper.selectCount(null);
        result.put("merchantCount", merchantCount);

        // 商品总数
        Long productCount = productMapper.selectCount(null);
        result.put("productCount", productCount);

        // 订单总数
        Long orderCount = orderMapper.selectCount(null);
        result.put("orderCount", orderCount);

        // 交易总额（已完成订单）
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 3));
        BigDecimal totalAmount = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalAmount", totalAmount);

        // 订单状态统计
        Map<String, Long> orderStatus = new HashMap<>();
        orderStatus.put("待支付", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 0)));
        orderStatus.put("待发货", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1)));
        orderStatus.put("待收货", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 2)));
        orderStatus.put("已完成", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 3)));
        orderStatus.put("已取消", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 4)));
        result.put("orderStatus", orderStatus);

        // 商品状态统计
        Map<String, Long> productStatus = new HashMap<>();
        productStatus.put("已上架", productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1)));
        productStatus.put("已下架", productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 0)));
        result.put("productStatus", productStatus);

        return Result.success(result);
    }
}

