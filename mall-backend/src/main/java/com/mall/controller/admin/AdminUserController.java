package com.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.result.Result;
import com.mall.entity.User;
import com.mall.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端 - 用户管理接口
 */
@RestController
@RequestMapping("/api/admin/user")
@Tag(name = "管理员端-用户管理")
public class AdminUserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/list")
    @Operation(summary = "用户列表")
    public Result<IPage<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword);
        }

        wrapper.orderByDesc(User::getCreateTime);
        IPage<User> result = userMapper.selectPage(pageParam, wrapper);

        // 清空密码
        result.getRecords().forEach(user -> user.setPassword(null));
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "用户详情")
    public Result<User> getUserDetail(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @PutMapping("/status/{id}")
    @Operation(summary = "启用/禁用用户")
    public Result<String> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success(status == 1 ? "启用成功" : "禁用成功");
    }
}

