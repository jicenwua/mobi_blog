package com.xcz.blog.controller;

import com.xcz.blog.domain.dto.LoginDTO;
import com.xcz.blog.domain.dto.RegisterDTO;
import com.xcz.blog.domain.dto.ResetPasswordDTO;
import com.xcz.blog.domain.dto.SendVerificationCodeDTO;
import com.xcz.blog.service.BlogAuthService;
import com.xcz.commons.core.domain.ResponseEntity;
import com.xcz.commons.core.utils.response.ResponseEntityUtils;
import com.xcz.commons.security.annotation.Release;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户认证接口（邮箱注册 / 登录）
 */
@Release
@RestController
@RequestMapping("/blog/auth")
public class BlogAuthController {

    @Resource
    private BlogAuthService blogAuthService;

    /**
     * 发送邮件验证码
     * @param dto   邮箱地址
     */
    @Release
    @PostMapping("/send-code")
    public ResponseEntity<Void> sendVerificationCode(@Valid @RequestBody SendVerificationCodeDTO dto) {
        blogAuthService.sendVerificationCode(dto);
        return ResponseEntityUtils.ok();
    }

    /**
     * 注册账号
     * @param dto   账号信息
     */
    @Release
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterDTO dto) {
        blogAuthService.register(dto);
        return ResponseEntityUtils.ok();
    }

    /**
     * 登录
     * @param dto   登录信息
     * @return  登录token
     */
    @Release
    @PostMapping("/login")
    public ResponseEntity<Map<String,Object>> login(@Valid @RequestBody LoginDTO dto) {
        return ResponseEntityUtils.ok(blogAuthService.login(dto));
    }

    /**
     * 发送重置密码验证码
     * @param dto 邮箱地址
     */
    @Release
    @PostMapping("/send-reset-code")
    public ResponseEntity<Void> sendResetCode(@Valid @RequestBody SendVerificationCodeDTO dto) {
        blogAuthService.sendResetCode(dto);
        return ResponseEntityUtils.ok();
    }

    /**
     * 重置密码
     * @param dto 重置信息
     */
    @Release
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        blogAuthService.resetPassword(dto);
        return ResponseEntityUtils.ok();
    }
}
