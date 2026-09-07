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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * QQ 邮箱 SMTP 发送验证码
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private ThreadPoolTaskExecutor blogThread;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final RedissonClient redis = BlogConstants.redisson;

    /**
     * 向目标邮箱发送 6 位注册验证码
     *
     * @param toEmail 目标邮箱
     */
    @Override
    public void sendVerificationCode(String toEmail) {
        RBucket<String> frozenBucket = acquireFrozenLock(toEmail);
        CompletableFuture.runAsync(() -> sendCodeEmail(
                toEmail,
                frozenBucket,
                "【" + BlogConstants.SITE_NAME + "】邮箱验证码",
                code -> """
                        <p>您好，</p>
                        <p>您正在注册 %s 账号，验证码为：<b>%s</b></p>
                        <p>验证码 5 分钟内有效，请勿泄露给他人。</p>
                        """.formatted(BlogConstants.SITE_NAME, code)), blogThread);
    }

    @Override
    public void sendResetPasswordCode(String toEmail) {
        RBucket<String> frozenBucket = acquireFrozenLock(toEmail);
        CompletableFuture.runAsync(() -> sendCodeEmail(
                toEmail,
                frozenBucket,
                "【" + BlogConstants.SITE_NAME + "】重置密码验证码",
                code -> """
                        <p>您好，</p>
                        <p>您正在重置 %s 账号密码，验证码为：<b>%s</b></p>
                        <p>验证码 5 分钟内有效，请勿泄露给他人。如非本人操作请忽略此邮件。</p>
                        """.formatted(BlogConstants.SITE_NAME, code)), blogThread);
    }

    private RBucket<String> acquireFrozenLock(String toEmail) {
        RBucket<String> frozenBucket = redis.getBucket(BlogConstants.EMAIL_FROZEN + ":" + toEmail);
        if (!frozenBucket.setIfAbsent("1", BlogConstants.FROZEN_TTL)) {
            throw new ServiceException("发送太频繁，请稍后再试");
        }
        return frozenBucket;
    }

    private void sendCodeEmail(String toEmail, RBucket<String> frozenBucket, String subject,
                               Function<String, String> bodyBuilder) {
        try {
            String code = generateCode();
            RBucket<String> bucket = redis.getBucket(BlogConstants.EMAIL_CODE_KEY + toEmail);
            bucket.set(code, BlogConstants.EMAIL_CODE_TTL);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(bodyBuilder.apply(code), true);
            mailSender.send(message);
        } catch (Exception e) {
            frozenBucket.delete();
            log.error("发送验证码邮件失败，目标邮箱: {}", toEmail, e);
        }
    }

    /**
     * 生成邮件验证码
     *
     * @return 6 位数字验证码
     */
    private String generateCode() {
        int bound = (int) Math.pow(10, BlogConstants.EMAIL_CODE_LENGTH);
        int code = ThreadLocalRandom.current().nextInt(bound / 10, bound);
        return String.valueOf(code);
    }
}
