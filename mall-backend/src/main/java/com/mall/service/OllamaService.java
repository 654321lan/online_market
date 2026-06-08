package com.mall.service;

import java.util.List;
import java.util.Map;

public interface OllamaService {
    
    String chat(String message, List<Map<String, String>> history);
    
    String chatWithContext(String message, String context);
    
    boolean isAvailable();
    
    String getCurrentModel();
}
