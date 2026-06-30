/*
 校园二手交易论坛系统 - 完整数据库SQL脚本
 Database: MySQL 8.0+
 Database Name: second_market
 Project: 校园二手市场交易平台
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 创建数据库
-- ----------------------------
CREATE DATABASE IF NOT EXISTS `second_market` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `second_market`;

-- ----------------------------
-- Table structure for user - 用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID（主键）',
  `account` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名(账号)',
  `sex` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别',
  `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `tel` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话（加密）',
  `idcard` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号（加密）',
  `balance` double NOT NULL DEFAULT 0 COMMENT '用户余额',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户密码（加密）',
  `college` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学院',
  `grade` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '班级',
  `roomnumb` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '宿舍号',
  `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic COMMENT '用户表';

-- ----------------------------
-- Table structure for goods - 商品表
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '商品ID（主键）',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品名称',
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品类别',
  `price` double NULL DEFAULT NULL COMMENT '商品价格',
  `number` int(11) NULL DEFAULT 1 COMMENT '商品数量',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '成色',
  `dealtypy` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '交易方式',
  `send_user` int(11) NULL DEFAULT NULL COMMENT '发布者用户ID',
  `contact_way` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系方式',
  `imgs` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品图片（多个用逗号分隔）',
  `describes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品描述',
  `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品封面图',
  `manage` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '审核状态:0发布 1管理员审核通过 2管理员审核不通过 3删除',
  `send_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_send_user` (`send_user`) USING BTREE,
  KEY `idx_type` (`type`) USING BTREE,
  KEY `idx_manage` (`manage`) USING BTREE,
  KEY `idx_send_time` (`send_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic COMMENT '商品表';

-- ----------------------------
-- Table structure for orders - 订单表
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单ID（主键）',
  `userid` int(11) NULL DEFAULT NULL COMMENT '下单用户ID',
  `goodsid` int(11) NULL DEFAULT NULL COMMENT '商品ID',
  `addressid` int(11) NULL DEFAULT NULL COMMENT '收货地址ID',
  `logistics` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流单号',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '订单金额',
  `number` int(11) NOT NULL DEFAULT 1 COMMENT '购买数量',
  `state` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT '订单状态',
  `rating` int(11) NULL DEFAULT NULL COMMENT '订单评分',
  `review_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评价内容',
  `send_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_userid` (`userid`) USING BTREE,
  KEY `idx_goodsid` (`goodsid`) USING BTREE,
  KEY `idx_state` (`state`) USING BTREE,
  KEY `idx_send_time` (`send_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic COMMENT '订单表';

-- ----------------------------
-- Table structure for address - 收货地址表
-- ----------------------------
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '地址ID（主键）',
  `userid` int(11) NULL DEFAULT NULL COMMENT '所属用户ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人姓名',
  `tel` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话',
  `province` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '省',
  `city` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '市',
  `county` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '县',
  `detail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址',
  `isdefault` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否默认地址',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_userid` (`userid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic COMMENT '收货地址表';

-- ----------------------------
-- Table structure for cart - 购物车表
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '购物车ID（主键）',
  `userid` int(11) NULL DEFAULT NULL COMMENT '用户ID',
  `goodsid` int(11) NULL DEFAULT NULL COMMENT '商品ID',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '商品单价',
  `number` int(11) NOT NULL DEFAULT 1 COMMENT '购买数量',
  `send_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_userid` (`userid`) USING BTREE,
  KEY `idx_goodsid` (`goodsid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic COMMENT '购物车表';

-- ----------------------------
-- Table structure for collect - 收藏表
-- ----------------------------
DROP TABLE IF EXISTS `collect`;
CREATE TABLE `collect` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '收藏ID（主键）',
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收藏类型(0商品 1帖子)',
  `user` int(11) NULL DEFAULT NULL COMMENT '用户ID',
  `sid` int(11) NULL DEFAULT NULL COMMENT '帖子或商品ID',
  `c_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user` (`user`) USING BTREE,
  KEY `idx_type` (`type`) USING BTREE,
  KEY `idx_sid` (`sid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic COMMENT '收藏表';

-- ----------------------------
-- Table structure for forum - 论坛帖子表
-- ----------------------------
DROP TABLE IF EXISTS `forum`;
CREATE TABLE `forum` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '帖子ID（主键）',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '帖子标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '帖子内容',
  `imgs` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '帖子图片（多个用逗号分隔）',
  `send_user` int(11) NULL DEFAULT NULL COMMENT '发布用户ID',
  `send_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `pass_time` datetime(0) NULL DEFAULT NULL COMMENT '过审时间',
  `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '帖子封面',
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '校园日常' COMMENT '帖子类型',
  `manage` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT '审核状态(0发布 1审核通过 2审核不通过)',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_send_user` (`send_user`) USING BTREE,
  KEY `idx_type` (`type`) USING BTREE,
  KEY `idx_manage` (`manage`) USING BTREE,
  KEY `idx_send_time` (`send_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic COMMENT '论坛帖子表';

-- ----------------------------
-- Table structure for forum_comment - 论坛评论表
-- ----------------------------
DROP TABLE IF EXISTS `forum_comment`;
CREATE TABLE `forum_comment` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '评论ID（主键）',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `forum_id` int(11) NOT NULL COMMENT '帖子ID',
  `user_id` int(11) NOT NULL COMMENT '评论用户ID',
  `parent_id` int(11) DEFAULT NULL COMMENT '父评论ID（回复）',
  `reply_to_user_id` int(11) DEFAULT NULL COMMENT '回复给用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1正常, 0删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_forum_id` (`forum_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='论坛评论表';

-- ----------------------------
-- Table structure for chat_session - 聊天会话表
-- ----------------------------
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID（主键）',
  `user_id` INT NOT NULL COMMENT '发起者用户ID',
  `target_user_id` INT NOT NULL COMMENT '目标用户ID',
  `goods_id` INT DEFAULT NULL COMMENT '关联商品ID',
  `last_message` VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息预览',
  `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
  `unread_count_user` INT DEFAULT 0 COMMENT '发起者未读数量',
  `unread_count_target` INT DEFAULT 0 COMMENT '目标用户未读数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target_goods` (`user_id`,`target_user_id`,`goods_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target_user_id` (`target_user_id`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_last_message_time` (`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天会话表';

-- ----------------------------
-- Table structure for chat_message - 聊天消息表
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID（主键）',
  `session_id` BIGINT NOT NULL COMMENT '会话ID',
  `sender_id` INT NOT NULL COMMENT '发送者ID',
  `receiver_id` INT NOT NULL COMMENT '接收者ID',
  `message_type` VARCHAR(20) DEFAULT 'text' COMMENT '消息类型',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `is_read` TINYINT DEFAULT 0 COMMENT '已读状态:0未读 1已读',
  `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
  `send_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_send_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天消息表';

-- ----------------------------
-- Table structure for manager - 管理员表
-- ----------------------------
DROP TABLE IF EXISTS `manager`;
CREATE TABLE `manager` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '管理员账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '管理员密码',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管理员头像',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_account` (`account`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic COMMENT '管理员表';

-- ----------------------------
-- Table structure for menu - 菜单表
-- ----------------------------
DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '菜单ID（主键）',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标地址',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单名称',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跳转地址',
  `sort` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '排序',
  `send_user` int(11) NULL DEFAULT NULL COMMENT '发布者ID',
  `send_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic COMMENT '系统菜单表';

-- ----------------------------
-- Table structure for notice - 公告通知表
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '公告ID（主键）',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内容',
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型: (公告1 通知0)',
  `send_user` int(11) NULL DEFAULT NULL COMMENT '发布者ID',
  `send_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_type` (`type`) USING BTREE,
  KEY `idx_send_time` (`send_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic COMMENT '公告通知表';

-- ----------------------------
-- Table structure for banner - 轮播图表
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '轮播图ID（主键）',
  `img_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片地址',
  `send_user` int(11) NULL DEFAULT NULL COMMENT '发布者ID',
  `send_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic COMMENT '首页轮播图表';

-- ----------------------------
-- Table structure for category - 商品分类表
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `cateid` int(11) NOT NULL AUTO_INCREMENT COMMENT '分类ID（主键）',
  `catename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类名称',
  PRIMARY KEY (`cateid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic COMMENT '商品分类表';

-- ----------------------------
-- Table structure for catechild - 子分类表
-- ----------------------------
DROP TABLE IF EXISTS `catechild`;
CREATE TABLE `catechild` (
  `childid` int(11) NOT NULL AUTO_INCREMENT COMMENT '子分类ID（主键）',
  `cateid` int(11) NULL DEFAULT NULL COMMENT '父分类ID',
  `goodid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品ID列表',
  `image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '子分类图片',
  `childname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '子分类名称',
  PRIMARY KEY (`childid`) USING BTREE,
  KEY `idx_cateid` (`cateid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic COMMENT '商品子分类表';

-- ----------------------------
-- 初始化数据 - 默认管理员账号
-- 账号: admin  密码: admin
-- ----------------------------
INSERT INTO `manager` (`id`, `account`, `password`, `avatar`) VALUES (1, 'admin', 'admin', NULL);

-- ----------------------------
-- 初始化数据 - 示例分类
-- ----------------------------
INSERT INTO `category` (`cateid`, `catename`) VALUES (1, '电子产品');
INSERT INTO `category` (`cateid`, `catename`) VALUES (2, '图书教材');
INSERT INTO `category` (`cateid`, `catename`) VALUES (3, '服饰鞋包');
INSERT INTO `category` (`cateid`, `catename`) VALUES (4, '生活用品');
INSERT INTO `category` (`cateid`, `catename`) VALUES (5, '运动户外');
INSERT INTO `category` (`cateid`, `catename`) VALUES (6, '学习用品');

SET FOREIGN_KEY_CHECKS = 1;

/*
 数据库创建完成！
 数据库名称: second_market
 表总数: 16张表
 包含功能:
  - 用户管理
  - 商品管理
  - 订单管理
  - 购物车
  - 收藏
  - 论坛社区
  - 即时聊天
  - 后台管理
  - 分类管理
  - 轮播图
  - 公告通知
*/