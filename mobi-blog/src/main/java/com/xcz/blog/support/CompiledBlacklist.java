package com.xcz.blog.support;

import com.xcz.blog.domain.vo.BlacklistSnapshot;
import com.xcz.commons.core.utils.StringUtils;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * 内存中可直接匹配的黑名单快照（正则已编译）。
 * <p>
 * 由 {@link BlackContentSupport} 在加载/刷新缓存时构建，供高频请求路径零 IO 校验。
 */
@Getter
class CompiledBlacklist {

    /** 黑名单 IP 集合（精确匹配） */
    private final Set<String> ips;

    /** 黑名单用户 ID 集合 */
    private final Set<Long> userIds;

    /** 已编译的违禁词正则列表 */
    private final List<Pattern> contentPatterns;

    private CompiledBlacklist(Set<String> ips, Set<Long> userIds, List<Pattern> contentPatterns) {
        this.ips = ips;
        this.userIds = userIds;
        this.contentPatterns = contentPatterns;
    }

    /**
     * 构建空快照（无拦截规则）。
     */
    static CompiledBlacklist empty() {
        return new CompiledBlacklist(Set.of(), Set.of(), List.of());
    }

    /**
     * 将 Redis / 数据库快照转换为可执行的内存结构，并预编译正则。
     *
     * @param snapshot 原始快照，可为 null
     * @return 编译后的黑名单；snapshot 为 null 时返回空规则
     */
    static CompiledBlacklist from(BlacklistSnapshot snapshot) {
        if (snapshot == null) {
            return empty();
        }
        Set<String> ips = snapshot.getIps() == null ? Set.of() : new HashSet<>(snapshot.getIps());
        Set<Long> userIds = snapshot.getUserIds() == null ? Set.of() : new HashSet<>(snapshot.getUserIds());
        List<Pattern> patterns = snapshot.getContentPatterns() == null
                ? List.of()
                : snapshot.getContentPatterns().stream()
                .filter(StringUtils::isNotEmpty)
                .map(CompiledBlacklist::compilePattern)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new CompiledBlacklist(ips, userIds, patterns);
    }

    /**
     * 编译单条违禁词正则；语法非法时返回 null 并跳过。
     */
    private static Pattern compilePattern(String raw) {
        try {
            return Pattern.compile(raw, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } catch (PatternSyntaxException ex) {
            return null;
        }
    }

    /**
     * 判断 IP 是否在黑名单中。
     */
    boolean isIpBlocked(String ip) {
        return StringUtils.isNotEmpty(ip) && ips.contains(ip);
    }

    /**
     * 判断用户 ID 是否在黑名单中。
     */
    boolean isUserBlocked(Long userId) {
        return userId != null && userIds.contains(userId);
    }

    /**
     * 判断文本是否命中任一违禁词正则（部分匹配）。
     */
    boolean matchesContent(String content) {
        if (StringUtils.isEmpty(content) || contentPatterns.isEmpty()) {
            return false;
        }
        for (Pattern pattern : contentPatterns) {
            if (pattern.matcher(content).find()) {
                return true;
            }
        }
        return false;
    }
}
