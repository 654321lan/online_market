package com.mall.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.result.Result;
import com.mall.entity.Order;
import com.mall.entity.Refund;
import com.mall.mapper.OrderMapper;
import com.mall.mapper.RefundMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户端 - 退换货接口
 */
@RestController
@RequestMapping("/api/refund")
@Tag(name = "用户端-退换货接口")
public class RefundController {

    @Autowired
    private RefundMapper refundMapper;

    @Autowired
    private OrderMapper orderMapper;

    @PostMapping("/apply")
    @Operation(summary = "申请退换货")
    public Result<String> applyRefund(@RequestBody Refund refund) {
        // 检查订单是否存在
        Order order = orderMapper.selectById(refund.getOrderId());
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 只有已完成或待收货的订单才能申请退换货
        if (order.getStatus() != 2 && order.getStatus() != 3) {
            return Result.error("当前订单状态不支持退换货申请");
        }

        // 检查是否已经申请过
        Long count = refundMapper.selectCount(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getOrderId, refund.getOrderId())
                .eq(Refund::getUserId, refund.getUserId())
                .in(Refund::getStatus, 0, 1));
        if (count > 0) {
            return Result.error("该订单已有进行中的退换货申请");
        }

        // 设置订单编号和商家ID
        refund.setOrderNo(order.getOrderNo());
        refund.setMerchantId(order.getMerchantId());
        refund.setStatus(0); // 待处理
        refund.setRefundAmount(order.getTotalAmount());
        refundMapper.insert(refund);

        return Result.success("申请提交成功，请等待商家处理");
    }

    @GetMapping("/list")
    @Operation(summary = "我的退换货列表")
    public Result<IPage<Refund>> getRefundList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {

        Page<Refund> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Refund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Refund::getUserId, userId);

        if (status != null) {
            wrapper.eq(Refund::getStatus, status);
        }

        wrapper.orderByDesc(Refund::getCreateTime);
        IPage<Refund> result = refundMapper.selectPage(pageParam, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "退换货详情")
    public Result<Refund> getRefundDetail(@PathVariable Long id) {
        Refund refund = refundMapper.selectById(id);
        return Result.success(refund);
    }

    @GetMapping("/active-orders")
    @Operation(summary = "获取有进行中退换货的订单ID列表")
    public Result<List<Long>> getActiveRefundOrderIds(@RequestParam Long userId) {
        List<Refund> refunds = refundMapper.selectList(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getUserId, userId)
                .in(Refund::getStatus, 0, 1));
        List<Long> orderIds = refunds.stream()
                .map(Refund::getOrderId)
                .collect(Collectors.toList());
        return Result.success(orderIds);
    }
}
