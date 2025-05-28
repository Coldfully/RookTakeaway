package com.fubukiss.rikky.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DeepSeekClient {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DeepSeekClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String analyzeUserProfile(String userHistoryJson) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // 创建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            
            // 创建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加系统消息
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个专业的用户行为分析专家，请根据用户的点餐历史数据，分析用户的饮食偏好和习惯，给出个性化的推荐建议。分析结果应该包含以下几个方面：1. 口味偏好分析 2. 消费习惯分析 3. 个性化推荐建议");
            messages.add(systemMessage);
            
            // 添加用户消息
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", "请分析以下用户数据并给出建议：\n" + userHistoryJson);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);

            log.info("发送DeepSeek API请求，请求体：{}", requestBody);

            // 创建HTTP请求
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            try {
                // 发送请求并获取响应
                Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);
                log.info("DeepSeek API响应：{}", response);

                // 处理响应
                if (response == null) {
                    log.error("DeepSeek API返回空响应");
                    throw new RuntimeException("AI分析服务返回空响应");
                }

                if (response.containsKey("error")) {
                    Map<String, Object> error = (Map<String, Object>) response.get("error");
                    String errorMessage = error.get("message").toString();
                    String errorType = error.get("type").toString();
                    String errorCode = error.get("code").toString();
                    
                    log.error("DeepSeek API返回错误：类型={}，代码={}，消息={}", errorType, errorCode, errorMessage);
                    
                    if ("invalid_request_error".equals(errorCode) && "Insufficient Balance".equals(errorMessage)) {
                        throw new RuntimeException("AI分析服务余额不足，请充值后重试");
                    }
                    
                    throw new RuntimeException("AI分析服务返回错误：" + errorMessage);
                }

                if (!response.containsKey("choices")) {
                    log.error("DeepSeek API返回数据格式异常，缺少choices字段");
                    throw new RuntimeException("AI分析服务返回数据格式异常");
                }

                List<?> choices = (List<?>) response.get("choices");
                if (choices.isEmpty()) {
                    log.error("DeepSeek API返回空choices列表");
                    throw new RuntimeException("AI分析服务返回空结果");
                }

                Map<String, Object> choice = (Map<String, Object>) choices.get(0);
                if (!choice.containsKey("message")) {
                    log.error("DeepSeek API返回数据格式异常，缺少message字段");
                    throw new RuntimeException("AI分析服务返回数据格式异常");
                }

                Map<String, String> message = (Map<String, String>) choice.get("message");
                if (!message.containsKey("content")) {
                    log.error("DeepSeek API返回数据格式异常，缺少content字段");
                    throw new RuntimeException("AI分析服务返回数据格式异常");
                }

                String content = message.get("content");
                if (content == null || content.trim().isEmpty()) {
                    log.error("DeepSeek API返回空内容");
                    throw new RuntimeException("AI分析服务返回空内容");
                }

                return content;
            } catch (HttpClientErrorException e) {
                log.error("DeepSeek API调用失败，HTTP状态码：{}，响应内容：{}", e.getRawStatusCode(), e.getResponseBodyAsString());
                if (e.getRawStatusCode() == 402) {
                    throw new RuntimeException("AI分析服务余额不足，请充值后重试");
                }
                throw new RuntimeException("AI分析服务调用失败：" + e.getMessage());
            }
        } catch (Exception e) {
            log.error("调用DeepSeek API失败", e);
            throw new RuntimeException("AI分析服务调用失败：" + e.getMessage());
        }
    }
}
