package org.example.RepositoryDemo;

import org.example.RepositoryDemo.dto.DeepSeekRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return aiService.processMessage(request.getUserId(), request.getMessage());
    }

    // 简单的健康检查接口
    @GetMapping("/health")
    public String health() {
        return "AI Service is running";
    }

    // 测试关键词功能的接口
    @GetMapping("/test")
    public ChatResponse testKeyword(@RequestParam String message) {
        return aiService.processMessage("test_user", message);
    }

    // POST方式测试关键词功能的接口
    @PostMapping("/test")
    public ChatResponse testKeywordPost(@RequestParam String message) {
        return aiService.processMessage("test_user", message);
    }
    
    // 直接与DeepSeek通信的接口
    @PostMapping("/deepseek")
    public ChatResponse deepseekChat(@RequestBody DeepSeekRequest request) {
        return aiService.processDeepSeekPrompt(request.getPrompt());
    }
}