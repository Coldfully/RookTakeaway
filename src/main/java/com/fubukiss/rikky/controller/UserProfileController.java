package com.fubukiss.rikky.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fubukiss.rikky.common.R;
import com.fubukiss.rikky.entity.User;
import com.fubukiss.rikky.entity.UserProfile;
import com.fubukiss.rikky.service.UserProfileService;
import com.fubukiss.rikky.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/backend/user/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户画像
     */
    @GetMapping("/page")
    public R<Page<UserProfile>> page(
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String email) {
        
        log.info("查询用户画像列表，页码：{}，每页数量：{}，邮箱：{}", currentPage, pageSize, email);
        
        // 构建分页对象
        Page<UserProfile> page = new Page<>(currentPage, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<UserProfile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(UserProfile::getUpdateTime);
        
        // 如果指定了邮箱，先查询用户
        if (email != null && !email.isEmpty()) {
            LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.like(User::getEmail, email);
            List<User> users = userService.list(userQueryWrapper);
            
            log.info("根据邮箱查询到用户数量：{}", users.size());
            
            if (!users.isEmpty()) {
                List<Long> userIds = users.stream()
                        .map(User::getId)
                        .collect(Collectors.toList());
                queryWrapper.in(UserProfile::getUserId, userIds);
            } else {
                log.info("未找到匹配的用户，返回空结果");
                return R.success(new Page<>());
            }
        }
        
        // 执行分页查询
        userProfileService.page(page, queryWrapper);
        log.info("查询到用户画像数量：{}", page.getRecords().size());
        
        // 获取所有用户ID
        List<Long> userIds = page.getRecords().stream()
                .map(UserProfile::getUserId)
                .collect(Collectors.toList());
        
        // 查询用户信息
        final Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userService.listByIds(userIds);
            log.info("查询到用户信息数量：{}", users.size());
            users.forEach(user -> userMap.put(user.getId(), user));
        }
        
        // 设置用户邮箱信息
        page.getRecords().forEach(profile -> {
            User user = userMap.get(profile.getUserId());
            if (user != null) {
                profile.setEmail(user.getEmail());
            }
        });
        
        return R.success(page);
    }

    /**
     * 生成用户画像
     */
    @PostMapping("/generate/{id}")
    public R<String> generateProfile(@PathVariable Long id) {
        try {
            userProfileService.generateUserProfile(id);
            return R.success("用户画像生成成功");
        } catch (Exception e) {
            log.error("生成用户画像失败", e);
            return R.error("生成用户画像失败：" + e.getMessage());
        }
    }

    /**
     * 批量生成用户画像
     */
    @PostMapping("/generate/batch")
    public R<String> generateBatchProfile() {
        try {
            userProfileService.generateBatchUserProfile();
            return R.success("批量生成用户画像成功");
        } catch (Exception e) {
            log.error("批量生成用户画像失败", e);
            return R.error("批量生成用户画像失败：" + e.getMessage());
        }
    }

    /**
     * 删除用户画像
     */
    @DeleteMapping("/{id}")
    public R<String> deleteProfile(@PathVariable Long id) {
        try {
            userProfileService.removeById(id);
            return R.success("用户画像删除成功");
        } catch (Exception e) {
            log.error("删除用户画像失败", e);
            return R.error("删除用户画像失败：" + e.getMessage());
        }
    }
} 