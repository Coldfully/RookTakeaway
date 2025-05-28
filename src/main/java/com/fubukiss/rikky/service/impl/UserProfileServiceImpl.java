package com.fubukiss.rikky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fubukiss.rikky.entity.OrderDetail;
import com.fubukiss.rikky.entity.UserProfile;
import com.fubukiss.rikky.mapper.UserProfileMapper;
import com.fubukiss.rikky.service.OrderDetailService;
import com.fubukiss.rikky.service.UserProfileService;
import com.fubukiss.rikky.util.DeepSeekClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService {

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private DeepSeekClient deepSeekClient;

    @Override
    public void generateUserProfile(Long id) {
        log.info("开始生成用户{}的画像", id);
        
        try {
            // 1. 获取用户历史订单数据
            List<OrderDetail> orderDetails = orderDetailService.getUserOrderDetails(id);
            log.info("获取到用户{}的订单数量：{}", id, orderDetails.size());
            
            if (orderDetails.isEmpty()) {
                log.warn("用户{}没有订单数据，无法生成画像", id);
                throw new RuntimeException("该用户暂无订单数据，请先下单后再生成画像");
            }
            
            // 2. 分析用户偏好
            Map<String, Long> dishFrequency = orderDetails.stream()
                    .collect(Collectors.groupingBy(OrderDetail::getName, Collectors.counting()));
            
            if (dishFrequency.isEmpty()) {
                log.warn("用户{}的订单中没有有效的菜品数据", id);
                throw new RuntimeException("用户订单中没有有效的菜品数据");
            }
            
            // 3. 构建用户数据JSON
            String userHistoryJson = buildUserHistoryJson(id, orderDetails, dishFrequency);
            log.info("构建的用户历史数据JSON：{}", userHistoryJson);
            
            // 4. 调用DeepSeek API进行分析
            String aiAnalysis = null;
            try {
                aiAnalysis = deepSeekClient.analyzeUserProfile(userHistoryJson);
            } catch (Exception e) {
                log.error("AI分析服务调用失败，使用基础分析：{}", e.getMessage());
                // 使用基础分析作为备选方案
                aiAnalysis = generateBasicAnalysis(orderDetails, dishFrequency);
            }
            
            if (aiAnalysis == null || aiAnalysis.contains("失败") || aiAnalysis.contains("错误")) {
                log.error("AI分析失败：{}", aiAnalysis);
                throw new RuntimeException("AI分析失败：" + aiAnalysis);
            }
            log.info("AI分析结果：{}", aiAnalysis);
            
            // 5. 保存或更新用户画像
            LambdaQueryWrapper<UserProfile> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserProfile::getUserId, id);
            UserProfile userProfile = this.getOne(queryWrapper);
            
            if (userProfile == null) {
                userProfile = new UserProfile();
                userProfile.setUserId(id);
                userProfile.setCreateTime(LocalDateTime.now());
                log.info("创建新的用户画像记录");
            } else {
                log.info("更新已存在的用户画像记录");
            }
            
            // 设置常点菜品
            String favoriteDishes = dishFrequency.entrySet().stream()
                    .limit(5)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.joining(","));
            userProfile.setFavoriteDishes(favoriteDishes);
            log.info("设置常点菜品：{}", favoriteDishes);
            
            // 设置口味偏好（从订单详情中提取）
            String tastePreference = orderDetails.stream()
                    .filter(detail -> detail.getDishFlavor() != null && !detail.getDishFlavor().isEmpty())
                    .map(OrderDetail::getDishFlavor)
                    .distinct()
                    .collect(Collectors.joining(","));
            userProfile.setTastePreference(tastePreference);
            log.info("设置口味偏好：{}", tastePreference);
            
            // 设置消费习惯（根据订单金额分析）
            String consumptionHabits = analyzeConsumptionHabits(orderDetails);
            userProfile.setConsumptionHabits(consumptionHabits);
            log.info("设置消费习惯：{}", consumptionHabits);
            
            // 设置AI分析结果
            userProfile.setAiAnalysis(aiAnalysis);
            userProfile.setUpdateTime(LocalDateTime.now());
            
            boolean success = this.saveOrUpdate(userProfile);
            log.info("保存用户画像{}", success ? "成功" : "失败");
            if (!success) {
                throw new RuntimeException("保存用户画像失败");
            }
        } catch (Exception e) {
            log.error("生成用户画像时发生错误：{}", e.getMessage());
            throw new RuntimeException("生成用户画像失败：" + e.getMessage());
        }
    }

    @Override
    public UserProfile getUserProfile(Long userId) {
        LambdaQueryWrapper<UserProfile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserProfile::getUserId, userId);
        return this.getOne(queryWrapper);
    }

    private String buildUserHistoryJson(Long userId, List<OrderDetail> orderDetails, Map<String, Long> dishFrequency) {
        // 构建用户历史数据JSON字符串
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"userId\":").append(userId).append(",");
        jsonBuilder.append("\"orderCount\":").append(orderDetails.size()).append(",");
        jsonBuilder.append("\"favoriteDishes\":[");
        
        // 添加最常点的5个菜品
        dishFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> {
                    jsonBuilder.append("{");
                    jsonBuilder.append("\"name\":\"").append(entry.getKey()).append("\",");
                    jsonBuilder.append("\"frequency\":").append(entry.getValue());
                    jsonBuilder.append("},");
                });
        
        if (jsonBuilder.charAt(jsonBuilder.length() - 1) == ',') {
            jsonBuilder.setLength(jsonBuilder.length() - 1);
        }
        
        jsonBuilder.append("]");
        jsonBuilder.append("}");
        
        return jsonBuilder.toString();
    }
    
    private String analyzeConsumptionHabits(List<OrderDetail> orderDetails) {
        // 分析用户的消费习惯
        double avgAmount = orderDetails.stream()
                .mapToDouble(detail -> detail.getAmount().doubleValue() * detail.getNumber())
                .average()
                .orElse(0.0);
                
        if (avgAmount > 100) {
            return "高消费";
        } else if (avgAmount > 50) {
            return "中等消费";
        } else {
            return "经济型消费";
        }
    }

    /**
     * 生成基础分析结果
     */
    private String generateBasicAnalysis(List<OrderDetail> orderDetails, Map<String, Long> dishFrequency) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("基础分析结果：\n\n");
        
        // 1. 口味偏好分析
        analysis.append("1. 口味偏好分析：\n");
        String tastePreference = orderDetails.stream()
                .filter(detail -> detail.getDishFlavor() != null && !detail.getDishFlavor().isEmpty())
                .map(OrderDetail::getDishFlavor)
                .distinct()
                .collect(Collectors.joining("、"));
        analysis.append("   - 偏好口味：").append(tastePreference.isEmpty() ? "暂无数据" : tastePreference).append("\n\n");
        
        // 2. 消费习惯分析
        analysis.append("2. 消费习惯分析：\n");
        double avgAmount = orderDetails.stream()
                .mapToDouble(detail -> detail.getAmount().doubleValue())
                .average()
                .orElse(0.0);
        analysis.append("   - 平均消费：").append(String.format("%.2f元", avgAmount)).append("\n\n");
        
        // 3. 个性化推荐
        analysis.append("3. 个性化推荐：\n");
        analysis.append("   - 常点菜品：").append(dishFrequency.entrySet().stream()
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining("、"))).append("\n");
        
        return analysis.toString();
    }
} 