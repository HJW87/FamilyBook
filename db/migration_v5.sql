-- ============================================
-- 数据库迁移 v5：为 users 表添加 avatar 列
-- 目的：支持用户自定义头像
--   avatar 值：
--     NULL/空 = 默认头像
--     "male" = 男头像
--     "female" = 女头像
--     其他字符串 = 自定义上传的文件名
-- 日期：2026-06-02
-- ============================================

ALTER TABLE users
    ADD COLUMN avatar VARCHAR(200) NULL COMMENT '用户头像（male/female/文件名）'
    AFTER password;
