package com.mall.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.result.Result;
import com.mall.entity.Refund;
import com.mall.mapper.RefundMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商家端 - 退换货管理接口
 */
@RestController
@RequestMapping("/api/merchant/refund")
@Tag(name = "商家端-退换货管理")
public class MerchantRefundController {

    @Autowired
    private RefundMapper refundMapper;

    @GetMapping("/list")
    @Operation(summary = "退换货列表")
    public Result<IPage<Refund>> getRefundList(
            @RequestParam Long merchantId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {

        Page<Refund> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Refund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Refund::getMerchantId, merchantId);

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

    @PutMapping("/approve/{id}")
    @Operation(summary = "同意退换货")
    public Result<String> approveRefund(@PathVariable Long id, @RequestParam String reply) {
        Refund refund = refundMapper.selectById(id);
        if (refund == null) {
            return Result.error("申请不存在");
        }

        if (refund.getStatus() != 0) {
            return Result.error("该申请已处理");
        }

        refund.setStatus(1); // 商家同意
        refund.setMerchantReply(reply);
        refundMapper.updateById(refund);
        return Result.success("已同意退换货申请");
    }

    @PutMapping("/reject/{id}")
    @Operation(summary = "拒绝退换货")
    public Result<String> rejectRefund(@PathVariable Long id, @RequestParam String reply) {
        Refund refund = refundMapper.selectById(id);
        if (refund == null) {
            return Result.error("申请不存在");
        }

        if (refund.getStatus() != 0) {
            return Result.error("该申请已处理");
        }

        refund.setStatus(2); // 商家拒绝
        refund.setMerchantReply(reply);
        refundMapper.updateById(refund);
        return Result.success("已拒绝退换货申请");
    }

    @PutMapping("/complete/{id}")
    @Operation(summary = "完成退换货")
    public Result<String> completeRefund(@PathVariable Long id) {
        Refund refund = refundMapper.selectById(id);
        if (refund == null) {
            return Result.error("申请不存在");
        }

        if (refund.getStatus() != 1) {
            return Result.error("只有已同意的申请才能标记完成");
        }

        refund.setStatus(3); // 已完成
        refundMapper.updateById(refund);
        return Result.success("退换货已完成");
    }
}
