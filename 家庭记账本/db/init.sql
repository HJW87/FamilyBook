-- ============================================
-- 家庭记账本 - 数据库初始化脚本（完整版）
-- 包含所有迁移：多用户/家庭/权限/头像/family_id
-- 数据库: family_account | 字符集: utf8mb4
-- ============================================

CREATE DATABASE IF NOT EXISTS family_account
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE family_account;

-- ============================================
-- 1. 用户表
-- ============================================
DROP TABLE IF EXISTS `record`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `family_member`;
DROP TABLE IF EXISTS `family`;
DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
    `id`          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID（内部自增）',
    `username`    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    `password`    VARCHAR(255) NOT NULL COMMENT 'BCrypt密码',
    `avatar`      VARCHAR(200) NULL     COMMENT '用户头像（male/female/文件名，空=默认）',
    `display_id`  BIGINT       UNIQUE   COMMENT '用户展示ID（如100001），用于邀请等场景',
    `created_at`  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 家庭表
-- ============================================
CREATE TABLE `family` (
    `id`          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '家庭ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '家庭名称',
    `invite_code` VARCHAR(20)  NOT NULL UNIQUE COMMENT '邀请码',
    `admin_id`    BIGINT       NOT NULL COMMENT '管理员用户ID',
    `created_at`  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_family_admin` (`admin_id`),
    CONSTRAINT `fk_family_admin` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家庭表';

-- ============================================
-- 3. 家庭成员（身份标签）表
-- ============================================
CREATE TABLE `family_member` (
    `id`         BIGINT      AUTO_INCREMENT PRIMARY KEY COMMENT '成员ID',
    `family_id`  BIGINT      NOT NULL COMMENT '所属家庭ID',
    `name`       VARCHAR(50) NOT NULL COMMENT '身份标签名称（如：爸爸、妈妈）',
    `user_id`    BIGINT      NULL     UNIQUE COMMENT '绑定的用户ID（空=空座位）',
    `created_at` TIMESTAMP   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_member_family` (`family_id`),
    CONSTRAINT `fk_member_family` FOREIGN KEY (`family_id`) REFERENCES `family` (`id`),
    CONSTRAINT `fk_member_user`   FOREIGN KEY (`user_id`)   REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家庭成员（身份标签）表';

-- ============================================
-- 4. 收支类别表
-- ============================================
CREATE TABLE `category` (
    `id`         BIGINT      AUTO_INCREMENT PRIMARY KEY COMMENT '类别ID',
    `family_id`  BIGINT      NULL     COMMENT '所属家庭ID（NULL=预设模板，创建家庭时复制）',
    `name`       VARCHAR(50) NOT NULL COMMENT '类别名称',
    `type`       VARCHAR(10) NOT NULL COMMENT '收支类型: INCOME / EXPENSE',
    `icon`       VARCHAR(10) DEFAULT '📦' COMMENT 'emoji图标',
    `sort_order` INT         DEFAULT 0  COMMENT '排序权重（越大越靠前）',
    `created_at` TIMESTAMP   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_cat_type`   (`type`),
    INDEX `idx_cat_family` (`family_id`),
    CONSTRAINT `fk_cat_family` FOREIGN KEY (`family_id`) REFERENCES `family` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收支类别表';

-- ============================================
-- 5. 收支记录表
-- ============================================
CREATE TABLE `record` (
    `id`            BIGINT         AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    `family_id`     BIGINT         NULL     COMMENT '所属家庭ID',
    `user_id`       BIGINT         NULL     COMMENT '创建该记录的用户ID',
    `type`          VARCHAR(10)    NOT NULL COMMENT '收支类型: INCOME / EXPENSE',
    `category_id`   BIGINT         NOT NULL COMMENT '关联类别ID',
    `amount`        DECIMAL(10,2)  NOT NULL COMMENT '金额',
    `family_member` VARCHAR(50)    NOT NULL COMMENT '家庭成员身份标签',
    `record_date`   DATE           NOT NULL COMMENT '记账日期',
    `note`          VARCHAR(200)   DEFAULT '' COMMENT '备注',
    `created_at`    TIMESTAMP      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_rec_date`    (`record_date`),
    INDEX `idx_rec_type`    (`type`),
    INDEX `idx_rec_cat`     (`category_id`),
    INDEX `idx_rec_member`  (`family_member`),
    INDEX `idx_rec_family`  (`family_id`),
    INDEX `idx_rec_user`    (`user_id`),
    CONSTRAINT `fk_rec_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收支记录表';

-- ============================================
-- 预设数据（family_id=NULL 的模板类别）
-- 创建家庭时由 DataInitializer 复制到家庭下
-- ============================================

-- 收入类别（6个）
INSERT INTO `category` (`family_id`, `name`, `type`, `icon`, `sort_order`) VALUES
(NULL, '工资',     'INCOME', '💰', 10),
(NULL, '奖金',     'INCOME', '🎁', 9),
(NULL, '兼职',     'INCOME', '💻', 8),
(NULL, '理财',     'INCOME', '📈', 7),
(NULL, '红包',     'INCOME', '🧧', 6),
(NULL, '其他收入', 'INCOME', '📦', 1);

-- 支出类别（11个）
INSERT INTO `category` (`family_id`, `name`, `type`, `icon`, `sort_order`) VALUES
(NULL, '餐饮',     'EXPENSE', '🍔', 10),
(NULL, '交通',     'EXPENSE', '🚗', 9),
(NULL, '购物',     'EXPENSE', '🛒', 8),
(NULL, '居住',     'EXPENSE', '🏠', 7),
(NULL, '娱乐',     'EXPENSE', '🎮', 6),
(NULL, '医疗',     'EXPENSE', '🏥', 5),
(NULL, '教育',     'EXPENSE', '📚', 4),
(NULL, '人情',     'EXPENSE', '🎉', 3),
(NULL, '通讯',     'EXPENSE', '📱', 2),
(NULL, '狗粮',     'EXPENSE', '🐶', 1),
(NULL, '其他支出', 'EXPENSE', '📦', 0);
