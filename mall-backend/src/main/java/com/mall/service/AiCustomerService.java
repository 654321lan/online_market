package com.mall.service;

import java.util.Map;

public interface AiCustomerService {
    
    String generateReply(String userMessage, Long sessionId, Long productId, Long orderId);
    
    String generateProductReply(String userMessage, Long productId);
    
    String generateOrderReply(String userMessage, Long orderId);
    
    String generateKnowledgeBasedReply(String userMessage, Long productId, String knowledgeType);
    
    boolean canAiAnswer(String userMessage, Long productId);
    
    boolean isAiAvailable();
    
    String getCurrentModel();
    
    boolean shouldUseAiReply(Long merchantId, String userMessage, Long productId, Integer currentAiReplies);
    
    boolean isMerchantOnline(Long merchantId);
    
    boolean isMerchantWorkHours(Long merchantId);
    
    boolean shouldTransferToHuman(String userMessage, Integer aiReplies);
    
    boolean shouldTransferToHuman(Long merchantId, String userMessage, Integer aiReplies);
    
    String getQuickReply(String userMessage, Long productId);
}