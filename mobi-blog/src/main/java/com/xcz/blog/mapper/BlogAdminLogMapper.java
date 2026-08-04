package com.xcz.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xcz.blog.domain.BlogAdminLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员操作日志 Mapper
 */
@Mapper
public interface BlogAdminLogMapper extends BaseMapper<BlogAdminLog> {
}
