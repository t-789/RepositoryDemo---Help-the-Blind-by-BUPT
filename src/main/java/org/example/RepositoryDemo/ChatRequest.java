package org.example.RepositoryDemo;
import lombok.Data;

@Data
public class ChatRequest {
    private String userId;
    private String message;

    // 默认构造函数
    public ChatRequest() {}

    // 全参构造函数
    public ChatRequest(String userId, String message) {
        this.userId = userId;
        this.message = message;
    }
}