# 校园二手商品交易平台 🎓

一个基于 **微信小程序** + **Spring Boot** + **Layui** 的校园二手商品交易平台，支持商品买卖、论坛社区、即时聊天等功能。

---

## 📌 项目简介

本系统是为高校校园打造的二手商品交易平台，旨在方便校内学生进行二手物品的买卖与交流。项目包含三个子模块：

| 模块 | 技术栈 | 说明 |
|------|--------|------|
| **SecondMarket** | 微信小程序原生 | 用户端，学生使用的小程序 |
| **SecondMarketServer** | Spring Boot + MyBatis-Plus | 后端服务，提供 RESTful API |
| **SecondMarketManager** | HTML + Layui | 后台管理端，管理员使用 |

---

## ✨ 功能概览

### 用户端（微信小程序）

- 🔐 **用户系统**：注册、登录、个人信息修改、实名认证
- 🛍️ **商品交易**：商品浏览、搜索、分类筛选、发布商品、加入购物车、下单购买
- 📦 **订单管理**：查看订单状态、发货、确认收货、评价打分
- ❤️ **收藏功能**：收藏商品和帖子
- 💬 **论坛社区**：发帖、评论、楼中楼回复、帖子分类浏览
- 💰 **余额系统**：账户余额充值、交易扣款
- 📍 **地址管理**：收货地址的增删改查
- 🔔 **消息通知**：系统公告、订单通知
- 💬 **即时聊天**：买卖双方实时在线沟通

### 后台管理端

- 📊 **首页仪表盘**：数据统计概览
- 👥 **用户管理**：查看用户列表、编辑用户信息
- 📦 **商品管理**：商品审核（通过/驳回）、商品上下架
- 📋 **订单管理**：查看所有订单、订单状态跟踪
- 📝 **论坛管理**：帖子审核、帖子管理
- 🏷️ **分类管理**：商品分类、子分类的增删改查
- 🎯 **轮播图管理**：首页轮播图的配置
- 📢 **公告管理**：系统公告的发布与管理
- 📋 **菜单管理**：后台菜单配置
- 🔑 **管理员设置**：修改密码、个人资料

---

## 🚀 快速开始

### 环境要求

| 软件 | 版本要求 |
|------|----------|
| JDK | 1.8+ |
| Maven | 3.x |
| MySQL | 8.0+ |
| 微信开发者工具 | 最新版 |
| Node.js | 12+ （小程序 npm 依赖） |

### 1. 数据库初始化

执行 SQL 脚本创建数据库和表：

```bash
# 用 Navicat 或 MySQL 命令行执行
source school-market-master/sql/second_market.sql
```

默认管理员账号：`admin` / `admin`

### 2. 启动后端服务

## 方法一：
1.配置好maven，下载maven教程可自行搜索

<img width="1228" height="918" alt="image" src="https://github.com/user-attachments/assets/07abb020-388d-4f36-93a1-f69a07901ebd" />

2.将\school-market\SecondMarketServer设置为maven项目，配置成功后可以点击右上角绿色三角启动

<img width="1920" height="706" alt="image" src="https://github.com/user-attachments/assets/769f2c36-5d63-4466-b749-01e9058ac3f8" />

3.控制台出现启动成功，就代表后端启动完成

<img width="1760" height="400" alt="image" src="https://github.com/user-attachments/assets/7500b06c-b09f-475f-bf2f-bbc3005792e8" />

---

## 方法二：
```bash
# 进入后端目录
cd SecondMarketServer


# 修改数据库配置（如需要）
# 编辑 src/main/resources/application.yml
# 修改 spring.datasource.druid.username 和 password

# 编译并启动
mvn clean install -DskipTests
mvn spring-boot:run
```

启动成功后访问：
- **API 服务**：http://localhost:9088
- **Swagger 文档**：http://localhost:9088/doc.html

---

### 3. 启动后台管理端

后台管理端文件夹为\school-market\SecondMarketManager，我是配置在phpstudy小布面板启动的
下载phpstudy小布面板教程可自行搜索

1.配置phpstudy小布面板
<img width="983" height="766" alt="image" src="https://github.com/user-attachments/assets/1ee45816-2b14-4355-bfa0-4f78692c5c3d" />

2.配置nginx
<img width="1299" height="565" alt="image" src="https://github.com/user-attachments/assets/97ee0ac6-5acd-4a7d-ad7b-a731fdc9d0d1" />

3.创建配置网站
点击右侧网站
<img width="999" height="776" alt="image" src="https://github.com/user-attachments/assets/271fa95d-918f-4655-822a-b50a0fe02d30" />

4.启动网站
点击管理后，点击打开网页即可，如果显示网站创建成功，要配置phpstudy/XXX/WWW什么的可以尝试刷新浏览器页面
<img width="1009" height="757" alt="image" src="https://github.com/user-attachments/assets/190595fa-2449-4ef9-ab27-c25797c9a60b" />

---


### 4. 启动微信小程序

1. 打开 [微信开发者工具]
2. 导入项目，选择 `SecondMarket` 目录
3. 填写你自己的 AppID（或使用测试号）
4. 修改小程序中的 API 请求地址为你的后端地址
5. 点击编译运行

---

## 📁 项目结构

