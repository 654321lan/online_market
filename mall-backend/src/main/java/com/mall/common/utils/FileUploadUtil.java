package com.mall.common.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传工具类
 */
@Component
public class FileUploadUtil {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 文件访问路径
     */
    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 获取文件扩展名
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 生成新文件名
        String newFileName = UUID.randomUUID().toString() + extension;

        // 创建上传目录
        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        // 获取绝对路径并确保以分隔符结尾
        String absolutePath = uploadFolder.getAbsolutePath();
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath += File.separator;
        }

        // 保存文件
        File destFile = new File(absolutePath + newFileName);
        file.transferTo(destFile);
        
        System.out.println("文件保存成功：" + destFile.getAbsolutePath());

        // 返回访问路径
        return "/uploads/" + newFileName;
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        // 从URL中提取文件名
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        
        // 获取上传目录的绝对路径
        File uploadFolder = new File(uploadDir);
        String absolutePath = uploadFolder.getAbsolutePath();
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath += File.separator;
        }
        
        File file = new File(absolutePath + fileName);

        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
}

