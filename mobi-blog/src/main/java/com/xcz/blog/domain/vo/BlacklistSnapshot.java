package com.xcz.blog.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 黑名单规则快照（用于 Redis 缓存）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 黑名单 IP 集合 */
    @Builder.Default
    private Set<String> ips = new HashSet<>();

    /** 黑名单用户 ID 集合 */
    @Builder.Default
    private Set<Long> userIds = new HashSet<>();

    /** 违禁内容正则表达式（原始字符串，写入 Redis 前未编译） */
    @Builder.Default
    private List<String> contentPatterns = new ArrayList<>();
}
