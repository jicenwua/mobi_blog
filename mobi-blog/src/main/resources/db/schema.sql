-- mobi-blog MySQL 初始化脚本

-- ----------------------------
-- 博客用户表
-- 邮箱作为登录账号，注册时需邮箱验证码验证
-- ----------------------------
CREATE TABLE IF NOT EXISTS `blog_user` (
    `user_id`        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `email`          VARCHAR(100) NOT NULL COMMENT '邮箱（登录账号）',
    `password`       VARCHAR(100) NOT NULL COMMENT '密码（加密）',
    `nick_name`      VARCHAR(50)  NOT NULL COMMENT '昵称',
    `avatar`         VARCHAR(255)          DEFAULT NULL COMMENT '头像',
    `role`           TINYINT      NOT NULL DEFAULT 1 COMMENT '角色（1普通用户 2管理员 3超级管理员）',
    `status`         CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `login_ip`       VARCHAR(50)           DEFAULT NULL COMMENT '最后登录IP',
    `login_date`     DATETIME              DEFAULT NULL COMMENT '最后登录时间',
    `create_time`    DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`         VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    `del_flag`       TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客用户表';

-- ----------------------------
-- 用户收藏夹表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `blog_favorite` (
    `favorite_id`   BIGINT      NOT NULL AUTO_INCREMENT COMMENT '收藏夹ID',
    `user_id`       BIGINT      NOT NULL COMMENT '用户ID',
    `favorite_name` VARCHAR(32) NOT NULL COMMENT '收藏夹名称',
    `create_time`   DATETIME             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`        VARCHAR(500)         DEFAULT NULL COMMENT '备注',
    `del_flag`      TINYINT     NOT NULL DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    PRIMARY KEY (`favorite_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏夹表';

-- ----------------------------
-- 收藏夹文章关联表
-- article_id 关联 MongoDB blog_article 集合的 _id
-- ----------------------------
CREATE TABLE IF NOT EXISTS `blog_favorite_article` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `favorite_id` BIGINT      NOT NULL COMMENT '收藏夹ID',
    `article_id`  VARCHAR(32) NOT NULL COMMENT 'MongoDB文章ID',
    `create_time` DATETIME             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_favorite_article` (`favorite_id`, `article_id`),
    KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏夹文章关联表';

-- ----------------------------
-- 管理员操作日志表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `blog_admin_log` (
    `log_id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `operator_id`    BIGINT       NOT NULL COMMENT '操作人用户ID',
    `module`         VARCHAR(50)  NOT NULL COMMENT '操作模块',
    `operation`      VARCHAR(50)  NOT NULL COMMENT '操作类型',
    `target_id`      VARCHAR(64)           DEFAULT NULL COMMENT '操作目标ID',
    `detail`         VARCHAR(500)          DEFAULT NULL COMMENT '操作详情',
    `create_time`    DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`log_id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';
