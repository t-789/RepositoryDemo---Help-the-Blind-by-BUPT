package org.example.RepositoryDemo.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

public class ImageSaver {
    private static final Logger logger = LogManager.getLogger(ImageSaver.class);
    
    // 常量定义
    private static final int DEFAULT_SIZE_LIMIT = 2 * 1024 * 1024; // 2MB
    private static final int DESCRIPTION_SIZE_LIMIT = 10 * 1024 * 1024; // 10MB
    
    /**
     * 保存图片的通用方法
     * @param obj 图片对象，支持MultipartFile和Base64字符串
     * @param path 存储路径类型 (例如: "avatar", "description")
     * @return 访问路径，如果保存失败则返回null
     */
    public static String saveImage(Object obj, String path) {
        int limit = DEFAULT_SIZE_LIMIT;
        if (Objects.equals(path, "description")) {
            limit = DESCRIPTION_SIZE_LIMIT;
        }
        
        if (obj instanceof MultipartFile file) {
            return saveMultipartFile(file, limit, path);
        } else if (obj instanceof String base64) {
            return saveBase64Image(base64, limit, path);
        } else {
            throw new RuntimeException("Invalid object type");
        }
    }
    
    private static String saveMultipartFile(MultipartFile file, long limit, String type) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return null;
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                return null;
            }

            // 检查文件大小
            if (file.getSize() > limit) {
                logger.error("文件大小超过限制: {} bytes (limit: {} bytes)", file.getSize(), limit);
                return null;
            }

            // 创建图片存储目录
            String uploadDir = "./external/static/" + type + "/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    logger.fatal("saveMultipartFile(): 创建{}存储目录失败", type);
                    return null;
                }
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID() + extension;
            Path path = Paths.get(uploadDir);
            Path filePath = path.resolve(fileName).normalize();

            // 验证文件路径是否在允许的目录内
            Path allowedDir = path.toAbsolutePath().normalize();
            if (!filePath.toAbsolutePath().normalize().startsWith(allowedDir)) {
                logger.error("saveMultipartFile(): 非法文件路径访问尝试");
                return null;
            }
            
            // 保存文件
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 返回访问路径
            return "/" + type + "/" + fileName;
        } catch (IOException e) {
            logger.error("saveMultipartFile(): 保存文件失败: {}", e.getMessage());
            return null;
        }
    }
    
    private static String saveBase64Image(String base64Data, long limit, String type) {
        try {
            // 检查是否是DataURL格式 (data:image/png;base64,...)
            String base64Content = base64Data;
            String fileExtension = ".png"; // 默认扩展名

            if (base64Data.startsWith("data:")) {
                // 解析DataURL格式
                String[] parts = base64Data.split(",");
                if (parts.length != 2) {
                    logger.error("saveBase64Image(): 无效的DataURL格式");
                    return null;
                }

                // 获取MIME类型
                String mimeTypePart = parts[0];
                base64Content = parts[1];

                // 从MIME类型中提取文件扩展名
                if (mimeTypePart.contains("image/jpeg") || mimeTypePart.contains("image/jpg")) {
                    fileExtension = ".jpg";
                } else if (mimeTypePart.contains("image/png")) {
                    fileExtension = ".png";
                } else {
                    logger.warn("saveBase64Image(): 不支持的图片格式: {}", mimeTypePart);
                    return null;
                }
            }

            // 解码Base64数据
            byte[] decodedBytes = Base64.getDecoder().decode(base64Content);

            // 检查文件大小
            if (decodedBytes.length > limit) {
                logger.error("{}文件大小超过限制: {} bytes (limit: {} bytes)", type, decodedBytes.length, limit);
                return null;
            }

            // 创建图片存储目录
            String uploadDir = "./external/static/" + type + "/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                if(!dir.mkdirs()){
                    logger.fatal("saveBase64Image(): 创建{}存储目录失败", type);
                    return null;
                }
            }

            // 生成唯一文件名
            String fileName = UUID.randomUUID() + fileExtension;
            Path path = Paths.get(uploadDir);
            Path filePath = path.resolve(fileName).normalize();

            // 验证文件路径是否在允许的目录内
            Path allowedDir = path.toAbsolutePath().normalize();
            if (!filePath.toAbsolutePath().normalize().startsWith(allowedDir)) {
                logger.error("saveBase64Image(): 非法文件路径访问尝试");
                return null;
            }

            // 保存文件
            Files.write(filePath, decodedBytes);

            // 返回访问路径
            return "/" + type + "/" + fileName;
        } catch (IllegalArgumentException e) {
            logger.error("saveBase64Image(): Base64解码失败: {}", e.getMessage());
            return null;
        } catch (IOException e) {
            logger.error("saveBase64Image(): 保存{}文件失败: {}", type, e.getMessage());
            return null;
        }
    }
}