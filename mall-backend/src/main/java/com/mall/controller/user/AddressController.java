package com.mall.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.result.Result;
import com.mall.entity.Address;
import com.mall.mapper.AddressMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端 - 收货地址接口
 */
@RestController
@RequestMapping("/api/address")
@Tag(name = "用户端-地址接口")
public class AddressController {

    @Autowired
    private AddressMapper addressMapper;

    @PostMapping("/add")
    @Operation(summary = "添加收货地址")
    public Result<String> addAddress(@RequestBody Address address) {
        // 如果设置为默认地址，先取消其他默认地址
        if (address.getIsDefault() == 1) {
            addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                    .eq(Address::getUserId, address.getUserId())
                    .set(Address::getIsDefault, 0));
        }
        addressMapper.insert(address);
        return Result.success("添加成功");
    }

    @GetMapping("/list")
    @Operation(summary = "地址列表")
    public Result<List<Address>> getAddressList(@RequestParam Long userId) {
        List<Address> list = addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreateTime));
        return Result.success(list);
    }

    @PutMapping("/update")
    @Operation(summary = "更新地址")
    public Result<String> updateAddress(@RequestBody Address address) {
        // 如果设置为默认地址，先取消其他默认地址
        if (address.getIsDefault() == 1) {
            addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                    .eq(Address::getUserId, address.getUserId())
                    .set(Address::getIsDefault, 0));
        }
        addressMapper.updateById(address);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除地址")
    public Result<String> deleteAddress(@PathVariable Long id) {
        addressMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @PutMapping("/default/{id}")
    @Operation(summary = "设置默认地址")
    public Result<String> setDefaultAddress(@PathVariable Long id, @RequestParam Long userId) {
        // 取消其他默认地址
        addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0));

        // 设置当前地址为默认
        Address address = addressMapper.selectById(id);
        address.setIsDefault(1);
        addressMapper.updateById(address);
        return Result.success("设置成功");
    }
}

