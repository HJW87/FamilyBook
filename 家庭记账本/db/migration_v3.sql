-- ============================================
-- 家庭记账本 - v3 迁移：添加用户展示ID
-- ============================================
USE family_account;

-- 给已有 users 表添加 display_id 列
ALTER TABLE users ADD COLUMN IF NOT EXISTS display_id BIGINT UNIQUE COMMENT '用户展示ID（如100001）';

-- 为已有用户生成展示ID（100000 + 自增ID）
UPDATE users SET display_id = 100000 + id WHERE display_id IS NULL;
