# Rook外卖系统

## 项目简介
Rook外卖系统是一个基于Spring Boot的外卖点餐平台，提供用户点餐、商家管理、订单处理等功能。系统集成了AI推荐功能，可以根据用户的点餐历史生成个性化推荐。

## 技术栈
- JDK版本：1.8
- Spring Boot：2.4.5
- MyBatis-Plus：3.4.2
- MySQL：8.0
- Redis：2.8.0
- Lombok：1.18.20
- FastJSON：1.2.76
- Druid：1.1.23
- Spring Mail：2.4.5

## 主要功能
1. 用户功能
   - 用户注册/登录
   - 邮箱验证
   - 菜品浏览和搜索
   - 购物车管理
   - 订单管理
   - 个人中心
   - AI个性化推荐

2. 商家功能
   - 菜品管理
   - 套餐管理
   - 订单管理
   - 数据统计

3. AI功能
   - 用户画像生成
   - 个性化推荐
   - 消费习惯分析

## 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 2.8+

## 快速开始
1. 克隆项目
```bash
git clone https://github.com/your-username/rook-takeaway.git
```

2. 配置数据库
- 创建数据库：rook_takeaway
- 修改`application.yml`中的数据库配置

3. 配置Redis
- 确保Redis服务已启动
- 修改`application.yml`中的Redis配置

4. 配置邮箱服务
- 修改`application.yml`中的邮箱配置
- 配置发件人邮箱和授权码

5. 配置DeepSeek API
- 在`application.yml`中配置DeepSeek API密钥和URL
```yaml
deepseek:
  api:
    key: your-api-key-here
    url: https://api.deepseek.com/v1/chat/completions
```

6. 启动项目
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

## 注意事项
1. 首次运行需要初始化数据库
2. 确保Redis服务正常运行
3. 配置正确的邮箱服务信息
4. 配置有效的DeepSeek API密钥

## 开发团队
- 项目负责人：[Your Name]
- 开发团队：[Team Members]

## 许可证
本项目采用 MIT 许可证
