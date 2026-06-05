package com.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.result.Result;
import com.mall.entity.Admin;
import com.mall.mapper.AdminMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端 - 认证接口
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员端-认证接口")
public class AdminAuthController {

    @Autowired
    private AdminMapper adminMapper;

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public Result<Admin> login(@RequestParam String username, @RequestParam String password) {
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, username)
                .eq(Admin::getPassword, password));

        if (admin == null) {
            return Result.error("用户名或密码错误");
        }

        if (admin.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        admin.setPassword(null);
        return Result.success(admin);
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public Result<String> updatePassword(@RequestParam Long id,
                                         @RequestParam String oldPassword,
                                         @RequestParam String newPassword) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            return Result.error("管理员不存在");
        }

        if (!admin.getPassword().equals(oldPassword)) {
            return Result.error("原密码错误");
        }

        admin.setPassword(newPassword);
        adminMapper.updateById(admin);
        return Result.success("密码修改成功");
    }
}

