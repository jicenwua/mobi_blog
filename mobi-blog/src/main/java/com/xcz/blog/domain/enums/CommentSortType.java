package com.xcz.blog.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 父评论排序方式
 */
@Getter
@AllArgsConstructor
public enum CommentSortType {

    /** 按点赞数降序，相同点赞数按发布时间降序 */
    HOT,

    /** 按发布时间降序 */
    TIME
}
