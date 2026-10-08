package com.xcz.blog.support;

import com.xcz.blog.constant.BlogConstants;
import com.xcz.commons.core.utils.StringUtils;
import jakarta.annotation.Resource;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.KeysScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class ViewSupport {

    private static final RedissonClient redisson = BlogConstants.redisson;

    private static final String VIEW = "blog:view:";

    @Resource
    private ArticleMongoSupport articleMongoSupport;

    /**
     * 定时刷星观看量
     */
    @Scheduled(fixedRate = 60000)
    public void updateView() {
        Iterable<String> keys = redisson.getKeys().getKeys(KeysScanOptions.defaults().pattern(VIEW + "*"));
        Map<String, Long> viewMap = new HashMap<>();
        keys.iterator().forEachRemaining(key -> {
            //如果是该键名开头，则获取浏览量
            String articleId = StringUtils.substring(key, VIEW.length());
            long count = redisson.getAtomicLong(key).getAndDelete();
            if (count <= 0) {
                return;
            }
            viewMap.put(articleId, count);
            if (viewMap.size() >= 100) {
                articleMongoSupport.incrementViewCountBatch(viewMap);
                viewMap.clear();
            }
        });
        if (!viewMap.isEmpty()) {
            articleMongoSupport.incrementViewCountBatch(viewMap);
        }
    }

    /**
     * 缓存新增观看量
     * <p>同一用户/IP 在 24h 内只计一次浏览量。
     * <p>使用 {@code SETNX + EXPIRE} 的原子版本 {@link RBucket#setIfAbsent(Object, Duration)}
     * 避免原实现中 {@code bucket.expire(...)} 在 key 不存在时不会创建 key 导致去重失效的问题。
     *
     * @param articleId 文章id
     * @param id        用户ID或IP
     */
    public static void addView(String articleId, String id) {
        RBucket<Object> bucket = redisson.getBucket(VIEW + articleId + ":" + id);
        // 原子地「不存在则创建并设置 24h 过期」；返回 false 表示已存在（去重命中）
        boolean created = bucket.setIfAbsent(Boolean.TRUE, Duration.ofDays(1));
        if (!created) {
            return;
        }
        redisson.getAtomicLong(VIEW + articleId).incrementAndGet();
    }


}