```
school-market/
├── SecondMarket/                # 微信小程序（用户端）
│   ├── pages/                   # 页面文件
│   │   ├── index/               # 首页
│   │   ├── category/            # 分类页
│   │   ├── send-goods/          # 发布商品
│   │   ├── forum/               # 论坛社区
│   │   ├── goods-detail/        # 商品详情
│   │   ├── forum-detail/        # 帖子详情
│   │   ├── cart2/               # 购物车
│   │   ├── order/               # 订单
│   │   ├── chat-list/           # 聊天列表
│   │   ├── chat-room/           # 聊天室
│   │   ├── ucenter/             # 个人中心
│   │   └── ...                  # 其他页面
│   ├── comComponent/            # 公共组件
│   ├── common/                  # 公共工具类
│   ├── app.js / app.json        # 小程序入口
│   └── package.json
│
├── SecondMarketServer/          # Spring Boot 后端
│   └── src/main/java/cn/only/hw/secondmarketserver/
│       ├── controller/          # 控制器层（14个）
│       ├── entity/              # 实体类（16个）
│       ├── mapper/              # MyBatis 数据访问层
│       ├── service/             # 业务逻辑层
│       └── config/              # 配置类（含Schema迁移）
│
├── SecondMarketManager/         # 后台管理端（Layui）
│   ├── index.html               # 管理端入口
│   ├── js/                      # 业务JS（各模块管理）
│   ├── libs/                    # 第三方库（Layui、jQuery等）
│   └── css/ / img/              # 样式和图片
│
└── school-market-master/        # 资源文件
    ├── sql/                     # 数据库SQL脚本
    └── img/                     # 图片存储目录
```

---

## 🗄️ 数据库表

| 序号 | 表名 | 说明 |
|------|------|------|
| 1 | `user` | 用户表 |
| 2 | `goods` | 商品表 |
| 3 | `orders` | 订单表 |
| 4 | `cart` | 购物车表 |
| 5 | `collect` | 收藏表 |
| 6 | `address` | 收货地址表 |
| 7 | `forum` | 论坛帖子表 |
| 8 | `forum_comment` | 论坛评论表 |
| 9 | `chat_session` | 聊天会话表 |
| 10 | `chat_message` | 聊天消息表 |
| 11 | `manager` | 管理员表 |
| 12 | `menu` | 系统菜单表 |
| 13 | `notice` | 公告通知表 |
| 14 | `banner` | 首页轮播图表 |
| 15 | `category` | 商品分类表 |
| 16 | `catechild` | 商品子分类表 |

---

## 🔧 技术栈

### 前端
- 微信小程序原生开发（WXML + WXSS + JavaScript）
- Crypto-JS 数据加密

### 后端
- **Spring Boot 2.4.5** - 核心框架
- **MyBatis-Plus 3.4.2** - ORM 框架
- **MySQL 8.0** - 关系型数据库
- **Druid 1.1.23** - 数据库连接池
- **Knife4j 3.0.2** - API 文档生成
- **Lombok** - 简化代码
- **阿里云短信 SDK** - 短信验证（可选）

### 后台管理
- **Layui 2.5.5** - UI 框架
- **jQuery 3.4.1** - DOM 操作
- **ECharts** - 数据可视化

---

## 📝 API 接口一览

| 控制器 | 路径前缀 | 说明 |
|--------|----------|------|
| UserController | `/user` | 用户注册/登录/信息管理 |
| GoodsController | `/goods` | 商品增删改查/审核 |
| OrdersController | `/orders` | 订单创建/查询/状态更新 |
| CartController | `/cart` | 购物车增删改查 |
| CollectController | `/collect` | 收藏/取消收藏 |
| AddressController | `/address` | 收货地址管理 |
| ForumController | `/forum` | 帖子/评论管理 |
| ChatController | `/chat` | 聊天会话/消息 |
| BannerController | `/banner` | 轮播图管理 |
| NoticeController | `/notice` | 公告管理 |
| CategoryController | `/category` | 分类管理 |
| CatechildController | `/catechild` | 子分类管理 |
| MenuController | `/menu` | 菜单管理 |
| ManagerController | `/manager` | 管理员登录/设置 |
| CommonController | `/common` | 文件上传等通用接口 |

---

## ⚙️ 配置说明

### 后端配置 (`application.yml`)

```yaml
server:
  port: 9088                          # 服务端口

spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/second_market  # 数据库连接
      username: root                   # 数据库用户名
      password: 123456                 # 数据库密码

common:
  path: ../school-market-master/img/   # 图片存储路径

security:
  crypto:
    secret: school-market-user-secret  # 数据加密密钥
```

### 图片存储

上传的图片默认存储在 `school-market-master/img/` 目录下。后端通过静态资源映射提供访问，确保该目录存在且可写。

---

## 📸 截图预览

<img width="254" height="552" alt="image" src="https://github.com/user-attachments/assets/2a159f63-e7ae-4cee-b19e-617461069ed3" />
<img width="219" height="473" alt="image" src="https://github.com/user-attachments/assets/8f092467-8897-4683-ba29-ba70d32bba93" />
<img width="265" height="563" alt="image" src="https://github.com/user-attachments/assets/13d69537-8e6e-4868-b924-49255f1836c4" />
<img width="249" height="541" alt="image" src="https://github.com/user-attachments/assets/d3c88d9e-6b82-47fd-b56b-bd7339e946d6" />
<img width="915" height="312" alt="image" src="https://github.com/user-attachments/assets/39ef5ffc-7fdb-4d47-a33a-15ea07bbfd8c" />
<img width="915" height="312" alt="image" src="https://github.com/user-attachments/assets/4ed1cc83-c4b6-47bd-9f5d-395146471ab0" />
<img width="915" height="254" alt="image" src="https://github.com/user-attachments/assets/c9f10c27-9863-4620-bd52-635c06e25277" />

---

## 🤝 贡献

本校园交易小程序系统基于https://github.com/WayneHu6/school-market，完善丰富了部分小程序功能

---
