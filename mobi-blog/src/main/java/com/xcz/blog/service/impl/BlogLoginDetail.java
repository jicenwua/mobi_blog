package com.xcz.blog.service.impl;

import com.xcz.blog.domain.BlogUser;
import com.xcz.blog.domain.enums.RoleStatue;
import com.xcz.blog.domain.enums.UserStatus;
import com.xcz.blog.mapper.BlogUserMapper;
import com.xcz.commons.core.exception.ServiceException;
import com.xcz.commons.security.extend.LoginUser;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 博客用户登录认证（邮箱作为 username）
 */
@Component
public class BlogLoginDetail implements UserDetailsService {

    @Resource
    private BlogUserMapper blogUserMapper;

    /**
     * 根据邮箱加载用户认证信息
     *
     * @param email 登录邮箱
     * @return Spring Security 用户详情
     * @throws UsernameNotFoundException 用户不存在或已停用
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        BlogUser user = blogUserMapper.selectUserByEmail(email);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        if (UserStatus.DISABLED.getCode().equals(user.getStatus())) {
            throw new ServiceException("账号已停用");
        }

        Map<String, Set<String>> roleMap = new HashMap<>();
        Integer role = user.getRole();
        roleMap.put(RoleStatue.getRoleStatus(role).name(), Collections.emptySet());

        return new LoginUser(
                user.getUserId(),
                null,
                null,
                null,
                roleMap,
                user.getEmail(),
                user.getNickName(),
                null,
                new HashMap<>(),
                user.getPassword(),
                UserStatus.NORMAL.getCode().equals(user.getStatus()),
                true,
                true,
                true
        );
    }
}
