package com.xcz.blog.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员操作日志 blog_admin_log
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("blog_admin_log")
public class BlogAdminLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long logId;

    /** 操作人用户 ID，昵称与角色在查询列表时动态关联 */
    private Long operatorId;

    /** 操作模块（文章 / 评论 / 用户） */
    private String module;

    /** 操作类型（发布文章 / 删除文章 / 删除评论 / 修改角色） */
    private String operation;

    /** 操作目标 ID（文章 ID、评论 ID、用户 ID 等） */
    private String targetId;

    /** 操作详情 */
    private String detail;

    /** 操作时间 */
    private LocalDateTime createTime;
}
