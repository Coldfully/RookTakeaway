package com.fubukiss.rikky.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fubukiss.rikky.common.R;
import com.fubukiss.rikky.entity.FoodRecommend;
import com.fubukiss.rikky.service.FoodRecommendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/backend/food/recommend")
public class FoodRecommendController {

    @Autowired
    private FoodRecommendService foodRecommendService;

    /**
     * 分页查询菜品推荐
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("分页查询菜品推荐，page={}, pageSize={}, name={}", page, pageSize, name);

        // 构造分页构造器
        Page<FoodRecommend> pageInfo = new Page<>(page, pageSize);

        // 构造条件构造器
        LambdaQueryWrapper<FoodRecommend> queryWrapper = new LambdaQueryWrapper<>();
        // 添加过滤条件
        queryWrapper.like(name != null, FoodRecommend::getName, name);
        // 添加排序条件
        queryWrapper.orderByDesc(FoodRecommend::getRecommendScore);

        // 执行查询
        foodRecommendService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 更新菜品推荐
     */
    @PostMapping("/{id}")
    public R<String> update(@PathVariable Long id) {
        log.info("更新菜品推荐，id={}", id);
        foodRecommendService.updateRecommend(id);
        return R.success("更新成功");
    }
} 