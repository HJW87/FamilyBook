-- ============================================
-- 数据库迁移 v4：为 record 表添加 user_id 列
-- 目的：标识每笔账单的创建用户，支持：
--   1. 用户只能修改/删除自己的账单
--   2. 统计模块按用户筛选个人账单
--   3. AI 助手绑定用户ID
-- 日期：2026-06-02
-- ============================================

ALTER TABLE record
    ADD COLUMN user_id BIGINT NULL COMMENT '创建该记录的用户ID'
    AFTER family_id;

-- 为 user_id 创建索引（提升按用户筛选时的查询性能）
CREATE INDEX idx_record_user_id ON record(user_id);

-- 可选：如果已有历史数据，可以尝试根据 family_member 名称回填 user_id
-- UPDATE record r
-- JOIN family_member fm ON r.family_id = fm.family_id AND r.family_member = fm.name
-- SET r.user_id = fm.user_id
-- WHERE r.user_id IS NULL AND fm.user_id IS NOT NULL;
