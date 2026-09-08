package com.xcz.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xcz.blog.domain.BlackContent;
import com.xcz.blog.domain.dto.BlackContentDTO;
import com.xcz.blog.mapper.BlackContentMapper;
import com.xcz.blog.mapper.BlogUserMapper;
import com.xcz.blog.service.AdminLogService;
import com.xcz.blog.service.BlackContentService;
import com.xcz.blog.support.BlackContentSupport;
import com.xcz.commons.core.exception.ServiceException;
import com.xcz.commons.core.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 黑名单配置服务实现。
 */
@Service
@RequiredArgsConstructor
public class BlackContentServiceImpl extends ServiceImpl<BlackContentMapper, BlackContent>
        implements BlackContentService {

    private final BlogUserMapper blogUserMapper;
    private final AdminLogService adminLogService;
    private final BlackContentSupport blackContentSupport;

    /**
     * 新增黑名单规则，写入后刷新缓存。
     */
    @Override
    public Long addBlackContent(Long operatorId, BlackContentDTO dto) {
        String blackIp = StringUtils.trimToEmpty(dto.getBlackIp());
        Long blackUserId = dto.getBlackUserId();
        String blackContent = StringUtils.trimToEmpty(dto.getBlackContent());
        if (StringUtils.isEmpty(blackIp) && blackUserId == null && StringUtils.isEmpty(blackContent)) {
            throw new ServiceException("请至少配置 IP、用户 ID 或关键词之一");
        }
        if (blackUserId != null && blogUserMapper.selectById(blackUserId) == null) {
            throw new ServiceException("黑名单用户不存在");
        }
        try {
            blackContentSupport.validateContentPattern(blackContent);
        } catch (IllegalArgumentException ex) {
            throw new ServiceException(ex.getMessage());
        }
        assertNotDuplicate(blackIp, blackUserId, blackContent);

        BlackContent entity = BlackContent.builder()
                .blackIp(StringUtils.isEmpty(blackIp) ? null : blackIp)
                .blackUserId(blackUserId)
                .blackContent(StringUtils.isEmpty(blackContent) ? null : blackContent)
                .createTime(LocalDateTime.now())
                .build();
        save(entity);
        blackContentSupport.refreshCache();

        adminLogService.record(operatorId, "黑名单", "新增规则", String.valueOf(entity.getId()),
                buildDetail(entity));
        return entity.getId();
    }

    /**
     * 删除黑名单规则，删除后刷新缓存。
     */
    @Override
    public void removeBlackContent(Long operatorId, Long id) {
        BlackContent entity = getById(id);
        if (entity == null) {
            throw new ServiceException("黑名单规则不存在");
        }
        removeById(id);
        blackContentSupport.refreshCache();
        adminLogService.record(operatorId, "黑名单", "删除规则", String.valueOf(id),
                buildDetail(entity));
    }

    /**
     * 分页查询黑名单列表，按创建时间倒序。
     * <p>
     * MyBatis-Plus 分页结果会转换为 Spring Data {@link org.springframework.data.domain.Page} 返回前端。
     */
    @Override
    public org.springframework.data.domain.Page<BlackContent> listBlackContents(int pageNum, int pageSize) {
        Page<BlackContent> page = page(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BlackContent>().orderByDesc(BlackContent::getCreateTime));
        return new PageImpl<>(page.getRecords(), PageRequest.of(Math.max(pageNum - 1, 0), pageSize), page.getTotal());
    }

    /**
     * 判断请求是否应被全局过滤器拦截（命中 IP 或用户黑名单）。
     */
    @Override
    public boolean isRequestBlocked(String ip, Long userId) {
        return blackContentSupport.isIpBlocked(ip) || blackContentSupport.isUserBlocked(userId);
    }

    /**
     * 校验文本是否命中违禁词正则，命中时抛出 {@link ServiceException}。
     */
    @Override
    public void assertContentNotBlocked(String content) {
        if (blackContentSupport.matchesContent(content)) {
            throw new ServiceException("触发了黑名单，请修改后重试");
        }
    }

    /**
     * 校验 IP / 用户 / 关键词是否已存在，避免重复配置。
     */
    private void assertNotDuplicate(String blackIp, Long blackUserId, String blackContent) {
        if (StringUtils.isNotEmpty(blackIp)
                && count(new LambdaQueryWrapper<BlackContent>().eq(BlackContent::getBlackIp, blackIp)) > 0) {
            throw new ServiceException("该 IP 已在黑名单中");
        }
        if (blackUserId != null
                && count(new LambdaQueryWrapper<BlackContent>().eq(BlackContent::getBlackUserId, blackUserId)) > 0) {
            throw new ServiceException("该用户已在黑名单中");
        }
        if (StringUtils.isNotEmpty(blackContent)
                && count(new LambdaQueryWrapper<BlackContent>()
                .eq(BlackContent::getBlackContent, blackContent)) > 0) {
            throw new ServiceException("该关键词已在黑名单中");
        }
    }

    /**
     * 拼接规则摘要，用于管理员操作日志。
     */
    private String buildDetail(BlackContent entity) {
        StringBuilder detail = new StringBuilder();
        if (StringUtils.isNotEmpty(entity.getBlackIp())) {
            detail.append("IP=").append(entity.getBlackIp()).append(' ');
        }
        if (entity.getBlackUserId() != null) {
            detail.append("用户ID=").append(entity.getBlackUserId()).append(' ');
        }
        if (StringUtils.isNotEmpty(entity.getBlackContent())) {
            detail.append("关键词=").append(entity.getBlackContent());
        }
        return detail.toString().trim();
    }
}
