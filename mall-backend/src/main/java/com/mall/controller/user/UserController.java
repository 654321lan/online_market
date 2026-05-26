package com.mall.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.result.Result;
import com.mall.entity.User;
import com.mall.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端 - 用户接口
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户端-用户接口")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<String> register(@RequestParam String username, @RequestParam String password) {
        // 检查用户名是否已存在
        User existUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (existUser != null) {
            return Result.error("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 实际项目应该加密
        user.setNickname(username);
        user.setStatus(1);
        userMapper.insert(user);

        return Result.success("注册成功");
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<User> login(@RequestParam String username, @RequestParam String password) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getPassword, password));

        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        // 清空密码
        user.setPassword(null);
        return Result.success(user);
    }

    @GetMapping("/info/{id}")
    @Operation(summary = "获取用户信息")
    public Result<User> getUserInfo(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户信息")
    public Result<String> updateUser(@RequestBody User user) {
        user.setPassword(null); // 不允许直接修改密码
        user.setUsername(null); // 不允许修改用户名
        userMapper.updateById(user);
        return Result.success("更新成功");
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public Result<String> updatePassword(@RequestParam Long id, 
                                         @RequestParam String oldPassword,
                                         @RequestParam String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        if (!user.getPassword().equals(oldPassword)) {
            return Result.error("原密码错误");
        }

        user.setPassword(newPassword);
        userMapper.updateById(user);
        return Result.success("密码修改成功");
    }
}

