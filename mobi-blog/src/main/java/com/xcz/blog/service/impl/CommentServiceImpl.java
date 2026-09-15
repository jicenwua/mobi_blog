package com.xcz.blog.service.impl;

import com.xcz.blog.domain.BlogUser;
import com.xcz.blog.domain.dto.CommentDTO;
import com.xcz.blog.domain.enums.CommentSortType;
import com.xcz.blog.domain.enums.RoleStatue;
import com.xcz.blog.domain.mongo.Comment;
import com.xcz.blog.domain.vo.CommentVO;
import com.xcz.blog.mapper.BlogUserMapper;
import com.xcz.blog.repository.CommentRepository;
import com.xcz.blog.service.AdminLogService;
import com.xcz.blog.service.BlackContentService;
import com.xcz.blog.service.CommentService;
import com.xcz.blog.support.ArticleMongoSupport;
import com.xcz.blog.support.UserDisplaySupport;
import com.xcz.commons.core.exception.ServiceException;
import com.xcz.commons.core.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文章评论服务。
 * <p>
 * 评论结构为两层：顶级评论（parentId 为空）与子评论（parentId 指向顶级评论），
 * 不支持回复子评论。评论数据存 MongoDB，文章 commentCount 存于 MongoDB 文章文档。
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    /** 列表页每条父评论默认展示的子评论预览条数 */
    private static final int CHILD_PREVIEW_LIMIT = 3;

    private final CommentRepository commentRepository;
    private final BlogUserMapper blogUserMapper;
    private final ArticleMongoSupport articleMongoSupport;
    private final UserDisplaySupport userDisplaySupport;
    private final AdminLogService adminLogService;
    private final BlackContentService blackContentService;

    /**
     * 发表评论或回复顶级评论。
     *
     * @param userId 当前用户 ID
     * @param dto    评论内容（articleId 必填，parentId 为空表示顶级评论）
     * @return 新评论 ID
     */
    @Override
    public String addComment(Long userId, CommentDTO dto) {
        requireUser(userId);
        articleMongoSupport.requireArticle(dto.getArticleId());
        // 违禁词正则校验（IP / 用户由全局过滤器拦截）
        blackContentService.assertContentNotBlocked(dto.getContent());

        String parentId = normalizeParentId(dto.getParentId());
        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new ServiceException("父评论不存在"));
            if (!dto.getArticleId().equals(parent.getArticleId())) {
                throw new ServiceException("父评论不属于该文章");
            }
            // 父评论本身已是子评论时，拒绝继续嵌套
            if (StringUtils.isNotEmpty(parent.getParentId())) {
                throw new ServiceException("仅支持两层评论，无法回复子评论");
            }
        }

        Comment comment = Comment.builder()
                .articleId(dto.getArticleId())
                .userId(userId)
                .content(dto.getContent())
                .parentId(parentId)
                .likeCount(0)
                .createTime(LocalDateTime.now())
                .build();
        String commentId = commentRepository.save(comment).getId();
        // 同步更新文章文档中的评论计数
        articleMongoSupport.adjustCommentCount(dto.getArticleId(), 1);
        return commentId;
    }

    /**
     * 删除评论。
     * <p>
     * 普通用户只能删自己的评论；管理员/作者可删任意评论。
     * 删除顶级评论时会级联删除其全部子评论，并按实际删除条数扣减文章 commentCount。
     *
     * @param userId    当前用户 ID
     * @param commentId 评论 ID
     */
    @Override
    public void deleteComment(Long userId, String commentId) {
        BlogUser blogUser = blogUserMapper.selectById(userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException("评论不存在"));

        if (!RoleStatue.isCanPublish(blogUser.getRole()) && !userId.equals(comment.getUserId())) {
            throw new ServiceException("无权删除该评论");
        }

        long deleteCount = 1;
        if (StringUtils.isEmpty(comment.getParentId())) {
            long childCount = commentRepository.countByParentId(commentId);
            commentRepository.deleteByParentId(commentId);
            deleteCount += childCount;
        }
        commentRepository.deleteById(commentId);
        articleMongoSupport.adjustCommentCount(comment.getArticleId(), -deleteCount);
        if (RoleStatue.isCanPublish(blogUser.getRole())) {
            adminLogService.record(userId, "评论", "删除评论", commentId,
                    String.format("删除用户：%s评论：%s", comment.getUserId(),truncate(comment.getContent(), 50)));
        }
    }

    /**
     * 截断文本，用于管理员操作日志摘要。
     */
    private String truncate(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }

    /**
     * 分页查询文章下的顶级评论，并附带每条评论的子评论预览。
     *
     * @param articleId 文章 ID
     * @param sort      排序方式（最新 / 最热）
     * @param pageNum   页码（从 1 开始）
     * @param pageSize  每页条数
     * @return 父评论分页，含子评论预览与总数
     */
    @Override
    public Page<CommentVO> listComments(String articleId, CommentSortType sort, int pageNum, int pageSize) {
        articleMongoSupport.requireArticle(articleId);
        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize);

        // 仅分页查顶级评论；子评论单独批量加载，避免 N+1
        Page<Comment> parentPage = CommentSortType.HOT.equals(sort)
                ? commentRepository.findByArticleIdAndParentIdIsNullOrderByLikeCountDescCreateTimeDesc(articleId, pageable)
                : commentRepository.findByArticleIdAndParentIdIsNullOrderByCreateTimeDesc(articleId, pageable);

        List<String> parentIds = parentPage.getContent().stream().map(Comment::getId).toList();
        if (parentIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, parentPage.getTotalElements());
        }

        Map<String, List<Comment>> childrenByParent = commentRepository.findByParentIdIn(parentIds).stream()
                .collect(Collectors.groupingBy(Comment::getParentId));

        List<Comment> allChildren = childrenByParent.values().stream()
                .flatMap(List::stream)
                .toList();
        userDisplaySupport.enrichComments(parentPage.getContent());
        userDisplaySupport.enrichComments(allChildren);

        List<CommentVO> result = parentPage.getContent().stream()
                .map(parent -> {
                    List<Comment> children = childrenByParent.getOrDefault(parent.getId(), List.of());
                    // 预览区按点赞数优先展示，完整列表通过 listReplies 分页拉取
                    List<Comment> previewChildren = children.stream()
                            .sorted(childLikeComparator())
                            .limit(CHILD_PREVIEW_LIMIT)
                            .toList();
                    return CommentVO.from(parent, previewChildren, children.size());
                })
                .toList();

        return new PageImpl<>(result, pageable, parentPage.getTotalElements());
    }

    /**
     * 分页查询某条顶级评论下的全部子评论（按时间正序）。
     *
     * @param parentId 父评论 ID（必须是顶级评论）
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @return 子评论分页
     */
    @Override
    public Page<Comment> listReplies(String parentId, int pageNum, int pageSize) {
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new ServiceException("父评论不存在"));
        if (StringUtils.isNotEmpty(parent.getParentId())) {
            throw new ServiceException("仅支持查询父评论下的子评论");
        }

        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize);
        Page<Comment> replyPage = commentRepository.findByParentIdOrderByCreateTimeAsc(parentId, pageable);
        userDisplaySupport.enrichComments(replyPage.getContent());
        return replyPage;
    }

    /**
     * 校验用户存在。
     */
    private BlogUser requireUser(Long userId) {
        BlogUser user = blogUserMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        return user;
    }

    /**
     * 将空 parentId 规范为 null，表示顶级评论。
     */
    private String normalizeParentId(String parentId) {
        return StringUtils.isEmpty(parentId) ? null : parentId;
    }

    /**
     * 子评论预览排序：点赞数降序，相同点赞按发布时间升序。
     */
    private Comparator<Comment> childLikeComparator() {
        return Comparator
                .comparing(Comment::getLikeCount, Comparator.nullsFirst(Comparator.reverseOrder()))
                .thenComparing(Comment::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder()));
    }
}
