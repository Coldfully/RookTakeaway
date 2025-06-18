package com.fubukiss.rikky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fubukiss.rikky.entity.OrderDetail;
import com.fubukiss.rikky.entity.User;
import com.fubukiss.rikky.entity.UserProfile;
import com.fubukiss.rikky.mapper.UserProfileMapper;
import com.fubukiss.rikky.service.OrderDetailService;
import com.fubukiss.rikky.service.UserProfileService;
import com.fubukiss.rikky.service.UserService;
import com.fubukiss.rikky.util.DeepSeekClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService {

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;

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
                if (aiAnalysis == null) {
                    log.warn("AI分析服务返回null，使用基础分析");
                    aiAnalysis = generateBasicAnalysis(orderDetails, dishFrequency);
                }
            } catch (Exception e) {
                log.error("AI分析服务调用失败，使用基础分析：{}", e.getMessage());
                // 使用基础分析作为备选方案
                aiAnalysis = generateBasicAnalysis(orderDetails, dishFrequency);
            }
            
            if (aiAnalysis == null) {
                log.error("基础分析也失败了");
                throw new RuntimeException("无法生成用户画像分析");
            }
            log.info("分析结果：{}", aiAnalysis);
            
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

    @Override
    public void generateBatchUserProfile() {
        // 获取所有用户
        List<User> users = userService.list();
        log.info("开始批量生成用户画像，总用户数：{}", users.size());
        
        int successCount = 0;
        int failCount = 0;
        StringBuilder errorMessages = new StringBuilder();
        
        for (User user : users) {
            try {
                log.info("开始处理用户：{}，邮箱：{}", user.getId(), user.getEmail());
                generateUserProfile(user.getId());
                successCount++;
                log.info("用户{}的画像生成成功", user.getEmail());
            } catch (Exception e) {
                failCount++;
                String errorMsg = String.format("用户%s（%s）生成失败：%s", user.getEmail(), user.getId(), e.getMessage());
                log.error(errorMsg);
                errorMessages.append(errorMsg).append("\n");
            }
        }
        
        String resultMessage = String.format("批量生成完成，成功：%d，失败：%d", successCount, failCount);
        if (failCount > 0) {
            resultMessage += "\n失败详情：\n" + errorMessages.toString();
        }
        
        log.info(resultMessage);
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
        
        // 计算消费稳定性评估
        double variance = orderDetails.stream()
                .mapToDouble(detail -> detail.getAmount().doubleValue() * detail.getNumber())
                .map(amount -> Math.pow(amount - avgAmount, 2))
                .average()
                .orElse(0.0);
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("消费习惯分析：\n");
        analysis.append(String.format("- 平均消费：%.2f元\n", avgAmount));
        
        // 消费水平评估
        if (avgAmount > 100) {
            analysis.append("- 消费水平：高消费\n");
        } else if (avgAmount > 50) {
            analysis.append("- 消费水平：中等消费\n");
        } else {
            analysis.append("- 消费水平：经济型消费\n");
        }
        
        if (variance < 100) {
            analysis.append("- 消费特征：消费稳定\n");
        } else if (variance < 500) {
            analysis.append("- 消费特征：消费波动适中\n");
        } else {
            analysis.append("- 消费特征：消费波动较大\n");
        }
        
        return analysis.toString();
    }

    /**
     * 生成基础分析结果
     */
    private String generateBasicAnalysis(List<OrderDetail> orderDetails, Map<String, Long> dishFrequency) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("基础分析结果：\n\n");
        
        // 1. 口味偏好分析
        analysis.append("1. 口味偏好分析：\n");
        Map<String, Long> flavorFrequency = orderDetails.stream()
                .filter(detail -> detail.getDishFlavor() != null && !detail.getDishFlavor().isEmpty())
                .flatMap(detail -> Arrays.stream(detail.getDishFlavor().split(",")))
                .collect(Collectors.groupingBy(
                    flavor -> flavor,
                    Collectors.counting()
                ));
        
        if (flavorFrequency.isEmpty()) {
            analysis.append("   - 暂无口味偏好数据\n");
        } else {
            analysis.append("   - 口味偏好统计：\n");
            flavorFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> {
                    analysis.append(String.format("     * %s：%d次\n", entry.getKey(), entry.getValue()));
                });
        }
        analysis.append("\n");
        
        // 2. 消费习惯分析
        analysis.append("2. 消费习惯分析：\n");
        double avgAmount = orderDetails.stream()
                .mapToDouble(detail -> detail.getAmount().doubleValue() * detail.getNumber())
                .average()
                .orElse(0.0);
        analysis.append(String.format("   - 平均消费：%.2f元\n", avgAmount));
        
        // 计算消费稳定性
        double variance = orderDetails.stream()
                .mapToDouble(detail -> detail.getAmount().doubleValue() * detail.getNumber())
                .map(amount -> Math.pow(amount - avgAmount, 2))
                .average()
                .orElse(0.0);
        
        if (variance < 100) {
            analysis.append("   - 消费特征：消费稳定\n");
        } else if (variance < 500) {
            analysis.append("   - 消费特征：消费波动适中\n");
        } else {
            analysis.append("   - 消费特征：消费波动较大\n");
        }
        analysis.append("\n");
        
        // 3. 个性化推荐
        analysis.append("3. 个性化推荐：\n");
        analysis.append("   - 常点菜品：\n");
        dishFrequency.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(3)
            .forEach(entry -> {
                analysis.append(String.format("     * %s（%d次）\n", entry.getKey(), entry.getValue()));
            });
        
        return analysis.toString();
    }
} 