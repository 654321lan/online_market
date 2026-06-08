package com.mall.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ollama")
public class OllamaConfig {
    
    private String baseUrl = "http://localhost:11434";
    private String model = "qwen2.5:7b";
    private Integer timeout = 120000;
    private Integer maxTokens = 1000;
    private Double temperature = 0.7;
    private Boolean enabled = true;
    private Double autoReplyThreshold = 0.8;
    private Integer maxRetries = 3;
    private Integer retryDelay = 1000;
    private Integer cacheSize = 50;
    private Integer cacheTtlMinutes = 3;
}