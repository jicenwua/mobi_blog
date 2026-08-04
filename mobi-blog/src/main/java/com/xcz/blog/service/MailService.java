package com.xcz.blog.service;

/**
 * 邮件发送服务（QQ 邮箱 SMTP）
 */
public interface MailService {

    /**
     * 向目标邮箱发送 6 位注册验证码
     *
     * @param toEmail 目标邮箱
     * @param code    验证码
     */
    void sendVerificationCode(String toEmail, String code);

    /**
     * 向目标邮箱发送 6 位重置密码验证码
     *
     * @param toEmail 目标邮箱
     * @param code    验证码
     */
    void sendResetPasswordCode(String toEmail, String code);
}
