package com.xcz.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论请求
 */
@Data
public class CommentDTO {

    @NotBlank(message = "文章 ID 不能为空")
    private String articleId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过 1000 字")
    private String content;

    /** 父评论 ID，回复评论时传入 */
    private String parentId;
}
