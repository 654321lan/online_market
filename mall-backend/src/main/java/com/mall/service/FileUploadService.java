package com.mall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务
 */
public interface FileUploadService {
    
    /**
     * 上传文件
     */
    String uploadFile(MultipartFile file);
}

