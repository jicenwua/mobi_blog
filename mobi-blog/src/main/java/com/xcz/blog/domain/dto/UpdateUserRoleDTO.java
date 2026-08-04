package com.xcz.blog.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改用户角色请求
 */
@Data
public class UpdateUserRoleDTO {

    @NotNull(message = "角色不能为空")
    private Integer role;
}
