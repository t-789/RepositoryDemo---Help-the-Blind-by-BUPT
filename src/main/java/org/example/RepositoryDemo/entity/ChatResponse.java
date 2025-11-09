package org.example.RepositoryDemo.entity;

import lombok.Data;

@Data
public class ChatResponse {
    private String type;  // "keyword", "ai", "error"
    private String content;
    private String timestamp;
    private Integer messageCount;

    public ChatResponse() {}

    public ChatResponse(String type, String content) {
        this.type = type;
        this.content = content;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }
}