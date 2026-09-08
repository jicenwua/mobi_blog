package com.xcz.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xcz.blog.domain.BlackContent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 黑名单配置 Mapper。
 * <p>
 * 对应表 {@code blog_black_content}，基础 CRUD 由 MyBatis-Plus 提供。
 */
@Mapper
public interface BlackContentMapper extends BaseMapper<BlackContent> {
}
