package com.xcz.blog.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增黑名单配置请求。
 * <p>
 * IP、用户 ID、关键词至少配置一项，未填写的字段不会写入数据库。
 */
@Data
public class BlackContentDTO {

    /** 黑名单 IP（精确匹配） */
    @Size(max = 50, message = "IP 长度不能超过 50 个字符")
    private String blackIp;

    /** 黑名单用户 ID（精确匹配，需为已注册用户） */
    private Long blackUserId;

    /** 违禁词正则表达式（内容命中即拦截，忽略大小写） */
    @Size(max = 200, message = "关键词长度不能超过 200 个字符")
    private String blackContent;
}
