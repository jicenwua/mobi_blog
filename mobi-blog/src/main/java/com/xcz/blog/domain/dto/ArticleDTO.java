package com.xcz.blog.domain.dto;

import com.xcz.blog.domain.enums.ArticleCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建 / 更新文章请求
 */
@Data
public class ArticleDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过 200")
    private String title;

    @Size(max = 500, message = "摘要长度不能超过 500")
    private String summary;

    @NotBlank(message = "正文不能为空")
    private String content;

    /**
     * @see ArticleCategory
     */
    @NotBlank(message = "分类不能为空")
    private String category;

    /** 文章标签，最多 10 个，每个标签最长 20 字符 */
    @Size(max = 10, message = "标签数量不能超过 10 个")
    @NotEmpty(message = "标签不能为空")
    private List<@Size(max = 20, message = "单个标签长度不能超过 20 个字符") String> tags;

}
