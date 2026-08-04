package com.xcz.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xcz.blog.domain.BlogAdminLog;
import com.xcz.blog.domain.BlogUser;
import com.xcz.blog.domain.dto.UpdateUserRoleDTO;
import com.xcz.blog.domain.dto.UpdateUserStatusDTO;
import com.xcz.blog.domain.enums.RoleStatue;
import com.xcz.blog.domain.enums.UserStatus;
import com.xcz.blog.domain.mongo.Article;
import com.xcz.blog.domain.mongo.Comment;
import com.xcz.blog.domain.vo.AdminLogVO;
import com.xcz.blog.domain.vo.AdminStatsVO;
import com.xcz.blog.domain.vo.AdminUserVO;
import com.xcz.blog.mapper.BlogUserMapper;
import com.xcz.blog.repository.ArticleRepository;
import com.xcz.blog.repository.CommentRepository;
import com.xcz.blog.service.AdminLogService;
import com.xcz.blog.service.AdminService;
import com.xcz.blog.support.UserDisplaySupport;
import com.xcz.commons.core.exception.ServiceException;
import com.xcz.commons.core.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理后台服务实现
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final BlogUserMapper blogUserMapper;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final AdminLogService adminLogService;
    private final UserDisplaySupport userDisplaySupport;

    /**
     * 汇总用户、文章、评论及管理员数量
     */
    @Override
    public AdminStatsVO getStats() {
        long userCount = blogUserMapper.selectCount(new LambdaQueryWrapper<>());
        long articleCount = articleRepository.count();
        long commentCount = commentRepository.count();
        long adminCount = blogUserMapper.selectCount(new LambdaQueryWrapper<BlogUser>()
                .in(BlogUser::getRole, RoleStatue.ADMIN.getValue(), RoleStatue.MASTER.getValue()));
        return AdminStatsVO.builder()
                .userCount(userCount)
                .articleCount(articleCount)
                .commentCount(commentCount)
                .adminCount(adminCount)
                .build();
    }

    /**
     * 分页查询文章，支持按标题或摘要关键词过滤
     */
    @Override
    public org.springframework.data.domain.Page<Article> listArticles(int pageNum, int pageSize, String keyword) {
        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize);
        org.springframework.data.domain.Page<Article> page = articleRepository.findAllByOrderByCreateTimeDesc(pageable);
        if (StringUtils.isNotEmpty(keyword)) {
            String kw = keyword.trim().toLowerCase();
            List<Article> filtered = page.getContent().stream()
                    .filter(article -> containsKeyword(article, kw))
                    .toList();
            userDisplaySupport.enrichArticles(filtered);
            return new PageImpl<>(filtered, pageable, filtered.size());
        }
        userDisplaySupport.enrichArticles(page.getContent());
        return page;
    }

    /**
     * 分页查询评论，支持按内容关键词过滤
     */
    @Override
    public org.springframework.data.domain.Page<Comment> listComments(int pageNum, int pageSize, String keyword) {
        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize);
        org.springframework.data.domain.Page<Comment> page = commentRepository.findAllByOrderByCreateTimeDesc(pageable);
        if (StringUtils.isNotEmpty(keyword)) {
            String kw = keyword.trim().toLowerCase();
            List<Comment> filtered = page.getContent().stream()
                    .filter(comment -> comment.getContent() != null
                            && comment.getContent().toLowerCase().contains(kw))
                    .toList();
            userDisplaySupport.enrichComments(filtered);
            return new PageImpl<>(filtered, pageable, filtered.size());
        }
        userDisplaySupport.enrichComments(page.getContent());
        return page;
    }

    /**
     * 分页查询用户，支持按昵称或邮箱模糊搜索
     */
    @Override
    public org.springframework.data.domain.Page<AdminUserVO> listUsers(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<BlogUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(BlogUser::getNickName, kw).or().like(BlogUser::getEmail, kw));
        }
        wrapper.orderByDesc(BlogUser::getCreateTime);
        Page<BlogUser> page = blogUserMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<AdminUserVO> records = page.getRecords().stream()
                .map(this::toAdminUserVO)
                .toList();
        return new PageImpl<>(records, PageRequest.of(Math.max(pageNum - 1, 0), pageSize), page.getTotal());
    }

    /**
     * 修改目标用户角色并记录操作日志
     */
    @Override
    public void updateUserRole(Long operatorId, Long userId, UpdateUserRoleDTO dto) {
        BlogUser operator = requireMaster(operatorId);
        BlogUser target = blogUserMapper.selectById(userId);
        if (target == null) {
            throw new ServiceException("用户不存在");
        }
        RoleStatue newRole = RoleStatue.getRoleStatus(dto.getRole());
        if (newRole == RoleStatue.MASTER && !RoleStatue.isMaster(operator.getRole())) {
            throw new ServiceException("仅超级管理员可授予超级管理员权限");
        }
        if (operatorId.equals(userId) && newRole != RoleStatue.MASTER) {
            throw new ServiceException("不能降低自己的权限");
        }
        RoleStatue oldRole = RoleStatue.getRoleStatus(target.getRole());
        target.setRole(newRole.getValue());
        blogUserMapper.updateById(target);
        adminLogService.record(operatorId, "用户", "修改角色",
                String.valueOf(userId),
                String.format("将用户 %s 的角色从 %s 修改为 %s", target.getNickName(), oldRole.name(), newRole.name()));
    }

    /**
     * 修改目标用户状态并记录操作日志
     */
    @Override
    public void updateUserStatus(Long operatorId, Long userId, UpdateUserStatusDTO dto) {
        BlogUser operator = requireAdmin(operatorId);
        BlogUser target = blogUserMapper.selectById(userId);
        if (target == null) {
            throw new ServiceException("用户不存在");
        }
        if (operatorId.equals(userId)) {
            throw new ServiceException("不能修改自己的账号状态");
        }
        if (RoleStatue.isMaster(target.getRole()) && !RoleStatue.isMaster(operator.getRole())) {
            throw new ServiceException("没有权限修改超级管理员的状态");
        }
        String newStatus = dto.getStatus();
        if (!UserStatus.NORMAL.getCode().equals(newStatus) && !UserStatus.DISABLED.getCode().equals(newStatus)) {
            throw new ServiceException("状态值不合法");
        }
        String oldStatus = target.getStatus();
        if (newStatus.equals(oldStatus)) {
            return;
        }
        target.setStatus(newStatus);
        blogUserMapper.updateById(target);
        adminLogService.record(operatorId, "用户", "修改状态",
                String.valueOf(userId),
                String.format("将用户 %s 的状态从 %s 修改为 %s",
                        target.getNickName(), statusLabel(oldStatus), statusLabel(newStatus)));
    }

    /**
     * 分页查询操作日志，并根据 operatorId 批量补全操作人昵称与角色
     */
    @Override
    public org.springframework.data.domain.Page<AdminLogVO> listLogs(int pageNum, int pageSize) {
        Page<BlogAdminLog> page = adminLogService.page(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BlogAdminLog>().orderByDesc(BlogAdminLog::getCreateTime));
        Map<Long, BlogUser> operatorMap = loadOperatorMap(page.getRecords());
        List<AdminLogVO> records = page.getRecords().stream()
                .map(log -> toAdminLogVO(log, operatorMap.get(log.getOperatorId())))
                .toList();
        return new PageImpl<>(records, PageRequest.of(Math.max(pageNum - 1, 0), pageSize), page.getTotal());
    }

    /**
     * 校验当前用户是否为 MASTER
     */
    private BlogUser requireMaster(Long operatorId) {
        BlogUser operator = requireAdmin(operatorId);
        if (!RoleStatue.isMaster(operator.getRole())) {
            throw new ServiceException("仅超级管理员可执行此操作");
        }
        return operator;
    }

    /**
     * 校验当前用户是否为管理员
     */
    private BlogUser requireAdmin(Long operatorId) {
        BlogUser operator = blogUserMapper.selectById(operatorId);
        if (operator == null) {
            throw new ServiceException("用户不存在");
        }
        if (!RoleStatue.isCanPublish(operator.getRole())) {
            throw new ServiceException("没有权限执行此操作");
        }
        return operator;
    }

    /**
     * 获取状态展示名称
     */
    private String statusLabel(String status) {
        return UserStatus.DISABLED.getCode().equals(status)
                ? UserStatus.DISABLED.getLabel()
                : UserStatus.NORMAL.getLabel();
    }

    /**
     * 批量加载日志操作人信息
     */
    private Map<Long, BlogUser> loadOperatorMap(List<BlogAdminLog> logs) {
        Set<Long> operatorIds = logs.stream()
                .map(BlogAdminLog::getOperatorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (operatorIds.isEmpty()) {
            return Map.of();
        }
        return blogUserMapper.selectBatchIds(operatorIds).stream()
                .collect(Collectors.toMap(BlogUser::getUserId, user -> user, (left, right) -> left));
    }

    /**
     * 将日志实体转换为展示对象，并补全操作人信息
     */
    private AdminLogVO toAdminLogVO(BlogAdminLog log, BlogUser operator) {
        String operatorName = operator != null ? operator.getNickName() : "未知用户";
        String operatorRole = operator != null
                ? RoleStatue.getRoleStatus(operator.getRole()).name()
                : "UNKNOWN";
        return AdminLogVO.builder()
                .logId(log.getLogId())
                .operatorId(log.getOperatorId())
                .operatorName(operatorName)
                .operatorRole(operatorRole)
                .module(log.getModule())
                .operation(log.getOperation())
                .targetId(log.getTargetId())
                .detail(log.getDetail())
                .createTime(log.getCreateTime())
                .build();
    }

    /**
     * 将用户实体转换为管理后台用户展示对象
     */
    private AdminUserVO toAdminUserVO(BlogUser user) {
        return AdminUserVO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickName(user.getNickName())
                .role(RoleStatue.getRoleStatus(user.getRole()).name())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .build();
    }

    /**
     * 判断文章标题或摘要是否包含关键词
     */
    private boolean containsKeyword(Article article, String keyword) {
        return (article.getTitle() != null && article.getTitle().toLowerCase().contains(keyword))
                || (article.getSummary() != null && article.getSummary().toLowerCase().contains(keyword));
    }
}
