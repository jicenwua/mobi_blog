package com.xcz.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xcz.blog.domain.BlackContent;
import com.xcz.blog.domain.dto.BlackContentDTO;
import org.springframework.data.domain.Page;

/**
 * 黑名单配置服务。
 * <p>
 * 提供黑名单规则的增删查、全局 IP/用户拦截，以及业务侧违禁词校验。
 */
public interface BlackContentService extends IService<BlackContent> {

    /**
     * 新增黑名单规则（管理员）。
     *
     * @param operatorId 操作人用户 ID
     * @param dto        黑名单配置
     * @return 规则 ID
     */
    Long addBlackContent(Long operatorId, BlackContentDTO dto);

    /**
     * 删除黑名单规则（管理员）。
     *
     * @param operatorId 操作人用户 ID
     * @param id         规则 ID
     */
    void removeBlackContent(Long operatorId, Long id);

    /**
     * 分页查询黑名单列表，按创建时间倒序。
     *
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @return 黑名单分页
     */
    Page<BlackContent> listBlackContents(int pageNum, int pageSize);

    /**
     * 判断请求是否命中 IP 或用户黑名单（供全局过滤器使用）。
     *
     * @param ip     客户端 IP
     * @param userId 用户 ID，未登录可为 null
     * @return 是否应拦截
     */
    boolean isRequestBlocked(String ip, Long userId);

    /**
     * 校验文本是否命中违禁词正则，命中时抛出业务异常。
     * <p>
     * 用于评论、文章等需要检查内容的写操作。
     *
     * @param content 待校验文本
     */
    void assertContentNotBlocked(String content);
}
