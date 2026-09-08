package com.xcz.blog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcz.blog.service.BlackContentService;
import com.xcz.blog.support.BlacklistServletFilter;
import com.xcz.commons.core.security.SecurityChainFilter;
import com.xcz.commons.security.service.TokenService;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注册全局黑名单 Servlet 过滤器。
 * <p>
 * 通过 {@link SecurityChainFilter} 接入 Spring Security 过滤器链，对所有请求做 IP / 用户拦截。
 */
@Configuration
@RequiredArgsConstructor
public class BlacklistFilterConfig {

    private final BlackContentService blackContentService;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    /**
     * 将黑名单过滤器注册到 Security 链中。
     *
     * @return Security 扩展过滤器
     */
    @Bean
    public SecurityChainFilter blacklistSecurityChainFilter() {
        Filter filter = new BlacklistServletFilter(blackContentService, tokenService, objectMapper);
        return () -> filter;
    }
}
