package org.example.RepositoryDemo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIService {

    @Value("${ai.python.service.url:http://localhost:5000/api/chat}")
    private String pythonServiceUrl;
    
    @Value("${ai.python.deepseek.url:http://localhost:8000/api/deepseek}")
    private String pythonDeepSeekUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger logger = LogManager.getLogger(AIService.class);

    public ChatResponse processMessage(String userId, String message) {
        try {
            // 准备请求体
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("user_id", userId);
            requestBody.put("message", message);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            // 发送请求到Python服务
            ResponseEntity<String> response = restTemplate.exchange(
                    pythonServiceUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());

                ChatResponse chatResponse = new ChatResponse();
                chatResponse.setType(jsonResponse.get("type").asText());
                chatResponse.setContent(jsonResponse.get("content").asText());
                chatResponse.setTimestamp(jsonResponse.get("timestamp").asText());

                if (jsonResponse.has("messageCount")) {
                    chatResponse.setMessageCount(jsonResponse.get("messageCount").asInt());
                }

                return chatResponse;
            } else {
                return new ChatResponse("error", "AI服务暂时不可用");
            }

        } catch (Exception e) {
            logger.error("调用AI服务时发生错误: ", e);
            return new ChatResponse("error", "调用AI服务时发生错误: " + e.getMessage());
        }
    }
    
    public ChatResponse processDeepSeekPrompt(String prompt) {
        try {
            // 准备请求体
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("prompt", prompt);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            // 发送请求到Python服务的DeepSeek端点
            ResponseEntity<String> response = restTemplate.exchange(
                    pythonDeepSeekUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());

                ChatResponse chatResponse = new ChatResponse();
                chatResponse.setType(jsonResponse.get("type").asText());
                chatResponse.setContent(jsonResponse.get("content").asText());
                chatResponse.setTimestamp(jsonResponse.get("timestamp").asText());

                return chatResponse;
            } else {
                return new ChatResponse("error", "AI服务暂时不可用");
            }

        } catch (Exception e) {
            logger.error("调用DeepSeek服务时发生错误: ", e);
            return new ChatResponse("error", "调用DeepSeek服务时发生错误: " + e.getMessage());
        }
    }
}