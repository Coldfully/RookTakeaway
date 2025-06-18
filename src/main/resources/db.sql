-- 用户画像表
CREATE TABLE IF NOT EXISTS `user_profile` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `taste_preference` varchar(255) DEFAULT NULL COMMENT '口味偏好',
    `favorite_dishes` varchar(255) DEFAULT NULL COMMENT '常点菜品',
    `consumption_habits` varchar(255) DEFAULT NULL COMMENT '消费习惯',
    `ai_analysis` text DEFAULT NULL COMMENT 'AI分析结果',
    `email` varchar(255) DEFAULT NULL COMMENT '用户邮箱',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户画像表'; 