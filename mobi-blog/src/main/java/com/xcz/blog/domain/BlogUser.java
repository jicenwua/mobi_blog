package com.xcz.blog.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 博客用户表 blog_user
 * <p>
 * 使用邮箱作为登录账号，注册时需通过邮箱验证码完成验证。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("blog_user")
public class BlogUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long userId;

    /** 邮箱，同时作为登录账号 */
    private String email;

    /** 登录密码（加密存储） */
    private String password;

    /** 用户昵称 */
    private String nickName;

    /***账号权限**/
    private Integer role;

    /**
     * 账号状态
     *
     * @see com.xcz.blog.domain.enums.UserStatus
     */
    private String status;

    /** 最后登录 IP */
    private String loginIp;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private String remark;

    @TableLogic
    private Integer delFlag;
}
