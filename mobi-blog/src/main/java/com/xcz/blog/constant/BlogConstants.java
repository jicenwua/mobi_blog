package com.xcz.blog.constant;

import com.xcz.commons.redis.extend.DatabaseEnum;
import com.xcz.commons.redis.utils.RedisUtil;
import org.redisson.api.RedissonClient;

import java.time.Duration;

/**
 * 博客业务常量
 */
public final class BlogConstants {

    /** 工具类，禁止实例化 */
    private BlogConstants() {
    }

    /** 网站名称 */
    public static final String SITE_NAME = "岑兹博客";

    public static final RedissonClient redisson = RedisUtil.getRedisson(DatabaseEnum.DATABASE_3);

    /** 邮箱验证码 Redis 前缀（注册） */
    public static final String EMAIL_CODE_KEY = "blog:email:code:";

    public static final String EMAIL_FROZEN = "blog:email:frozen:";

    public static final Duration FROZEN_TTL = Duration.ofMinutes(1);

    /** 验证码有效期 */
    public static final Duration EMAIL_CODE_TTL = Duration.ofMinutes(5);

    /** 验证码长度 */
    public static final int EMAIL_CODE_LENGTH = 6;

    /*** 文章图片保存地址 **/
    public static final String IMAGE_PATH = "blog/image/";

    public static final String CATEGORY_CACHE = "blog:category";

    /** 黑名单规则快照缓存 */
    public static final String BLACKLIST_CACHE = "blog:blacklist:snapshot";

    /** 黑名单缓存有效期 */
    public static final Duration BLACKLIST_CACHE_TTL = Duration.ofHours(1);
}
