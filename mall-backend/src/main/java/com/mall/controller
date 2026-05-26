package com.mall.controller;

import com.mall.common.result.Result;
import com.mall.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传接口
 */
@RestController
@RequestMapping("/api")
@Tag(name = "文件上传接口")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileUploadService.uploadFile(file);
            return Result.success(filePath);
        } catch (Exception e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
}

