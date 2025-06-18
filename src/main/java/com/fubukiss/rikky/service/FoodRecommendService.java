package com.fubukiss.rikky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fubukiss.rikky.entity.FoodRecommend;

public interface FoodRecommendService extends IService<FoodRecommend> {
    /**
     * 更新菜品推荐
     * @param id 菜品ID
     */
    void updateRecommend(Long id);
} 