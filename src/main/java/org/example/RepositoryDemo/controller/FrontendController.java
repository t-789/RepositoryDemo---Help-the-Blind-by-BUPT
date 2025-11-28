package org.example.RepositoryDemo.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FrontendController {

    // 处理 /front/{name} 请求，自动映射到 external/templates/{name}.html
    @GetMapping("/front/{name}")
    public ResponseEntity<String> serveFrontendPage(@PathVariable String name) throws IOException {
        // 构建文件路径
        String filePath = "./external/templates/" + name + ".html";
        Path path = Paths.get(filePath);
        
        // 检查文件是否存在
        if (Files.exists(path) && Files.isReadable(path)) {
            // 读取文件内容
            String content = new String(Files.readAllBytes(path));
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(content);
        } else {
            // 文件不存在，返回404
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/front/math/{name}")
    public ResponseEntity<String> serveFrontendMath(@PathVariable String name) throws IOException {
        // 构建文件路径
        String filePath = "./external/templates/math/" + name + ".html";
        Path path = Paths.get(filePath);

        // 检查文件是否存在
        if (Files.exists(path) && Files.isReadable(path)) {
            // 读取文件内容
            String content = new String(Files.readAllBytes(path));
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(content);
        } else {
            // 文件不存在，返回404
            return ResponseEntity.notFound().build();
        }
    }
    
    // 处理 /front/res/{resourcePath} 请求，用于提供静态资源文件
    @GetMapping("/front/res/**")
    public ResponseEntity<byte[]> serveFrontendResource(HttpServletRequest request) throws IOException {
        // 获取完整的请求路径
        String fullPath = request.getRequestURI();
        
        // 提取资源路径部分（去掉 /front/res/ 前缀）
        String resourcePath = fullPath.substring("/front/res/".length());
        
        // 构建文件路径
        String filePath = "./external/templates/res/" + resourcePath;
        Path path = Paths.get(filePath);
        
        // 检查文件是否存在
        if (Files.exists(path) && Files.isReadable(path)) {
            // 读取文件内容
            byte[] content = Files.readAllBytes(path);
            
            // 根据文件扩展名设置正确的Content-Type
            String contentType = getContentType(resourcePath);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(content);
        } else {
            // 文件不存在，返回404
            return ResponseEntity.notFound().build();
        }
    }
    
    // 根据文件扩展名返回对应的Content-Type
    private String getContentType(String fileName) {
        if (fileName.endsWith(".css")) {
            return "text/css";
        } else if (fileName.endsWith(".js")) {
            return "application/javascript";
        } else if (fileName.endsWith(".json")) {
            return "application/json";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (fileName.endsWith(".ico")) {
            return "image/x-icon";
        } else if (fileName.endsWith(".woff")) {
            return "font/woff";
        } else if (fileName.endsWith(".woff2")) {
            return "font/woff2";
        } else if (fileName.endsWith(".ttf")) {
            return "font/ttf";
        } else {
            return "application/octet-stream";
        }
    }
}