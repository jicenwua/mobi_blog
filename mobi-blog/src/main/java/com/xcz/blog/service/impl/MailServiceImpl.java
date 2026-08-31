package com.xcz.blog.service.impl;

import com.xcz.blog.constant.BlogConstants;
import com.xcz.blog.service.MailService;
import com.xcz.commons.core.exception.ServiceException;
import jakarta.annotation.Resource;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * QQ 邮箱 SMTP 发送验证码
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final RedissonClient redis = BlogConstants.redisson;

    /**
     * 向目标邮箱发送 6 位注册验证码
     *
     * @param toEmail 目标邮箱
     * @param code    验证码
     */
    @Override
    public void sendVerificationCode(String toEmail, String code) {
        RBucket<String> frozenBucket = redis.getBucket(BlogConstants.EMAIL_FROZEN + ":" + toEmail);
        if (!frozenBucket.setIfAbsent("1", BlogConstants.FROZEN_TTL)) {
            throw new ServiceException("发送太频繁，请稍后再试");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("【Mobi Blog】邮箱验证码");
            helper.setText("""
                    <p>您好，</p>
                    <p>您正在注册 Mobi Blog 账号，验证码为：<b>%s</b></p>
                    <p>验证码 5 分钟内有效，请勿泄露给他人。</p>
                    """.formatted(code), true);
            mailSender.send(message);
        } catch (Exception e) {
            frozenBucket.delete();
            log.error("发送验证码邮件失败，目标邮箱: {}", toEmail, e);
            throw new ServiceException("验证码发送失败，请稍后重试");
        }
    }

    @Override
    public void sendResetPasswordCode(String toEmail, String code) {
        RBucket<String> frozenBucket = redis.getBucket(BlogConstants.EMAIL_FROZEN + ":" + toEmail);
        if (!frozenBucket.setIfAbsent("1", BlogConstants.FROZEN_TTL)) {
            throw new ServiceException("发送太频繁，请稍后再试");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("【Mobi Blog】重置密码验证码");
            helper.setText("""
                    <p>您好，</p>
                    <p>您正在重置 Mobi Blog 账号密码，验证码为：<b>%s</b></p>
                    <p>验证码 5 分钟内有效，请勿泄露给他人。如非本人操作请忽略此邮件。</p>
                    """.formatted(code), true);
            mailSender.send(message);
        } catch (Exception e) {
            frozenBucket.delete();
            log.error("发送重置密码验证码邮件失败，目标邮箱: {}", toEmail, e);
            throw new ServiceException("验证码发送失败，请稍后重试");
        }
    }
}
