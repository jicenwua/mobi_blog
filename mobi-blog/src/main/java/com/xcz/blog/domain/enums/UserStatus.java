package com.xcz.blog.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户账号状态
 */
@Getter
@AllArgsConstructor
public enum UserStatus {

    NORMAL("0", "正常"),
    DISABLED("1", "停用");

    private final String code;
    private final String label;
}
