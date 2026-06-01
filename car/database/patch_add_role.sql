-- 安全补丁：为 sys_user 表添加 role 字段并设置初始值
-- 执行一次即可，幂等安全

-- 1. 添加 role 列（如已存在则忽略）
ALTER TABLE sys_user
    ADD COLUMN IF NOT EXISTS `role` VARCHAR(32) NOT NULL DEFAULT 'USER'
    COMMENT '用户角色: ADMIN=管理员, USER=普通用户';

-- 2. 将 admin 账号设为管理员
UPDATE sys_user SET role = 'ADMIN' WHERE username = 'admin';

-- 3. 其余用户默认为普通用户（兜底）
UPDATE sys_user SET role = 'USER' WHERE role IS NULL OR role = '';


