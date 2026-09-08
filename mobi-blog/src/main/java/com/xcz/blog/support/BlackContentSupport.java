package com.xcz.blog.support;

import com.xcz.blog.constant.BlogConstants;
import com.xcz.blog.domain.BlackContent;
import com.xcz.blog.domain.vo.BlacklistSnapshot;
import com.xcz.blog.mapper.BlackContentMapper;
import com.xcz.commons.core.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 黑名单缓存与匹配辅助类。
 * <p>
 * 采用「本地内存 → Redis → 数据库」三级读取；
 * 预加载，新增/删除后调用 {@link #refreshCache()} 刷新。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BlackContentSupport {

    private final BlackContentMapper blackContentMapper;

    /** 本地热缓存，避免每个请求都访问 Redis */
    private volatile CompiledBlacklist localCache;

    /** 缓存加载锁，防止并发回源时重复查询数据库 */
    private final Object loadLock = new Object();

    /**
     * 判断 IP 是否命中黑名单（精确匹配）。
     *
     * @param ip 客户端 IP
     * @return 命中返回 true
     */
    public boolean isIpBlocked(String ip) {
        return getCompiled().isIpBlocked(ip);
    }

    /**
     * 判断用户是否命中黑名单。
     *
     * @param userId 用户 ID
     * @return 命中返回 true
     */
    public boolean isUserBlocked(Long userId) {
        return getCompiled().isUserBlocked(userId);
    }

    /**
     * 判断文本是否命中违禁词正则（忽略大小写，{@link Pattern#find()} 部分匹配）。
     *
     * @param content 待校验文本
     * @return 命中返回 true
     */
    public boolean matchesContent(String content) {
        return getCompiled().matchesContent(content);
    }

    /**
     * 从数据库重新加载黑名单，并同步更新 Redis 与本地缓存。
     * <p>
     * 在应用启动、新增规则、删除规则时调用。
     */
    public void refreshCache() {
        synchronized (loadLock) {
            BlacklistSnapshot snapshot = loadFromDatabase();
            BlogConstants.redisson.getBucket(BlogConstants.BLACKLIST_CACHE).set(snapshot, BlogConstants.BLACKLIST_CACHE_TTL);
            localCache = CompiledBlacklist.from(snapshot);
            log.info("黑名单缓存已刷新：IP {} 条，用户 {} 条，正则 {} 条",
                    snapshot.getIps().size(), snapshot.getUserIds().size(), snapshot.getContentPatterns().size());
        }
    }

    /**
     * 校验违禁词正则是否合法。
     *
     * @param pattern 正则表达式
     * @throws IllegalArgumentException 语法非法时抛出
     */
    public void validateContentPattern(String pattern) {
        if (StringUtils.isEmpty(pattern)) {
            return;
        }
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException ex) {
            throw new IllegalArgumentException("违禁词正则表达式不合法");
        }
    }

    /**
     * 获取编译后的黑名单快照。
     * <p>
     * 读取顺序：本地内存 → Redis → 数据库；后两级通过双重检查锁避免缓存击穿。
     *
     * @return 可立即用于匹配的快照
     */
    private CompiledBlacklist getCompiled() {
        CompiledBlacklist cached = localCache;
        if (cached != null) {
            return cached;
        }

        RBucket<BlacklistSnapshot> bucket = BlogConstants.redisson.getBucket(BlogConstants.BLACKLIST_CACHE);
        BlacklistSnapshot redisSnapshot = bucket.get();
        if (redisSnapshot != null) {
            localCache = CompiledBlacklist.from(redisSnapshot);
            return localCache;
        }

        synchronized (loadLock) {
            if (localCache != null) {
                return localCache;
            }
            redisSnapshot = bucket.get();
            if (redisSnapshot != null) {
                localCache = CompiledBlacklist.from(redisSnapshot);
                return localCache;
            }
            // 本地与 Redis 均无缓存时，由当前线程回源并刷新
            refreshCache();
            return localCache;
        }
    }

    /**
     * 扫描全表构建快照（IP / 用户 / 正则分桶）。
     * <p>
     * 数据库异常时记录警告并返回空规则，避免影响主流程启动。
     *
     * @return 黑名单快照
     */
    private BlacklistSnapshot loadFromDatabase() {
        Set<String> ips = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        List<String> contentPatterns = new ArrayList<>();

        try {
            for (BlackContent item : blackContentMapper.selectList(null)) {
                if (StringUtils.isNotEmpty(item.getBlackIp())) {
                    ips.add(item.getBlackIp());
                }
                if (item.getBlackUserId() != null) {
                    userIds.add(item.getBlackUserId());
                }
                if (StringUtils.isNotEmpty(item.getBlackContent())) {
                    contentPatterns.add(item.getBlackContent());
                }
            }
        } catch (Exception ex) {
            log.warn("加载黑名单数据失败，将使用空规则继续运行：{}", ex.getMessage());
        }

        return BlacklistSnapshot.builder()
                .ips(ips)
                .userIds(userIds)
                .contentPatterns(contentPatterns)
                .build();
    }
}
