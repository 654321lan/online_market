package com.mall.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.result.Result;
import com.mall.entity.Order;
import com.mall.entity.Product;
import com.mall.mapper.OrderMapper;
import com.mall.mapper.ProductMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家端 - 数据统计接口
 */
@RestController
@RequestMapping("/api/merchant/statistics")
@Tag(name = "商家端-数据统计")
public class MerchantStatisticsController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @GetMapping("/sales")
    @Operation(summary = "销售统计")
    public Result<Map<String, Object>> getSalesStatistics(@RequestParam Long merchantId) {
        Map<String, Object> result = new HashMap<>();

        // 查询所有订单
        List<Order> allOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, merchantId));

        // 查询已完成订单
        List<Order> completedOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, merchantId)
                .eq(Order::getStatus, 3));

        // 计算销售额
        BigDecimal totalSales = completedOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        result.put("totalSales", totalSales);
        result.put("orderCount", allOrders.size());
        result.put("completedCount", completedOrders.size());

        // 订单状态统计
        Map<String, Long> orderStatus = new HashMap<>();
        orderStatus.put("待支付", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, merchantId).eq(Order::getStatus, 0)));
        orderStatus.put("待发货", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, merchantId).eq(Order::getStatus, 1)));
        orderStatus.put("待收货", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, merchantId).eq(Order::getStatus, 2)));
        orderStatus.put("已完成", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, merchantId).eq(Order::getStatus, 3)));
        orderStatus.put("已取消", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, merchantId).eq(Order::getStatus, 4)));
        result.put("orderStatus", orderStatus);

        return Result.success(result);
    }

    @GetMapping("/product")
    @Operation(summary = "商品统计")
    public Result<Map<String, Object>> getProductStatistics(@RequestParam Long merchantId) {
        Map<String, Object> result = new HashMap<>();

        // 商品总数
        Long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId));

        // 上架商品数
        Long onSale = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId)
                .eq(Product::getStatus, 1));

        // 下架商品数
        Long offSale = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId)
                .eq(Product::getStatus, 0));

        // 库存预警（库存小于10）
        Long lowStock = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId)
                .lt(Product::getStock, 10));

        result.put("productCount", productCount);
        result.put("onSale", onSale);
        result.put("offSale", offSale);
        result.put("lowStock", lowStock);

        // 商品状态统计
        Map<String, Long> productStatus = new HashMap<>();
        productStatus.put("已上架", onSale);
        productStatus.put("已下架", offSale);
        result.put("productStatus", productStatus);

        return Result.success(result);
    }
}

