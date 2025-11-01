package org.example.RepositoryDemo.dto;

import lombok.Data;

@Data
public class DeepSeekRequest {
    private String prompt;

    // 默认构造函数
    public DeepSeekRequest() {}

    // 全参构造函数
    public DeepSeekRequest(String prompt) {
        this.prompt = prompt;
    }
}