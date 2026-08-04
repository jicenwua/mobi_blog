package com.xcz.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xcz.blog.domain.BlogUser;
import com.xcz.blog.domain.dto.LoginDTO;
import com.xcz.blog.domain.dto.RegisterDTO;
import com.xcz.blog.domain.dto.ResetPasswordDTO;
import com.xcz.blog.domain.dto.SendVerificationCodeDTO;

import java.util.Map;

/**
 * 博客用户认证服务
 */
public interface BlogAuthService extends IService<BlogUser> {

    /**
     * 发送邮件
     * @param dto 邮件
     */
    void sendVerificationCode(SendVerificationCodeDTO dto);

    /**
     * 注册账号
     * @param dto 账号信息
     */
    void register(RegisterDTO dto);

    /**
     * 登录账号
     *
     * @param dto 登录信息
     * @return token
     */
    Map<String, Object> login(LoginDTO dto);

    /**
     * 发送重置密码验证码
     *
     * @param dto 邮箱
     */
    void sendResetCode(SendVerificationCodeDTO dto);

    /**
     * 重置密码
     *
     * @param dto 重置信息
     */
    void resetPassword(ResetPasswordDTO dto);
}
