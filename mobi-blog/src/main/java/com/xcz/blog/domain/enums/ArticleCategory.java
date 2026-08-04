package com.xcz.blog.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章分类
 */
@Getter
@AllArgsConstructor
public enum ArticleCategory {

    DATABASE("database", "数据库"),
    MIDDLEWARE("middleware", "中间件"),
    SPRING("spring", "Spring框架"),
    AI("ai", "AI");

    private final String code;
    private final String label;
}
