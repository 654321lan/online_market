package com.mall.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.result.Result;
import com.mall.entity.Cart;
import com.mall.entity.Product;
import com.mall.mapper.CartMapper;
import com.mall.mapper.ProductMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端 - 购物车接口
 */
@RestController
@RequestMapping("/api/cart")
@Tag(name = "用户端-购物车接口")
public class CartController {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @PostMapping("/add")
    @Operation(summary = "添加商品到购物车")
    public Result<String> addToCart(@RequestParam Long userId,
                                     @RequestParam Long productId,
                                     @RequestParam Integer quantity) {
        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == 0) {
            return Result.error("商品不存在或已下架");
        }

        // 检查购物车中是否已存在该商品
        Cart existCart = cartMapper.selectOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId));

        if (existCart != null) {
            // 更新数量
            existCart.setQuantity(existCart.getQuantity() + quantity);
            cartMapper.updateById(existCart);
        } else {
            // 新增购物车记录
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cartMapper.insert(cart);
        }

        return Result.success("添加成功");
    }

    @GetMapping("/list")
    @Operation(summary = "购物车列表")
    public Result<List<Cart>> getCartList(@RequestParam Long userId) {
        List<Cart> list = cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreateTime));
        return Result.success(list);
    }

    @PutMapping("/update")
    @Operation(summary = "更新购物车商品数量")
    public Result<String> updateCart(@RequestParam Long id, @RequestParam Integer quantity) {
        Cart cart = cartMapper.selectById(id);
        if (cart == null) {
            return Result.error("购物车记录不存在");
        }
        cart.setQuantity(quantity);
        cartMapper.updateById(cart);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除购物车商品")
    public Result<String> deleteCart(@PathVariable Long id) {
        cartMapper.deleteById(id);
        return Result.success("删除成功");
    }
}

