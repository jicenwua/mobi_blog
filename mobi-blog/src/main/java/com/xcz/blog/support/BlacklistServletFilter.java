package com.xcz.blog.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcz.blog.service.BlackContentService;
import com.xcz.commons.core.domain.ResponseEntity;
import com.xcz.commons.core.utils.StringUtils;
import com.xcz.commons.core.utils.ip.Ipv6Utils;
import com.xcz.commons.core.utils.response.ResponseEntityUtils;
import com.xcz.commons.security.extend.LoginUser;
import com.xcz.commons.security.service.TokenService;
import com.xcz.commons.security.utils.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 全局黑名单请求过滤器。
 * <p>
 * 拦截命中 IP 或用户 ID 的请求：已登录用户强制退出登录；未登录用户返回通用错误提示。
 * 违禁词正则不在此过滤器校验，由业务接口（如发表评论）单独处理。
 */
@RequiredArgsConstructor
public class BlacklistServletFilter extends OncePerRequestFilter {

    /** 黑名单管理接口前缀，命中时不拦截，便于管理员解封 */
    private static final String BLACKLIST_ADMIN_PATH_PREFIX = "/blog/black";

    private final BlackContentService blackContentService;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    /**
     * 对每个请求校验 IP / 用户是否命中黑名单。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 黑名单管理接口放行，避免管理员误封自己后无法解封
        if (isBlacklistAdminPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = Ipv6Utils.getClientIp(request);
        String token = SecurityUtils.getToken(request);
        Long userId = resolveUserId(token);

        if (!blackContentService.isRequestBlocked(ip, userId)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 已登录用户：注销 token 并清理安全上下文
        if (StringUtils.isNotEmpty(token)) {
            tokenService.logout(token);
            SecurityContextHolder.clearContext();
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    ResponseEntityUtils.fail("账号或 IP 已被限制，请重新登录"));
            return;
        }

        // 未登录用户：返回通用错误，不暴露黑名单细节
        writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                ResponseEntityUtils.fail("请求错误"));
    }

    /**
     * 判断是否为黑名单管理接口（不参与全局拦截）。
     */
    private boolean isBlacklistAdminPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith(BLACKLIST_ADMIN_PATH_PREFIX);
    }

    /**
     * 从 token 解析用户 ID；无 token 或 token 无效时返回 null。
     *
     * @param token 请求头中的 Bearer Token
     * @return 用户 ID，无法解析时返回 null
     */
    private Long resolveUserId(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        try {
            LoginUser loginUser = tokenService.getLoginUser(token);
            return loginUser == null ? null : loginUser.getUserId();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 向客户端写入 JSON 格式的统一响应体。
     *
     * @param response HTTP 响应
     * @param status   HTTP 状态码
     * @param body     响应体
     */
    private void writeJson(HttpServletResponse response, int status, ResponseEntity<?> body) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
