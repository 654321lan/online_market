package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.entity.Merchant;
import com.mall.entity.Order;
import com.mall.entity.Product;
import com.mall.entity.ProductKnowledge;
import com.mall.entity.ProductSpec;
import com.mall.entity.ProductSpecValue;
import com.mall.entity.ProductSku;
import com.mall.mapper.MerchantMapper;
import com.mall.mapper.OrderMapper;
import com.mall.mapper.ProductMapper;
import com.mall.mapper.ProductKnowledgeMapper;
import com.mall.mapper.ProductSpecMapper;
import com.mall.mapper.ProductSpecValueMapper;
import com.mall.mapper.ProductSkuMapper;
import com.mall.service.AiCustomerService;
import com.mall.service.OllamaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiCustomerServiceImpl implements AiCustomerService {
    
    @Autowired
    private OllamaService ollamaService;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private ProductKnowledgeMapper productKnowledgeMapper;
    
    @Autowired
    private MerchantMapper merchantMapper;
    
    @Autowired
    private ProductSpecMapper productSpecMapper;
    
    @Autowired
    private ProductSpecValueMapper productSpecValueMapper;
    
    @Autowired
    private ProductSkuMapper productSkuMapper;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String generateReply(String userMessage, Long sessionId, Long productId, Long orderId) {
        try {
            if (!ollamaService.isAvailable()) {
                log.warn("Ollama服务不可用");
                return null;
            }
            
            String context = buildContext(productId, orderId);
            log.info("生成AI回复，用户问题: {}, 商品ID: {}, 订单ID: {}, 上下文长度: {}", 
                userMessage, productId, orderId, context.length());
            log.debug("上下文内容: {}", context);
            
            String reply = ollamaService.chatWithContext(userMessage, context);
            log.info("AI回复: {}", reply);
            
            return reply;
            
        } catch (Exception e) {
            log.error("AI reply generation error, 用户问题: {}, 商品ID: {}", userMessage, productId, e);
            return null;
        }
    }
    
    @Override
    public String generateProductReply(String userMessage, Long productId) {
        try {
            Product product = productMapper.selectById(productId);
            if (product == null) {
                return "商品信息不存在，请确认商品ID。";
            }
            
            String context = buildProductContext(product);
            return ollamaService.chatWithContext(userMessage, context);
            
        } catch (Exception e) {
            log.error("Product reply generation error", e);
            return null;
        }
    }
    
    @Override
    public String generateOrderReply(String userMessage, Long orderId) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                return "订单信息不存在，请确认订单ID。";
            }
            
            String context = buildOrderContext(order);
            return ollamaService.chatWithContext(userMessage, context);
            
        } catch (Exception e) {
            log.error("Order reply generation error", e);
            return null;
        }
    }
    
    @Override
    public boolean isAiAvailable() {
        return ollamaService.isAvailable();
    }
    
    @Override
    public String getCurrentModel() {
        return ollamaService.getCurrentModel();
    }
    
    private String buildContext(Long productId, Long orderId) {
        StringBuilder context = new StringBuilder();
        context.append("电商客服助手上下文信息：\n");
        
        if (productId != null) {
            Product product = productMapper.selectById(productId);
            if (product != null) {
                String productContext = buildProductContext(product);
                context.append(productContext).append("\n");
                log.info("构建商品上下文，商品ID: {}, 商品名称: {}, 描述长度: {}", 
                    productId, product.getName(), 
                    product.getDescription() != null ? product.getDescription().length() : 0);
            } else {
                log.warn("商品不存在，商品ID: {}", productId);
            }
        }
        
        if (orderId != null) {
            Order order = orderMapper.selectById(orderId);
            if (order != null) {
                context.append(buildOrderContext(order)).append("\n");
            }
        }
        
        return context.toString();
    }
    
    private String buildProductContext(Product product) {
        StringBuilder context = new StringBuilder();
        
        context.append("【商品基本信息】\n");
        context.append(String.format("商品名称：%s\n", product.getName()));
        context.append(String.format("商品价格：%.2f元\n", product.getPrice()));
        context.append(String.format("商品库存：%d件\n", product.getStock()));
        context.append(String.format("商品销量：%d件\n", product.getSales()));
        
        String description = product.getDescription() != null ? product.getDescription() : "暂无描述";
        context.append(String.format("商品描述：%s\n", description));
        
        if (product.getDetailDescription() != null && !product.getDetailDescription().trim().isEmpty()) {
            context.append(String.format("详细描述：%s\n", product.getDetailDescription()));
        }
        
        context.append("\n【商品规格信息】\n");
        
        try {
            List<ProductSpec> specs = productSpecMapper.selectList(
                new LambdaQueryWrapper<ProductSpec>()
                    .eq(ProductSpec::getProductId, product.getId())
                    .orderByAsc(ProductSpec::getSort)
            );
            
            for (ProductSpec spec : specs) {
                List<ProductSpecValue> values = productSpecValueMapper.selectList(
                    new LambdaQueryWrapper<ProductSpecValue>()
                        .eq(ProductSpecValue::getSpecId, spec.getId())
                        .orderByAsc(ProductSpecValue::getSort)
                );
                
                if (!values.isEmpty()) {
                    String valueList = values.stream()
                        .map(ProductSpecValue::getSpecValue)
                        .collect(Collectors.joining("、"));
                    context.append(String.format("%s：%s\n", spec.getSpecName(), valueList));
                }
            }
        } catch (Exception e) {
            log.error("查询商品规格信息失败，productId: {}", product.getId(), e);
            context.append("暂无规格信息\n");
        }
        
        if (product.getHasSku() != null && product.getHasSku()) {
            context.append("\n【商品SKU信息】\n");
            
            try {
                List<ProductSku> skus = productSkuMapper.selectList(
                    new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, product.getId())
                        .eq(ProductSku::getStatus, 1)
                );
                
                for (ProductSku sku : skus) {
                    context.append(String.format("- 规格：%s，价格：%.2f元，库存：%d件\n", 
                        sku.getSpecs(), sku.getPrice(), sku.getStock()));
                }
            } catch (Exception e) {
                log.error("查询商品SKU信息失败，productId: {}", product.getId(), e);
                context.append("暂无SKU信息\n");
            }
        }
        
        context.append("\n注意：请严格基于上述商品信息回答用户问题。如果商品信息中没有相关内容，请明确告知用户\"商品信息中没有相关说明\"。");
        
        return context.toString();
    }
    
    private String buildOrderContext(Order order) {
        return String.format("""
            订单信息：
            - 订单号：%s
            - 订单金额：%.2f元
            - 订单状态：%s
            - 收货人：%s
            - 收货电话：%s
            - 收货地址：%s
            """, 
            order.getOrderNo(), 
            order.getTotalAmount(), 
            getOrderStatusText(order.getStatus()),
            order.getReceiverName(),
            order.getReceiverPhone(),
            order.getReceiverAddress()
        );
    }
    
    private String getOrderStatusText(Integer status) {
        switch (status) {
            case 0: return "待支付";
            case 1: return "待发货";
            case 2: return "待收货";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知状态";
        }
    }
    
    @Override
    public String generateKnowledgeBasedReply(String userMessage, Long productId, String knowledgeType) {
        try {
            if (!ollamaService.isAvailable()) {
                return null;
            }
            
            LambdaQueryWrapper<ProductKnowledge> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductKnowledge::getProductId, productId)
                   .eq(ProductKnowledge::getKnowledgeType, knowledgeType)
                   .eq(ProductKnowledge::getStatus, 1)
                   .orderByDesc(ProductKnowledge::getPriority)
                   .last("LIMIT 1");
            
            ProductKnowledge knowledge = productKnowledgeMapper.selectOne(wrapper);
            if (knowledge == null) {
                return null;
            }
            
            String knowledgeContent = knowledge.getKnowledgeContent();
            String context = buildKnowledgeContext(knowledgeContent, knowledgeType);
            
            return ollamaService.chatWithContext(userMessage, context);
            
        } catch (Exception e) {
            log.error("Knowledge based reply generation error", e);
            return null;
        }
    }
    
    @Override
    public boolean canAiAnswer(String userMessage, Long productId) {
        try {
            String knowledgeType = analyzeQuestionType(userMessage);
            if (knowledgeType == null) {
                return false;
            }
            
            LambdaQueryWrapper<ProductKnowledge> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductKnowledge::getProductId, productId)
                   .eq(ProductKnowledge::getKnowledgeType, knowledgeType)
                   .eq(ProductKnowledge::getStatus, 1);
            
            return productKnowledgeMapper.selectCount(wrapper) > 0;
            
        } catch (Exception e) {
            log.error("AI answer capability check error", e);
            return false;
        }
    }
    
    private String analyzeQuestionType(String userMessage) {
        String message = userMessage.toLowerCase();
        
        if (message.contains("尺码") || message.contains("大小") || message.contains("身高") || message.contains("体重")) {
            return "size";
        }
        if (message.contains("材质") || message.contains("面料") || message.contains("材料")) {
            return "material";
        }
        if (message.contains("发货") || message.contains("快递") || message.contains("物流") || message.contains("配送")) {
            return "shipping";
        }
        if (message.contains("退货") || message.contains("换货") || message.contains("退款") || message.contains("售后")) {
            return "policy";
        }
        
        return null;
    }
    
    private String buildKnowledgeContext(String knowledgeContent, String knowledgeType) {
        try {
            JsonNode jsonNode = objectMapper.readTree(knowledgeContent);
            
            StringBuilder context = new StringBuilder();
            context.append("商品").append(getKnowledgeTypeName(knowledgeType)).append("信息：\n");
            
            jsonNode.fields().forEachRemaining(entry -> {
                context.append("- ").append(entry.getKey()).append(": ").append(entry.getValue().asText()).append("\n");
            });
            
            return context.toString();
            
        } catch (Exception e) {
            log.error("Build knowledge context error", e);
            return "商品相关信息：" + knowledgeContent;
        }
    }
    
    private String getKnowledgeTypeName(String knowledgeType) {
        switch (knowledgeType) {
            case "size": return "尺码";
            case "material": return "材质";
            case "shipping": return "发货";
            case "policy": return "政策";
            default: return "信息";
        }
    }
    
    @Override
    public boolean shouldUseAiReply(Long merchantId, String userMessage, Long productId, Integer currentAiReplies) {
        try {
            Merchant merchant = merchantMapper.selectById(merchantId);
            if (merchant == null) {
                log.warn("商家不存在，merchantId: {}", merchantId);
                return false;
            }
            
            if (merchant.getAiAutoReplyEnabled() == null || merchant.getAiAutoReplyEnabled() != 1) {
                log.warn("商家未启用AI自动回复，merchantId: {}, aiAutoReplyEnabled: {}", 
                    merchantId, merchant.getAiAutoReplyEnabled());
                return false;
            }
            
            if (!isAiAvailable()) {
                log.warn("AI服务不可用，merchantId: {}", merchantId);
                return false;
            }
            
            if (shouldTransferToHuman(userMessage, currentAiReplies)) {
                log.warn("需要转人工，merchantId: {}, currentAiReplies: {}", merchantId, currentAiReplies);
                return false;
            }
            
            boolean merchantOnline = isMerchantOnline(merchantId);
            boolean merchantWorkHours = isMerchantWorkHours(merchantId);
            
            log.info("AI回复判断，merchantId: {}, 商家在线: {}, 工作时间: {}, 商品ID: {}", 
                merchantId, merchantOnline, merchantWorkHours, productId);
            
            log.info("AI回复条件满足，允许AI回复，merchantId: {}", merchantId);
            return true;
            
        } catch (Exception e) {
            log.error("Check AI reply capability error, merchantId: {}", merchantId, e);
            return false;
        }
    }
    
    @Override
    public boolean isMerchantOnline(Long merchantId) {
        try {
            Merchant merchant = merchantMapper.selectById(merchantId);
            return merchant != null && merchant.getOnlineStatus() != null && merchant.getOnlineStatus() == 1;
        } catch (Exception e) {
            log.error("Check merchant online status error", e);
            return false;
        }
    }
    
    @Override
    public boolean isMerchantWorkHours(Long merchantId) {
        try {
            Merchant merchant = merchantMapper.selectById(merchantId);
            if (merchant == null || merchant.getWorkHoursStart() == null || merchant.getWorkHoursEnd() == null) {
                return true;
            }
            
            java.time.LocalTime now = java.time.LocalTime.now();
            java.time.LocalTime startTime = java.time.LocalTime.parse(merchant.getWorkHoursStart());
            java.time.LocalTime endTime = java.time.LocalTime.parse(merchant.getWorkHoursEnd());
            
            return !now.isBefore(startTime) && !now.isAfter(endTime);
            
        } catch (Exception e) {
            log.error("Check merchant work hours error", e);
            return true;
        }
    }
    
    @Override
    public boolean shouldTransferToHuman(String userMessage, Integer aiReplies) {
        if (aiReplies != null && aiReplies >= 3) {
            return true;
        }
        
        String message = userMessage.toLowerCase();
        String[] transferKeywords = {
            "人工", "客服", "转人工", "不满足", "不满意", "解决不了", 
            "听不懂", "说人话", "复杂", "投诉", "退款", "退货"
        };
        
        for (String keyword : transferKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean shouldTransferToHuman(Long merchantId, String userMessage, Integer aiReplies) {
        // 商家离线时不支持转人工
        if (!isMerchantOnline(merchantId)) {
            return false;
        }
        
        return shouldTransferToHuman(userMessage, aiReplies);
    }
    
    @Override
    public String getQuickReply(String userMessage, Long productId) {
        String message = userMessage.toLowerCase();
        
        if (message.contains("价格") || message.contains("多少钱")) {
            if (productId != null) {
                Product product = productMapper.selectById(productId);
                if (product != null) {
                    return "该商品价格为：" + product.getPrice() + "元";
                }
            }
            return "您好，关于商品价格，请查看商品详情页面的价格信息。";
        }
        
        if (message.contains("库存") || message.contains("有货")) {
            if (productId != null) {
                Product product = productMapper.selectById(productId);
                if (product != null) {
                    return product.getStock() > 0 ? "该商品目前有货，库存：" + product.getStock() + "件" : "该商品暂时缺货，请稍后再来查看。";
                }
            }
            return "您好，关于库存情况，请查看商品详情页面的库存信息。";
        }
        
        if (message.contains("发货") || message.contains("快递")) {
            return "我们一般在下单后24-48小时内发货，发货后会通过短信通知您快递单号。";
        }
        
        if (message.contains("退货") || message.contains("退款")) {
            return "关于退货退款，您可以在订单详情页面申请。收到商品后7天内可申请退货，15天内可申请退款。";
        }
        
        if (message.contains("你好") || message.contains("在吗")) {
            return "您好！我是智能客服助手，有什么可以帮您的吗？";
        }
        
        return null;
    }
}