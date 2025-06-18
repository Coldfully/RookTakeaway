package com.fubukiss.rikky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fubukiss.rikky.entity.Dish;
import com.fubukiss.rikky.entity.FoodRecommend;
import com.fubukiss.rikky.entity.OrderDetail;
import com.fubukiss.rikky.mapper.FoodRecommendMapper;
import com.fubukiss.rikky.service.DishService;
import com.fubukiss.rikky.service.FoodRecommendService;
import com.fubukiss.rikky.service.OrderDetailService;
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
public class FoodRecommendServiceImpl extends ServiceImpl<FoodRecommendMapper, FoodRecommend> implements FoodRecommendService {

    @Autowired
    private DishService dishService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private DeepSeekClient deepSeekClient;

    @Override
    public void updateRecommend(Long id) {
        // 1. 获取菜品信息
        Dish dish = dishService.getById(id);
        if (dish == null) {
            return;
        }

        // 2. 获取菜品相关的订单数据
        List<OrderDetail> orderDetails = orderDetailService.getDishOrderDetails(id);
        
        // 3. 分析菜品数据
        double recommendScore = calculateRecommendScore(dish, orderDetails);
        String recommendReason = generateRecommendReason(dish, orderDetails);
        
        // 4. 保存或更新推荐信息
        FoodRecommend foodRecommend = this.getById(id);
        if (foodRecommend == null) {
            foodRecommend = new FoodRecommend();
            foodRecommend.setId(id);
            foodRecommend.setCreateTime(LocalDateTime.now());
        }
        
        foodRecommend.setName(dish.getName());
        foodRecommend.setImage(dish.getImage());
        foodRecommend.setPrice(dish.getPrice());
        // 计算销量
        int totalSales = orderDetails.stream()
                .mapToInt(OrderDetail::getNumber)
                .sum();
        foodRecommend.setSaleNum(totalSales);
        foodRecommend.setRecommendScore(recommendScore);
        foodRecommend.setRecommendReason(recommendReason);
        foodRecommend.setUpdateTime(LocalDateTime.now());
        
        this.saveOrUpdate(foodRecommend);
    }

    private double calculateRecommendScore(Dish dish, List<OrderDetail> orderDetails) {
        // 计算推荐分数（0-5分）
        double score = 0.0;
        
        // 1. 销量得分（最高2分）
        int totalSales = orderDetails.stream()
                .mapToInt(OrderDetail::getNumber)
                .sum();
        score += Math.min(totalSales / 100.0, 2.0);
        
        // 2. 价格得分（最高1分）
        double priceScore = 1.0 - (dish.getPrice().doubleValue() / 1000.0);
        score += Math.max(0, priceScore);
        
        // 3. 评分得分（最高2分）
        if (dish.getStatus() != null && dish.getStatus() == 1) {
            score += 2.0; // 在售状态加2分
        }
        
        return Math.min(score, 5.0);
    }

    private String generateRecommendReason(Dish dish, List<OrderDetail> orderDetails) {
        // 构建推荐理由
        StringBuilder reason = new StringBuilder();
        
        // 1. 销量情况
        int totalSales = orderDetails.stream()
                .mapToInt(OrderDetail::getNumber)
                .sum();
        if (totalSales > 100) {
            reason.append("销量火爆，");
        } else if (totalSales > 50) {
            reason.append("销量不错，");
        }
        
        // 2. 价格优势
        if (dish.getPrice().doubleValue() < 30) {
            reason.append("价格实惠，");
        }
        
        // 3. 状态情况
        if (dish.getStatus() != null && dish.getStatus() == 1) {
            reason.append("正在热销，");
        }
        
        // 4. 通用推荐语
        reason.append("值得一试！");
        
        return reason.toString();
    }
} 