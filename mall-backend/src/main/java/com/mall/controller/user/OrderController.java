package com.mall.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.result.Result;
import com.mall.entity.*;
import com.mall.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户端 - 订单接口
 */
@RestController
@RequestMapping("/api/order")
@Tag(name = "用户端-订单接口")
public class OrderController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MemberLevelMapper memberLevelMapper;

    @PostMapping("/create")
    @Operation(summary = "创建订单（按商家自动拆分）")
    @Transactional
    public Result<List<Order>> createOrder(@RequestBody Order order, @RequestParam String cartIds) {
        try {
            String[] cartIdArr = cartIds.split(",");

            // 1. 加载所有购物车项和对应商品，并验证
            List<Cart> cartItems = new ArrayList<>();
            Map<Long, Product> productMap = new HashMap<>();

            for (String cartIdStr : cartIdArr) {
                Long cartId = Long.parseLong(cartIdStr);
                Cart cart = cartMapper.selectById(cartId);
                if (cart == null) continue;

                Product product = productMapper.selectById(cart.getProductId());
                if (product == null || product.getStatus() == 0) {
                    return Result.error("商品 [" + (product != null ? product.getName() : cart.getProductId()) + "] 不存在或已下架");
                }
                if (product.getStock() < cart.getQuantity()) {
                    return Result.error("商品 [" + product.getName() + "] 库存不足");
                }

                cartItems.add(cart);
                productMap.put(cart.getProductId(), product);
            }

            if (cartItems.isEmpty()) {
                return Result.error("未选择有效商品");
            }

            // 获取用户会员折扣信息
            User user = userMapper.selectById(order.getUserId());
            Integer userMemberLevel = (user != null && user.getMemberLevel() != null) ? user.getMemberLevel() : 0;
            MemberLevel memberLevel = memberLevelMapper.selectOne(
                    new LambdaQueryWrapper<MemberLevel>()
                            .eq(MemberLevel::getLevel, userMemberLevel)
                            .eq(MemberLevel::getStatus, 1));
            BigDecimal discountRate = (memberLevel != null) ? memberLevel.getDiscount() : BigDecimal.ONE;

            // 2. 按商家ID分组
            Map<Long, List<Cart>> merchantCartMap = cartItems.stream()
                    .collect(Collectors.groupingBy(cart -> productMap.get(cart.getProductId()).getMerchantId()));

            // 3. 为每个商家创建独立订单
            List<Order> createdOrders = new ArrayList<>();
            long timestamp = System.currentTimeMillis();
            int seq = 1;

            for (Map.Entry<Long, List<Cart>> entry : merchantCartMap.entrySet()) {
                Long merchantId = entry.getKey();
                List<Cart> merchantCarts = entry.getValue();

                // 计算该商家订单的原价总金额
                BigDecimal originalAmount = BigDecimal.ZERO;
                for (Cart cart : merchantCarts) {
                    Product product = productMap.get(cart.getProductId());
                    originalAmount = originalAmount.add(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));
                }

                // 计算会员折扣后金额
                BigDecimal totalAmount = originalAmount.multiply(discountRate).setScale(2, java.math.RoundingMode.HALF_UP);
                BigDecimal discountAmount = originalAmount.subtract(totalAmount);

                // 创建订单
                Order newOrder = new Order();
                newOrder.setOrderNo("ORD" + timestamp + String.format("%03d", seq++));
                newOrder.setUserId(order.getUserId());
                newOrder.setMerchantId(merchantId);
                newOrder.setOriginalAmount(originalAmount);
                newOrder.setTotalAmount(totalAmount);
                newOrder.setMemberLevel(userMemberLevel);
                newOrder.setDiscount(discountRate);
                newOrder.setDiscountAmount(discountAmount);
                newOrder.setStatus(0); // 待支付
                newOrder.setReceiverName(order.getReceiverName());
                newOrder.setReceiverPhone(order.getReceiverPhone());
                newOrder.setReceiverAddress(order.getReceiverAddress());
                newOrder.setRemark(order.getRemark());
                orderMapper.insert(newOrder);

                // 创建订单明细、扣库存、删购物车
                for (Cart cart : merchantCarts) {
                    Product product = productMap.get(cart.getProductId());

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrderId(newOrder.getId());
                    orderItem.setProductId(product.getId());
                    orderItem.setProductName(product.getName());
                    orderItem.setProductImage(product.getImage());
                    orderItem.setPrice(product.getPrice());
                    orderItem.setQuantity(cart.getQuantity());
                    orderItem.setTotalAmount(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));
                    orderItemMapper.insert(orderItem);

                    // 扣减库存
                    product.setStock(product.getStock() - cart.getQuantity());
                    productMapper.updateById(product);

                    // 删除购物车记录
                    cartMapper.deleteById(cart.getId());
                }

                createdOrders.add(newOrder);
            }

            return Result.success(createdOrders);
        } catch (Exception e) {
            return Result.error("创建订单失败：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "订单列表")
    public Result<IPage<Order>> getOrderList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {

        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);

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

    @PostMapping("/pay/{id}")
    @Operation(summary = "支付订单（模拟）")
    public Result<String> payOrder(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }

        if (order.getStatus() != 0) {
            return Result.error("订单状态不正确");
        }

        // 更新订单状态为待发货
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 更新销量
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setSales(product.getSales() + item.getQuantity());
                productMapper.updateById(product);
            }
        }

        return Result.success("支付成功");
    }

    @PutMapping("/cancel/{id}")
    @Operation(summary = "取消订单")
    @Transactional
    public Result<String> cancelOrder(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }

        if (order.getStatus() != 0) {
            return Result.error("只能取消待支付订单");
        }

        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productMapper.updateById(product);
            }
        }

        // 更新订单状态
        order.setStatus(4); // 已取消
        orderMapper.updateById(order);
        return Result.success("订单已取消");
    }

    @PutMapping("/receive/{id}")
    @Operation(summary = "确认收货")
    public Result<String> receiveOrder(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }

        if (order.getStatus() != 2) {
            return Result.error("订单状态不正确");
        }

        order.setStatus(3); // 已完成
        order.setFinishTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return Result.success("确认收货成功");
    }
}

