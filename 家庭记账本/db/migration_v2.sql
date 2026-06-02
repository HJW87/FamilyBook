-- ============================================
-- 家庭记账本 - 多用户多家庭架构 DDL 变更
-- ============================================
USE family_account;

-- 1. 创建 users 表
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID（内部自增）',
    display_id  BIGINT       NOT NULL UNIQUE COMMENT '用户展示ID（如100001），用于邀请等场景',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password    VARCHAR(255) NOT NULL COMMENT 'BCrypt密码',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 创建 family 表
CREATE TABLE IF NOT EXISTS family (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '家庭ID',
    name        VARCHAR(100) NOT NULL COMMENT '家庭名称',
    invite_code VARCHAR(20)  NOT NULL UNIQUE COMMENT '邀请码',
    admin_id    BIGINT       NOT NULL COMMENT '管理员用户ID',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (admin_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家庭表';

-- 3. 先删除有外键依赖的表（按依赖逆序删除）
DROP TABLE IF EXISTS record;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS family_member;

-- 4. 重建 category 表（添加 family_id）
CREATE TABLE category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '类别ID',
    family_id   BIGINT        COMMENT '所属家庭ID(NULL=预设模板)',
    name        VARCHAR(50)   NOT NULL COMMENT '类别名称',
    type        VARCHAR(10)   NOT NULL COMMENT '收支类型: INCOME/EXPENSE',
    icon        VARCHAR(10)   DEFAULT '📦' COMMENT 'emoji图标',
    sort_order  INT           DEFAULT 0 COMMENT '排序权重(越大越靠前)',
    created_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_type (type),
    INDEX idx_family_id (family_id),
    FOREIGN KEY (family_id) REFERENCES family(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收支类别表';

-- 5. 重建 family_member 表（添加 family_id 和 user_id）
CREATE TABLE family_member (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '成员ID',
    family_id   BIGINT       COMMENT '所属家庭ID',
    name        VARCHAR(50)  NOT NULL COMMENT '身份标签名称(如:爸爸)',
    user_id     BIGINT       UNIQUE COMMENT '绑定的用户ID（一对一，空=空座位）',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (family_id) REFERENCES family(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家庭成员表';

-- 6. 重建 record 表（添加 family_id）
CREATE TABLE record (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    family_id     BIGINT        COMMENT '所属家庭ID',
    type          VARCHAR(10)   NOT NULL COMMENT '收支类型: INCOME/EXPENSE',
    category_id   BIGINT        NOT NULL COMMENT '关联类别ID',
    amount        DECIMAL(10,2) NOT NULL COMMENT '金额',
    family_member VARCHAR(50)   NOT NULL COMMENT '家庭成员名称',
    record_date   DATE          NOT NULL COMMENT '记账日期',
    note          VARCHAR(200)  DEFAULT '' COMMENT '备注',
    created_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_record_date (record_date),
    INDEX idx_type (type),
    INDEX idx_category_id (category_id),
    INDEX idx_family_member (family_member),
    INDEX idx_family_id (family_id),
    CONSTRAINT fk_record_category FOREIGN KEY (category_id) REFERENCES category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收支记录表';
