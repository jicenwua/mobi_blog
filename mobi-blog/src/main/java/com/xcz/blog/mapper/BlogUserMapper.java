package com.xcz.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xcz.blog.domain.BlogUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 博客用户 Mapper
 */
@Mapper
public interface BlogUserMapper extends BaseMapper<BlogUser> {

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息，不存在时返回 null
     */
    BlogUser selectUserByEmail(@Param("email") String email);
}
