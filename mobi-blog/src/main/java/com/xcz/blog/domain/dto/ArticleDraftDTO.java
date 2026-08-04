package com.xcz.blog.domain.dto;

import com.xcz.blog.domain.enums.ArticleCategory;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 保存文章草稿请求（字段均可为空，至少需有一项非空内容由服务端校验）
 */
@Data
public class ArticleDraftDTO {

    @Size(max = 200, message = "标题长度不能超过 200")
    private String title;

    @Size(max = 500, message = "摘要长度不能超过 500")
    private String summary;

    private String content;

    /**
     * @see ArticleCategory
     */
    private String category;

    @Size(max = 10, message = "标签数量不能超过 10 个")
    private List<@Size(max = 20, message = "单个标签长度不能超过 20 个字符") String> tags;
}
