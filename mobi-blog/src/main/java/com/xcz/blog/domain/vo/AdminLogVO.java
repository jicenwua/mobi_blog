package com.xcz.blog.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理后台操作日志展示对象
 * <p>
 * 操作人昵称与角色在查询时从 {@code blog_user} 动态补全，不持久化到日志表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLogVO {

    /** 日志 ID */
    private Long logId;

    /** 操作人用户 ID */
    private Long operatorId;

    /** 操作人昵称（查询时动态补全） */
    private String operatorName;

    /** 操作人角色（查询时动态补全，如 ADMIN / MASTER） */
    private String operatorRole;

    /** 操作模块 */
    private String module;

    /** 操作类型 */
    private String operation;

    /** 操作目标 ID */
    private String targetId;

    /** 操作详情 */
    private String detail;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
