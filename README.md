# Rook外卖系统

## 项目简介

Rook外卖系统是一个基于Spring Boot的外卖点餐平台，提供用户点餐、商家管理、订单处理等功能。系统集成了AI推荐功能，可以根据用户的点餐历史生成个性化推荐和用户画像分析。

## 技术栈

- JDK版本：1.8
- Spring Boot：2.4.5
- MyBatis-Plus：3.4.2
- MySQL：8.0
- Lombok：1.18.20
- FastJSON：1.2.76
- Druid：1.1.23
- Spring Mail：2.4.5
- Commons Lang：2.6
- DeepSeek AI：最新版

## 主要功能

1. 用户功能
   - 用户注册/登录
   - 邮箱验证（QQ邮箱）
   - 菜品浏览和搜索
   - 购物车管理
   - 订单管理
   - 个人中心
   - AI个性化推荐
   - 用户画像查看

2. 商家功能
   - 菜品管理
   - 套餐管理
   - 订单管理
   - 数据统计
   - 用户画像管理
     * 查看用户画像列表
     * 批量生成用户画像
     * 删除用户画像

3. AI功能
   - 用户画像生成
     * 常点菜品分析
     * 口味偏好分析
     * 消费习惯分析
     * AI深度分析
   - 个性化推荐
   - 消费习惯分析
   - 批量画像生成

## 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- QQ邮箱账号（用于发送验证码）
- DeepSeek API密钥

## 快速开始

1. 克隆项目

```bash
git clone https://github.com/Coldfully/RookTakeaway.git
```

2. 配置数据库

- 创建数据库：rook_takeaway
- 配置环境变量：
  - DB_URL：数据库连接URL
  - DB_USERNAME：数据库用户名
  - DB_PASSWORD：数据库密码

3. 配置邮箱服务

- 使用QQ邮箱服务
- 配置发件人邮箱和授权码
- 默认配置：
  - 邮箱：1468832469@qq.com
  - 验证码有效期：10分钟

4. 配置DeepSeek API

- 配置环境变量：
  - DEEPSEEK_API_KEY：API密钥
  - DEEPSEEK_API_URL：API地址（默认为：https://api.deepseek.com/v1/chat/completions）

5. 启动项目

```bash
mvn spring-boot:run
```

## 访问地址

- 用户端：http://localhost:9009
- 管理端：http://localhost:9009/backend
- API文档：http://localhost:9009/doc.html

## 项目结构

```
src/main/java/com/fubukiss/rikky
├── common          // 公共组件
├── config          // 配置类
├── controller      // 控制器
├── entity          // 实体类
├── mapper          // 数据访问层
├── service         // 服务层
└── util            // 工具类
```

## 用户画像功能说明

1. 画像生成
   - 自动分析用户订单历史
   - 生成常点菜品列表
   - 分析用户口味偏好
   - 评估消费习惯
   - AI深度分析用户特征

2. 画像管理
   - 支持批量生成用户画像
   - 支持删除无效画像
   - 提供画像列表查看
   - 支持按用户邮箱搜索

3. 画像内容
   - 常点菜品：展示用户最常点的5个菜品
   - 口味偏好：分析用户的口味选择
   - 消费习惯：包含平均消费、消费水平、消费特征
   - AI分析：提供深度用户特征分析

## 注意事项

1. 首次运行需要初始化数据库
2. 需要配置有效的QQ邮箱账号和授权码
3. 需要配置有效的DeepSeek API密钥
4. 文件上传路径默认配置为：D:\ProjectSome\rikky-takeaway-1.0.3\img\
5. 用户画像生成需要用户有订单历史数据
6. 批量生成用户画像时，系统会自动跳过没有订单数据的用户

## 开发团队

- 项目负责人：[Your Name]
- 开发团队：[Team Members]

## 许可证

本项目采用 MIT 许可证
