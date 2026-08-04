package com.xcz.blog.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理后台用户列表项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserVO {

    private Long userId;
    private String email;
    private String nickName;
    private String role;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
