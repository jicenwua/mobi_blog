package com.xcz.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xcz.blog.domain.BlogAdminLog;

/**
 * 管理员操作日志服务
 */
public interface AdminLogService extends IService<BlogAdminLog> {

    /**
     * 异步记录管理员操作日志
     * <p>
     * 仅当操作人存在且具备发布权限（ADMIN / MASTER）时写入。
     *
     * @param operatorId 操作人用户 ID
     * @param module     操作模块，如「文章」「评论」「用户」
     * @param operation  操作类型，如「发布文章」「删除评论」
     * @param targetId   操作目标 ID，可为空
     * @param detail     操作详情描述
     */
    void record(Long operatorId, String module, String operation, String targetId, String detail);
}
