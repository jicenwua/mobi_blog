package com.xcz.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcz.blog.domain.BlogAdminLog;
import com.xcz.blog.domain.BlogUser;
import com.xcz.blog.domain.enums.RoleStatue;
import com.xcz.blog.mapper.BlogAdminLogMapper;
import com.xcz.blog.mapper.BlogUserMapper;
import com.xcz.blog.service.AdminLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 管理员操作日志服务实现
 */
@Service
@RequiredArgsConstructor
public class AdminLogServiceImpl extends ServiceImpl<BlogAdminLogMapper, BlogAdminLog> implements AdminLogService {

    private final BlogUserMapper blogUserMapper;

    /**
     * 异步写入管理员操作日志，仅持久化操作人 ID 与操作内容。
     */
    @Override
    @Async("blogThread")
    public void record(Long operatorId, String module, String operation, String targetId, String detail) {
        BlogUser operator = blogUserMapper.selectById(operatorId);
        if (operator == null || !RoleStatue.isCanPublish(operator.getRole())) {
            return;
        }
        BlogAdminLog log = BlogAdminLog.builder()
                .operatorId(operatorId)
                .module(module)
                .operation(operation)
                .targetId(targetId)
                .detail(detail)
                .createTime(LocalDateTime.now())
                .build();
        save(log);
    }
}
