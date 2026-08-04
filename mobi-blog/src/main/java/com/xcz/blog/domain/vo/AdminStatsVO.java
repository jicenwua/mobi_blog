package com.xcz.blog.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理后台统计数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsVO {

    private long userCount;
    private long articleCount;
    private long commentCount;
    private long adminCount;
}
