package com.fubukiss.rikky.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_profile")
public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("taste_preference")
    private String tastePreference;  // 口味偏好

    @TableField("favorite_dishes")
    private String favoriteDishes;   // 常点菜品

    @TableField("consumption_habits")
    private String consumptionHabits; // 消费习惯

    @TableField("ai_analysis")
    private String aiAnalysis;       // AI分析结果

    @TableField("email")
    private String email;           // 用户邮箱

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
} 