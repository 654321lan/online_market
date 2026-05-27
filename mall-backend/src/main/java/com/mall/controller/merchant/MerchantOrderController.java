package com.mall.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.result.Result;
import com.mall.entity.Order;
import com.mall.entity.OrderItem;
import com.mall.mapper.OrderItemMapper;
import com.mall.mapper.OrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商家端 - 订单管理接口
 */
@RestController
@RequestMapping("/api/merchant/order")
@Tag(name = "商家端-订单管理")
public class MerchantOrderController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @GetMapping("/list")
    @Operation(summary = "订单列表")
    public Result<IPage<Order>> getOrderList(
            @RequestParam Long merchantId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {

        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getMerchantId, merchantId);

        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        wrapper.orderByDesc(Order::getCreateTime);
        IPage<Order> result = orderMapper.selectPage(pageParam, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "订单详情")
    public Result<Order> getOrderDetail(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        return Result.success(order);
    }

    @GetMapping("/items/{orderId}")
    @Operation(summary = "订单明细")
    public Result<List<OrderItem>> getOrderItems(@PathVariable Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        return Result.success(items);
    }

    @PutMapping("/ship/{id}")
    @Operation(summary = "订单发货")
    public Result<String> shipOrder(@PathVariable Long id,
                                    @RequestParam String trackingNo,
                                    @RequestParam String expressCompany) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }

        if (order.getStatus() != 1) {
            return Result.error("订单状态不正确");
        }

        order.setStatus(2); // 待收货
        order.setTrackingNo(trackingNo);
        order.setExpressCompany(expressCompany);
        order.setShipTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return Result.success("发货成功");
    }
}

