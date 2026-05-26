package com.mall.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.result.Result;
import com.mall.entity.MemberLevel;
import com.mall.entity.RechargeRecord;
import com.mall.entity.User;
import com.mall.mapper.MemberLevelMapper;
import com.mall.mapper.RechargeRecordMapper;
import com.mall.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端 - 会员接口
 */
@RestController
@RequestMapping("/api/member")
@Tag(name = "用户端-会员接口")
public class MemberController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MemberLevelMapper memberLevelMapper;

    @Autowired
    private RechargeRecordMapper rechargeRecordMapper;

    @GetMapping("/info/{userId}")
    @Operation(summary = "获取会员信息")
    public Result<Map<String, Object>> getMemberInfo(@PathVariable Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);

        // 获取当前等级信息
        MemberLevel currentLevel = memberLevelMapper.selectOne(
                new LambdaQueryWrapper<MemberLevel>()
                        .eq(MemberLevel::getLevel, user.getMemberLevel())
                        .eq(MemberLevel::getStatus, 1));

        // 获取下一等级信息
        MemberLevel nextLevel = memberLevelMapper.selectOne(
                new LambdaQueryWrapper<MemberLevel>()
                        .gt(MemberLevel::getLevel, user.getMemberLevel())
                        .eq(MemberLevel::getStatus, 1)
                        .orderByAsc(MemberLevel::getLevel)
                        .last("LIMIT 1"));

        // 所有等级列表
        List<MemberLevel> allLevels = memberLevelMapper.selectList(
                new LambdaQueryWrapper<MemberLevel>()
                        .eq(MemberLevel::getStatus, 1)
                        .orderByAsc(MemberLevel::getLevel));

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("currentLevel", currentLevel);
        result.put("nextLevel", nextLevel);
        result.put("allLevels", allLevels);

        return Result.success(result);
    }

    @PostMapping("/recharge")
    @Operation(summary = "模拟充值")
    @Transactional
    public Result<Map<String, Object>> recharge(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("充值金额必须大于0");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 更新余额和累计充值
        BigDecimal newBalance = (user.getBalance() == null ? BigDecimal.ZERO : user.getBalance()).add(amount);
        BigDecimal newTotalRecharge = (user.getTotalRecharge() == null ? BigDecimal.ZERO : user.getTotalRecharge()).add(amount);

        user.setBalance(newBalance);
        user.setTotalRecharge(newTotalRecharge);

        // 根据累计充值金额判断是否升级
        Integer oldLevel = user.getMemberLevel() == null ? 0 : user.getMemberLevel();
        MemberLevel newMemberLevel = memberLevelMapper.selectOne(
                new LambdaQueryWrapper<MemberLevel>()
                        .le(MemberLevel::getMinAmount, newTotalRecharge)
                        .eq(MemberLevel::getStatus, 1)
                        .orderByDesc(MemberLevel::getLevel)
                        .last("LIMIT 1"));

        boolean upgraded = false;
        if (newMemberLevel != null && newMemberLevel.getLevel() > oldLevel) {
            user.setMemberLevel(newMemberLevel.getLevel());
            upgraded = true;
        }

        userMapper.updateById(user);

        // 记录充值记录
        RechargeRecord record = new RechargeRecord();
        record.setUserId(userId);
        record.setAmount(amount);
        rechargeRecordMapper.insert(record);

        Map<String, Object> result = new HashMap<>();
        result.put("balance", newBalance);
        result.put("totalRecharge", newTotalRecharge);
        result.put("memberLevel", user.getMemberLevel());
        result.put("upgraded", upgraded);
        if (upgraded && newMemberLevel != null) {
            result.put("newLevelName", newMemberLevel.getName());
        }

        return Result.success(result);
    }

    @GetMapping("/recharge/records/{userId}")
    @Operation(summary = "充值记录")
    public Result<IPage<RechargeRecord>> getRechargeRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<RechargeRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<RechargeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RechargeRecord::getUserId, userId)
                .orderByDesc(RechargeRecord::getCreateTime);
        IPage<RechargeRecord> result = rechargeRecordMapper.selectPage(pageParam, wrapper);
        return Result.success(result);
    }

    @GetMapping("/levels")
    @Operation(summary = "获取所有会员等级")
    public Result<List<MemberLevel>> getAllLevels() {
        List<MemberLevel> levels = memberLevelMapper.selectList(
                new LambdaQueryWrapper<MemberLevel>()
                        .eq(MemberLevel::getStatus, 1)
                        .orderByAsc(MemberLevel::getLevel));
        return Result.success(levels);
    }

    @GetMapping("/discount/{userId}")
    @Operation(summary = "获取用户折扣信息")
    public Result<Map<String, Object>> getUserDiscount(@PathVariable Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Integer level = user.getMemberLevel() == null ? 0 : user.getMemberLevel();
        MemberLevel memberLevel = memberLevelMapper.selectOne(
                new LambdaQueryWrapper<MemberLevel>()
                        .eq(MemberLevel::getLevel, level)
                        .eq(MemberLevel::getStatus, 1));

        Map<String, Object> result = new HashMap<>();
        result.put("memberLevel", level);
        result.put("levelName", memberLevel != null ? memberLevel.getName() : "普通会员");
        result.put("discount", memberLevel != null ? memberLevel.getDiscount() : BigDecimal.ONE);
        return Result.success(result);
    }
}
