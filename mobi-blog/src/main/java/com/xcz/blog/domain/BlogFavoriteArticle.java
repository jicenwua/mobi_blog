package com.xcz.blog.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏夹文章关联表 blog_favorite_article
 * <p>
 * articleId 关联 MongoDB 中文章文档的 _id。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("blog_favorite_article")
public class BlogFavoriteArticle implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 收藏夹 ID */
    private Long favoriteId;

    /** MongoDB 文章 ID */
    private String articleId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
