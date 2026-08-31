package com.xcz.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcz.blog.constant.BlogConstants;
import com.xcz.blog.domain.BlogUser;
import com.xcz.blog.domain.dto.LoginDTO;
import com.xcz.blog.domain.dto.RegisterDTO;
import com.xcz.blog.domain.dto.ResetPasswordDTO;
import com.xcz.blog.domain.dto.SendVerificationCodeDTO;
import com.xcz.blog.domain.enums.RoleStatue;
import com.xcz.blog.domain.enums.UserStatus;
import com.xcz.blog.mapper.BlogUserMapper;
import com.xcz.blog.service.BlogAuthService;
import com.xcz.blog.service.MailService;
import com.xcz.commons.core.exception.CaptchaException;
import com.xcz.commons.core.exception.ServiceException;
import com.xcz.commons.core.exception.user.CaptchaExpireException;
import com.xcz.commons.core.exception.user.UserPasswordNotMatchException;
import com.xcz.commons.core.utils.ServletUtils;
import com.xcz.commons.core.utils.ip.IpUtils;
import com.xcz.commons.core.utils.ip.Ipv6Utils;
import com.xcz.commons.security.extend.LoginUser;
import com.xcz.commons.security.service.TokenService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 博客用户认证服务实现
 * <p>
 * 继承 {@link ServiceImpl}，通过 MyBatis-Plus 提供的 {@code lambdaQuery}、{@code save}、
 * {@code update} 等方法操作 {@code blog_user}，避免直接调用 Mapper。
 */
@Slf4j
@Service
public class BlogAuthServiceImpl extends ServiceImpl<BlogUserMapper, BlogUser> implements BlogAuthService {

    private final RedissonClient redisson = BlogConstants.redisson;

    @Resource
    private MailService mailService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private TokenService tokenService;

    /**
     * 发送注册邮件验证码
     *
     * @param dto 邮箱地址
     */
    @Override
    public void sendVerificationCode(SendVerificationCodeDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (existsByEmail(email)) {
            throw new ServiceException("该邮箱已注册");
        }

        String code = generateCode();
        RBucket<String> bucket = redisson.getBucket(BlogConstants.EMAIL_CODE_KEY + email);
        bucket.set(code, BlogConstants.EMAIL_CODE_TTL);
        mailService.sendVerificationCode(email, code);
    }

    /**
     * 注册账号
     *
     * @param dto 账号信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        verifyCode(email, dto.getVerificationCode());

        if (existsByEmail(email)) {
            throw new ServiceException("该邮箱已注册");
        }

        BlogUser user = BlogUser.builder()
                .email(email)
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickName(dto.getNickName())
                .role(RoleStatue.CUSTOMER.getValue())
                .status(UserStatus.NORMAL.getCode())
                .build();
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        save(user);

        redisson.getBucket(BlogConstants.EMAIL_CODE_KEY + email).delete();
    }

    /**
     * 登录账号
     *
     * @param dto 登录信息
     * @return 登录 token 及用户信息
     */
    @Override
    public Map<String, Object> login(LoginDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        String password = dto.getPassword();
        String ip = Ipv6Utils.getClientIp(ServletUtils.getRequest());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            loginUser.setIpaddr(ip);
            loginUser.setLoginLocation(IpUtils.getIpLocation(ip));
            String token = tokenService.createToken(loginUser);

            lambdaUpdate().eq(BlogUser::getUserId, loginUser.getUserId())
                    .set(BlogUser::getLoginIp, ip)
                    .set(BlogUser::getLoginDate, LocalDateTime.now());

            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("userId", loginUser.getUserId());
            map.put("nickname", loginUser.getName());
            map.put("role", loginUser.getAUthorityList());
            return map;
        } catch (UserPasswordNotMatchException e) {
            throw new UserPasswordNotMatchException();
        } catch (Exception e) {
            log.warn("博客用户登录失败，邮箱: {}, 原因: {}", email, e.getMessage());
            throw e;
        }
    }

    /**
     * 发送重置密码验证码
     *
     * @param dto 邮箱
     */
    @Override
    public void sendResetCode(SendVerificationCodeDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (!existsByEmail(email)) {
            throw new ServiceException("该邮箱未注册");
        }

        String code = generateCode();
        RBucket<String> bucket = redisson.getBucket(BlogConstants.EMAIL_CODE_KEY + email);
        bucket.set(code, BlogConstants.EMAIL_CODE_TTL);
        mailService.sendResetPasswordCode(email, code);
    }

    /**
     * 重置密码
     *
     * @param dto 重置信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        verifyCode(email, dto.getVerificationCode());

        BlogUser user = getByEmail(email);
        if (user == null) {
            throw new ServiceException("该邮箱未注册");
        }

        update(new LambdaUpdateWrapper<BlogUser>()
                .eq(BlogUser::getUserId, user.getUserId())
                .set(BlogUser::getPassword, passwordEncoder.encode(dto.getPassword()))
                .set(BlogUser::getUpdateTime, LocalDateTime.now()));

        redisson.getBucket(BlogConstants.EMAIL_CODE_KEY + email).delete();
    }

    /**
     * 判断邮箱是否已注册
     */
    private boolean existsByEmail(String email) {
        return lambdaQuery().eq(BlogUser::getEmail, email).exists();
    }

    /**
     * 根据邮箱查询用户
     */
    private BlogUser getByEmail(String email) {
        return lambdaQuery().eq(BlogUser::getEmail, email).one();
    }

    /**
     * 校验注册验证码
     *
     * @param email 邮箱
     * @param code  验证码
     */
    private void verifyCode(String email, String code) {
        RBucket<String> bucket = redisson.getBucket(BlogConstants.EMAIL_CODE_KEY + email);
        String cachedCode = bucket.get();
        if (cachedCode == null) {
            throw new CaptchaExpireException();
        }
        if (!cachedCode.equals(code)) {
            throw new CaptchaException("验证码错误");
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
