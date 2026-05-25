package com.mall.service.impl;

import com.mall.common.utils.FileUploadUtil;
import com.mall.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务实现
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            return fileUploadUtil.uploadFile(file);
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }
}

