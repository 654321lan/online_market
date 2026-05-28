package com.mall.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.result.Result;
import com.mall.entity.Merchant;
import com.mall.mapper.MerchantMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商家端 - 商家认证接口
 */
@RestController
@RequestMapping("/api/merchant")
@Tag(name = "商家端-认证接口")
public class MerchantAuthController {

    @Autowired
    private MerchantMapper merchantMapper;

    @PostMapping("/register")
    @Operation(summary = "商家注册")
    public Result<String> register(@RequestParam String username, 
                                   @RequestParam String password,
                                   @RequestParam String shopName) {
        // 检查用户名是否已存在
        Merchant existMerchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUsername, username));
        if (existMerchant != null) {
            return Result.error("商家账号已存在");
        }

        // 创建商家
        Merchant merchant = new Merchant();
        merchant.setUsername(username);
        merchant.setPassword(password);
        merchant.setShopName(shopName);
        merchant.setStatus(2); // 待审核
        merchantMapper.insert(merchant);

        return Result.success("注册成功，请等待管理员审核");
    }

    @PostMapping("/login")
    @Operation(summary = "商家登录")
    public Result<Merchant> login(@RequestParam String username, @RequestParam String password) {
        Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUsername, username)
                .eq(Merchant::getPassword, password));

        if (merchant == null) {
            return Result.error("用户名或密码错误");
        }

        if (merchant.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        if (merchant.getStatus() == 2) {
            return Result.error("账号正在审核中，请等待管理员审核通过后再登录");
        }

        merchant.setPassword(null);
        return Result.success(merchant);
    }

    @GetMapping("/info/{id}")
    @Operation(summary = "商家信息")
    public Result<Merchant> getMerchantInfo(@PathVariable Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant != null) {
            merchant.setPassword(null);
        }
        return Result.success(merchant);
    }

    @PutMapping("/update")
    @Operation(summary = "更新商家信息")
    public Result<String> updateMerchant(@RequestBody Merchant merchant) {
        merchant.setPassword(null);
        merchant.setUsername(null);
        merchantMapper.updateById(merchant);
        return Result.success("更新成功");
    }
}

