package com.xcz.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 修改用户状态请求
 */
@Data
public class UpdateUserStatusDTO {

    /** 账号状态：0 正常，1 停用 */
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "[01]", message = "状态值不合法")
    private String status;
}
