package com.xcz.blog.service;

/**
 * 邮件发送服务（QQ 邮箱 SMTP）
 */
public interface MailService {

    /**
     * 向目标邮箱发送 6 位注册验证码
     *
     * @param toEmail 目标邮箱
     */
    void sendVerificationCode(String toEmail);

    /**
     * 向目标邮箱发送 6 位重置密码验证码
     *
     * @param toEmail 目标邮箱
     */
    void sendResetPasswordCode(String toEmail);
}
