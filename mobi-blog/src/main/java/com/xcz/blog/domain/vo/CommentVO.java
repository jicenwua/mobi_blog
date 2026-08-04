package com.xcz.blog.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xcz.blog.domain.mongo.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 父评论展示对象（含子评论预览）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO {

    private String id;

    private String articleId;

    private Long userId;

    private String userNickName;

    private String content;

    private Integer likeCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 点赞最高的子评论预览（最多 3 条） */
    private List<Comment> childComments;

    /** 子评论总数 */
    private long childCount;

    /** 是否还有更多子评论 */
    private boolean hasMoreChildren;

    public static CommentVO from(Comment parent, List<Comment> childComments, long childCount) {
        return CommentVO.builder()
                .id(parent.getId())
                .articleId(parent.getArticleId())
                .userId(parent.getUserId())
                .userNickName(parent.getUserNickName())
                .content(parent.getContent())
                .likeCount(parent.getLikeCount())
                .createTime(parent.getCreateTime())
                .childComments(childComments)
                .childCount(childCount)
                .hasMoreChildren(childCount > childComments.size())
                .build();
    }
}
