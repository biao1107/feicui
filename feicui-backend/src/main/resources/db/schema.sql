-- ============================================================
-- 高翠网 - AI翡翠匹配平台 数据库建表脚本
-- 数据库: MySQL 8.x   字符集: utf8mb4
-- 表前缀: t_
-- 逻辑删除字段: deleted (0 未删除, 1 已删除)
-- ============================================================

CREATE DATABASE IF NOT EXISTS `gaocui` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `gaocui`;

-- ------------------------------------------------------------
-- 1. 商家表 (邮箱验证码登录, 无密码; FREE 免费 / VIP 会员)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `t_merchant`;
CREATE TABLE `t_merchant` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `email`           VARCHAR(128) NOT NULL COMMENT '登录邮箱(唯一)',
  `tier`            VARCHAR(16)  NOT NULL DEFAULT 'FREE' COMMENT '会员层级: FREE-免费 / VIP',
  `vip_expire_time` DATETIME              DEFAULT NULL    COMMENT 'VIP 到期时间(FREE 为空)',
  `web_notify`      TINYINT      NOT NULL DEFAULT 1       COMMENT '站内通知开关: 1 开 0 关',
  `email_notify`    TINYINT      NOT NULL DEFAULT 1       COMMENT '邮件通知开关: 1 开 0 关',
  `created_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         TINYINT      NOT NULL DEFAULT 0       COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';

-- ------------------------------------------------------------
-- 2. 邮箱验证码表 (注册/登录/改邮箱)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `t_verification_code`;
CREATE TABLE `t_verification_code` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `email`        VARCHAR(128) NOT NULL COMMENT '邮箱',
  `code`         VARCHAR(16)  NOT NULL COMMENT '验证码',
  `scene`        VARCHAR(32)  NOT NULL DEFAULT 'LOGIN' COMMENT '场景: LOGIN/REGISTER/CHANGE_EMAIL',
  `expire_time`  DATETIME     NOT NULL COMMENT '过期时间',
  `used`         TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已使用: 0 否 1 是',
  `created_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_email_scene` (`email`, `scene`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮箱验证码表';

-- ------------------------------------------------------------
-- 3. 商品表
-- status: DRAFT 草稿 / LISTED 已上架 / DELISTED 已下架
-- tags / images 用 JSON 存储数组
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `t_product`;
CREATE TABLE `t_product` (
  `id`           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_id`  BIGINT         NOT NULL COMMENT '所属商家ID',
  `title`        VARCHAR(128)   NOT NULL COMMENT '商品标题',
  `brief`        VARCHAR(255)            DEFAULT NULL COMMENT '商品简介(约50字)',
  `description`  TEXT COMMENT '商品详情(约300字, AI 详情)',
  `price`        DECIMAL(12,2)           DEFAULT NULL COMMENT '预估售价',
  `tags`         JSON COMMENT '标签数组',
  `images`       JSON COMMENT '图片URL数组, 第一张为主图',
  `status`       VARCHAR(16)    NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT/LISTED/DELISTED',
  `ai_generated` TINYINT        NOT NULL DEFAULT 0 COMMENT '文案是否AI生成: 0 否 1 是',
  `created_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_merchant_status` (`merchant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ------------------------------------------------------------
-- 4. 客资表 (买家在商品详情页留邮箱留言 → 进入对应商家客资)
-- status: PENDING 待联系 / CONTACTED 已联系
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `t_lead`;
CREATE TABLE `t_lead` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id`   BIGINT       NOT NULL COMMENT '关联商品ID',
  `merchant_id`  BIGINT       NOT NULL COMMENT '所属商家ID(冗余便于查询)',
  `buyer_email`  VARCHAR(128) NOT NULL COMMENT '买家联系邮箱',
  `message`      VARCHAR(500)          DEFAULT NULL COMMENT '买家留言(预算/场景等)',
  `status`       VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/CONTACTED',
  `created_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_status` (`merchant_id`, `status`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客资表';

-- ------------------------------------------------------------
-- 5. 系统通知表 (新客资提醒 / VIP 到期提醒)
-- type: NEW_LEAD 新客资 / VIP_EXPIRE VIP到期提醒
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `t_notification`;
CREATE TABLE `t_notification` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_id`  BIGINT       NOT NULL COMMENT '接收商家ID',
  `type`         VARCHAR(32)  NOT NULL COMMENT '类型: NEW_LEAD/VIP_EXPIRE',
  `content`      VARCHAR(500) NOT NULL COMMENT '通知内容',
  `is_read`      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读: 0 否 1 是',
  `created_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_read` (`merchant_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';
