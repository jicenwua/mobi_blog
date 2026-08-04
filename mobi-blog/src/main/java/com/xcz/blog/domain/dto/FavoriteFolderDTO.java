package com.xcz.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建 / 更新收藏夹请求
 */
@Data
public class FavoriteFolderDTO {

    @NotBlank(message = "收藏夹名称不能为空")
    @Size(max = 32, message = "收藏夹名称不能超过 32 个字符")
    private String favoriteName;
}
