package com.fubukiss.rikky.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        int maxRetries = 3;
        int currentRetry = 0;
        
        while (currentRetry < maxRetries) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + apiKey.trim());
                headers.set("Accept", "application/json");

                // 创建请求体
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", "deepseek-chat");
                requestBody.put("temperature", 0.7);
                requestBody.put("max_tokens", 2000);
                
                // 创建消息列表
                List<Map<String, String>> messages = new ArrayList<>();
                
                // 添加系统消息
                Map<String, String> systemMessage = new HashMap<>();
                systemMessage.put("role", "system");
                systemMessage.put("content", "你是一个专业的用户行为分析专家，请根据用户的点餐历史数据，分析用户的饮食偏好和习惯，给出个性化的推荐建议。分析结果应该包含以下几个方面：\n" +
                    "1. 口味偏好分析：分析用户的口味偏好，包括辣度、忌口等\n" +
                    "2. 消费习惯分析：分析用户的消费水平、消费稳定性等\n" +
                    "3. 个性化推荐建议：根据用户的历史订单，推荐可能感兴趣的新菜品\n" +
                    "请确保分析结果详细且专业，每个部分都要有具体的分析数据支持。");
                messages.add(systemMessage);
                
                // 添加用户消息
                Map<String, String> userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", "请分析以下用户数据并给出建议：\n" + userHistoryJson);
                messages.add(userMessage);
                
                requestBody.put("messages", messages);

                log.info("发送DeepSeek API请求，第{}次尝试，URL: {}", currentRetry + 1, apiUrl);
                log.debug("请求头: {}", headers);
                log.debug("请求体: {}", requestBody);

                // 创建HTTP请求
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
                
                // 发送请求
                ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
                
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    log.debug("API响应: {}", responseBody);
                    
                    // 检查是否有错误信息
                    if (responseBody.containsKey("error")) {
                        Map<String, Object> error = (Map<String, Object>) responseBody.get("error");
                        String errorMessage = error.get("message").toString();
                        log.error("DeepSeek API返回错误：{}", errorMessage);
                        currentRetry++;
                        continue;
                    }
                    
                    if (responseBody.containsKey("choices") && !((List)responseBody.get("choices")).isEmpty()) {
                        Map<String, Object> choice = (Map<String, Object>)((List)responseBody.get("choices")).get(0);
                        if (choice.containsKey("message")) {
                            Map<String, String> message = (Map<String, String>)choice.get("message");
                            if (message.containsKey("content")) {
                                String content = message.get("content");
                                if (content != null && !content.trim().isEmpty()) {
                                    log.info("DeepSeek API调用成功，返回内容长度：{}", content.length());
                                    return content;
                                }
                            }
                        }
                    }
                } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                    log.error("DeepSeek API地址不存在，请检查配置：{}", apiUrl);
                    return null;
                }
                
                log.warn("DeepSeek API返回数据格式不正确或内容为空，准备重试");
                currentRetry++;
                if (currentRetry < maxRetries) {
                    Thread.sleep(1000 * currentRetry); // 递增延迟重试
                }
                
            } catch (HttpClientErrorException e) {
                if (e.getRawStatusCode() == 404) {
                    log.error("DeepSeek API地址不存在，请检查配置：{}", apiUrl);
                    return null;
                } else if (e.getRawStatusCode() == 401) {
                    log.error("DeepSeek API认证失败，请检查API密钥是否正确配置。错误详情：{}", e.getResponseBodyAsString());
                    return null;
                }
                log.error("DeepSeek API调用失败，第{}次尝试：{}", currentRetry + 1, e.getMessage());
                currentRetry++;
                if (currentRetry < maxRetries) {
                    try {
                        Thread.sleep(1000 * currentRetry); // 递增延迟重试
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("DeepSeek API调用失败，第{}次尝试：{}", currentRetry + 1, e.getMessage());
                currentRetry++;
                if (currentRetry < maxRetries) {
                    try {
                        Thread.sleep(1000 * currentRetry); // 递增延迟重试
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        log.error("DeepSeek API调用失败，已达到最大重试次数");
        return null;
    }
}
