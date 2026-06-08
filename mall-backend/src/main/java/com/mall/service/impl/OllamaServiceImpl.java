package com.mall.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.config.OllamaConfig;
import com.mall.service.OllamaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class OllamaServiceImpl implements OllamaService {
    
    @Autowired
    private OllamaConfig ollamaConfig;
    
    private WebClient webClient;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final Object lock = new Object();
    private int timeoutMs;
    
    private Semaphore requestSemaphore;
    private final Map<String, CacheEntry> responseCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cacheCleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicInteger cacheSize = new AtomicInteger(0);
    
    private static class CacheEntry {
        String content;
        long timestamp;
        
        CacheEntry(String content) {
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    @PostConstruct
    public void init() {
        log.info("开始初始化Ollama服务...");
        log.info("Ollama配置 - baseUrl: {}, model: {}, timeout: {}, maxTokens: {}", 
            ollamaConfig.getBaseUrl(), ollamaConfig.getModel(), ollamaConfig.getTimeout(), ollamaConfig.getMaxTokens());
        
        int timeoutMs = ollamaConfig.getTimeout() != null ? ollamaConfig.getTimeout() : 180000;
        log.info("实际使用的超时时间: {}ms", timeoutMs);
        
        ConnectionProvider provider = ConnectionProvider.builder("ollama-connection-pool")
                .maxConnections(3)
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofMinutes(5))
                .pendingAcquireTimeout(Duration.ofSeconds(90))
                .evictInBackground(Duration.ofSeconds(120))
                .build();
        
        HttpClient httpClient = HttpClient.create(provider)
                .responseTimeout(Duration.ofMillis(timeoutMs))
                .doOnConnected(conn -> 
                    conn.addHandlerLast(new io.netty.handler.timeout.ReadTimeoutHandler(
                        timeoutMs / 1000, TimeUnit.SECONDS))
                    .addHandlerLast(new io.netty.handler.timeout.WriteTimeoutHandler(
                        timeoutMs / 1000, TimeUnit.SECONDS))
                );
        
        this.webClient = WebClient.builder()
                .baseUrl(ollamaConfig.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        log.info("Ollama服务初始化完成，基础URL: {}, 超时: {}ms", ollamaConfig.getBaseUrl(), timeoutMs);
    }
    
    @Override
    public String chat(String message, List<Map<String, String>> history) {
        int maxRetries = ollamaConfig.getMaxRetries() != null ? ollamaConfig.getMaxRetries() : 3;
        int retryDelay = ollamaConfig.getRetryDelay() != null ? ollamaConfig.getRetryDelay() : 1000;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("model", ollamaConfig.getModel());
                request.put("stream", false);
                
                List<Map<String, String>> messages = new ArrayList<>();
                messages.add(Map.of("role", "system", "content", getSystemPrompt()));
                
                if (history != null && !history.isEmpty()) {
                    messages.addAll(history);
                }
                
                messages.add(Map.of("role", "user", "content", message));
                request.put("messages", messages);
                
                Map<String, Object> options = new HashMap<>();
                options.put("temperature", ollamaConfig.getTemperature());
                options.put("num_predict", ollamaConfig.getMaxTokens());
                request.put("options", options);
                
                String response = webClient.post()
                        .uri("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(objectMapper.writeValueAsString(request))
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(Duration.ofMillis(timeoutMs))
                        .block();
                
                if (response == null || response.isEmpty()) {
                    log.warn("Ollama返回空响应，尝试: {}/{}", attempt, maxRetries);
                    if (attempt < maxRetries) {
                        Thread.sleep(retryDelay);
                        continue;
                    }
                    return "抱歉，我现在无法回答您的问题，请稍后再试。";
                }
                
                JsonNode jsonNode = objectMapper.readTree(response);
                String content = jsonNode.path("message").path("content").asText();
                
                if (content == null || content.trim().isEmpty()) {
                    log.warn("Ollama返回空内容，尝试: {}/{}", attempt, maxRetries);
                    if (attempt < maxRetries) {
                        Thread.sleep(retryDelay);
                        continue;
                    }
                    return "抱歉，我现在无法回答您的问题，请稍后再试。";
                }
                
                log.info("Ollama回复成功，尝试: {}/{}", attempt, maxRetries);
                return content;
                
            } catch (Exception e) {
                log.error("Ollama chat error, 尝试: {}/{}, 错误: {}", attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    return "抱歉，我现在无法回答您的问题，请稍后再试。";
                }
            }
        }
        return "抱歉，我现在无法回答您的问题，请稍后再试。";
    }
    
    @Override
    public String chatWithContext(String message, String context) {
        int maxRetries = ollamaConfig.getMaxRetries() != null ? ollamaConfig.getMaxRetries() : 3;
        int retryDelay = ollamaConfig.getRetryDelay() != null ? ollamaConfig.getRetryDelay() : 1000;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String fullMessage = context + "\n\n用户问题：" + message;
            
                Map<String, Object> request = new HashMap<>();
                request.put("model", ollamaConfig.getModel());
                request.put("stream", false);
            
                List<Map<String, String>> messages = new ArrayList<>();
                messages.add(Map.of("role", "system", "content", getSystemPrompt()));
                messages.add(Map.of("role", "user", "content", fullMessage));
                request.put("messages", messages);
            
                Map<String, Object> options = new HashMap<>();
                options.put("temperature", ollamaConfig.getTemperature());
                options.put("num_predict", ollamaConfig.getMaxTokens());
                request.put("options", options);
            
                String response = webClient.post()
                        .uri("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(objectMapper.writeValueAsString(request))
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(Duration.ofMillis(timeoutMs))
                        .block();
            
                if (response == null || response.isEmpty()) {
                    log.warn("Ollama返回空响应（带上下文），尝试: {}/{}", attempt, maxRetries);
                    if (attempt < maxRetries) {
                        Thread.sleep(retryDelay);
                        continue;
                    }
                    return "抱歉，我现在无法回答您的问题，请稍后再试。";
                }
            
                JsonNode jsonNode = objectMapper.readTree(response);
                String content = jsonNode.path("message").path("content").asText();
                
                if (content == null || content.trim().isEmpty()) {
                    log.warn("Ollama返回空内容（带上下文），尝试: {}/{}", attempt, maxRetries);
                    if (attempt < maxRetries) {
                        Thread.sleep(retryDelay);
                        continue;
                    }
                    return "抱歉，我现在无法回答您的问题，请稍后再试。";
                }
                
                log.info("Ollama回复成功（带上下文），尝试: {}/{}", attempt, maxRetries);
                return content;
                
            } catch (Exception e) {
                log.error("Ollama chat with context error, 尝试: {}/{}, 错误: {}", attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    return "抱歉，我现在无法回答您的问题，请稍后再试。";
                }
            }
        }
        return "抱歉，我现在无法回答您的问题，请稍后再试。";
    }

    @Override
    public boolean isAvailable() {
        try {
            String response = webClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();
            
            if (response == null || response.isEmpty()) {
                log.warn("Ollama服务返回空响应");
                return false;
            }
            
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode models = jsonNode.path("models");
            
            if (!models.isArray() || models.size() == 0) {
                log.warn("Ollama服务没有可用模型");
                return false;
            }
            
            boolean modelExists = false;
            for (JsonNode model : models) {
                String modelName = model.path("name").asText();
                if (modelName.startsWith(ollamaConfig.getModel().split(":")[0])) {
                    modelExists = true;
                    log.info("Ollama服务可用，模型: {}", modelName);
                    break;
                }
            }
            
            if (!modelExists) {
                log.warn("Ollama服务未找到指定模型: {}", ollamaConfig.getModel());
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.warn("Ollama服务不可用: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getCurrentModel() {
        return ollamaConfig.getModel();
    }
    
    private String getSystemPrompt() {
        return """
            你是一个专业的电商客服助手，负责回答用户关于商品、订单、物流等问题。

            重要规则：
            1. 必须基于提供的商品信息回答问题
            2. 如果商品信息中没有相关内容，明确告知用户"商品信息中没有相关说明"
            3. 不要编造或猜测商品信息
            4. 回答要简洁明了，友好专业
            5. 如果遇到无法回答的问题，建议用户联系人工客服

            请根据下方提供的商品信息，准确回答用户的问题。
            """;
    }
}