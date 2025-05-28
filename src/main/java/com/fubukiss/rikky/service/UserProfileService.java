package com.fubukiss.rikky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fubukiss.rikky.entity.UserProfile;

public interface UserProfileService extends IService<UserProfile> {
    
    /**
     * 生成用户画像
     * @param id 用户ID
     */
    void generateUserProfile(Long id);

    /**
     * 获取用户画像
     * @param userId 用户ID
     * @return 用户画像信息
     */
    UserProfile getUserProfile(Long userId);
} 