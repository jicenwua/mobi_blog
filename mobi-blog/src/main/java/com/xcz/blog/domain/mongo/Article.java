package com.xcz.blog.domain.mongo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xcz.blog.domain.enums.ArticleCategory;
import com.xcz.blog.domain.enums.ArticleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 博客文章（MongoDB 集合：blog_article）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "blog_article")
public class Article {

    @Id
    private String id;

    /** 文章标题 */
    private String title;

    /** 文章摘要 */
    private String summary;

    /** 文章正文（Markdown / HTML） */
    private String content;

    /** 作者用户 ID（MySQL blog_user.user_id） */
    @Indexed
    private Long authorId;

    /** 作者昵称（查询时从 MySQL 补全，不持久化） */
    @Transient
    private String authorName;

    /**
     * 文章分类
     *
     * @see ArticleCategory
     */
    @Indexed
    private String category;

    /**
     * 文章标签（可多选，如：MySQL、Redis、微服务）
     */
    @Indexed
    private List<String> tags;

    /** 浏览量 */
    private Long viewCount;

    /** 收藏量 */
    private Long favoriteCount;

    /** 评论数 */
    private Long commentCount;

    /**
     * 发布状态
     *
     * @see ArticleStatus
     */
    @Indexed
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
