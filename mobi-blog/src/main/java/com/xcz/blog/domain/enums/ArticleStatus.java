package com.xcz.blog.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章发布状态
 */
@Getter
@AllArgsConstructor
public enum ArticleStatus {

    DRAFT("draft", "草稿"),
    PUBLISHED("published", "已发布"),
    ARCHIVED("archived", "已归档");

    private final String code;
    private final String label;
}
