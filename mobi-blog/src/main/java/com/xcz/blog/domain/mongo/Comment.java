package com.xcz.blog.domain.mongo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 文章评论（MongoDB 集合：blog_comment）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "blog_comment")
public class Comment {

    @Id
    private String id;

    /** MongoDB 文章 ID */
    @Indexed
    private String articleId;

    /** 评论用户 ID（MySQL blog_user.user_id） */
    @Indexed
    private Long userId;

    /** 评论用户昵称（查询时从 MySQL 补全，不持久化） */
    @Transient
    private String userNickName;

    /** 评论内容 */
    private String content;

    /** 父评论 ID，为空表示顶级评论 */
    @Indexed
    private String parentId;

    /** 点赞数 */
    private Integer likeCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
