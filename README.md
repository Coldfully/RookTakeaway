# Rook外卖系统

## 项目简介

Rook外卖系统是一个外卖点餐平台，是基于悦刻外卖项目（`https://github.com/riverify/rikky-takeaway`）改编的。系统在原项目的基础上新增了了AI推荐功能，可以根据用户的点餐历史生成个性化推荐和用户画像分析。（使用DeepSeek的API接口）

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
- DeepSeek AI：最新

## 主要功能

1. 用户功能

   用户可以进行注册/登录，采用的是邮箱验证，未经注册的用户在验证后会自动注册。登录后，可以在界面上进行菜品浏览和搜索、购物车管理、订单和地址管理等相关操作。
2. 商家功能

   管理菜品、套餐、订单，进行增删改查的操作，可以一键生成AI对用户的画像，根据用户过往点餐记录自动生成用户画像分析。对于已有的用户画像，也可以进行删除或重新生成。用户画像分析包括对常点菜品、口味偏好、消费习惯的基础分析，还有在此基础上利用AI大模型进行的AI深度分析，对于多个用户的情况，可以点击批量生成，一键为所有用户生成画像。

## 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- QQ邮箱账号（用于发送验证码，如果换用其他邮箱需要修改配置信息）
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
- 请根据需要自行配置发件人邮箱和授权码

4. 配置DeepSeek AI

- 在 `application.yml`中配置：
- ```yaml
  deepseek:
    api:
      key: sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
      url: https://api.deepseek.com/v1/chat/completions
  ```

5. 启动项目

```bash
mvn spring-boot:run
```

## 访问地址

- 用户端：http://localhost:9009
- 管理端：http://localhost:9009/backend
- API文档：http://localhost:9009/doc.html
- 也可以自行在配置文件中修改端口

## 目录结构

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

## 重要说明

- **图片上传目录**：所有用户上传的图片均存放于 `/img/`目录，已在 `.gitignore`中忽略，无需纳入版本控制。
- **配置文件**：`src/main/resources/application.yml`包含数据库、邮箱、AI等敏感配置，建议根据实际环境调整。
- **编译输出**：`/target/`目录为Maven编译输出，已在 `.gitignore`中忽略。
- **IDE配置**：`.idea/`等IDE相关目录已忽略。
- **问题**：相关前端功能还存在问题，暂未解决

## .gitignore 示例

详见项目根目录 `.gitignore`，主要内容如下：

```gitignore
# Java编译输出
/target/
*.class

# 日志文件
*.log

# IDE配置
.idea/
*.iml
*.ipr
*.iws
.idea/shelf/
.idea/workspace.xml
.idea/httpRequests/
.idea/dataSources/
.idea/dataSources.local.xml

# 本地环境配置
*.local
*.env

# 临时文件
*.tmp
*.swp
*.bak

# 打包文件
*.jar
*.war
*.ear
*.zip
*.tar.gz
*.rar

# 虚拟机崩溃日志
hs_err_pid*

# 图片上传目录
/img/
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
