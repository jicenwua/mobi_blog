package com.xcz.blog.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 黑名单配置 blog_black_content
 * <p>
 * 每条记录可配置 IP、用户 ID、违禁关键词之一或多项，命中任一项即拦截。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("blog_black_content")
public class BlackContent implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 黑名单 IP（客户端 IP 精确匹配） */
    private String blackIp;

    /** 黑名单用户 ID（与 blog_user.user_id 对应） */
    private Long blackUserId;

    /** 黑名单关键词（正则表达式，内容命中即拦截） */
    private String blackContent;

    /** 规则创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
