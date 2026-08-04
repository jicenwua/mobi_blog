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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final int CHILD_PREVIEW_LIMIT = 3;

    private final CommentRepository commentRepository;
    private final BlogUserMapper blogUserMapper;
    private final ArticleMongoSupport articleMongoSupport;
    private final UserDisplaySupport userDisplaySupport;
    private final AdminLogService adminLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addComment(Long userId, CommentDTO dto) {
        requireUser(userId);
        articleMongoSupport.requireArticle(dto.getArticleId());

        String parentId = normalizeParentId(dto.getParentId());
        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new ServiceException("父评论不存在"));
            if (!dto.getArticleId().equals(parent.getArticleId())) {
                throw new ServiceException("父评论不属于该文章");
            }
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
        articleMongoSupport.adjustCommentCount(dto.getArticleId(), 1);
        return commentId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
                    String.format("删除评论：%s", truncate(comment.getContent(), 50)));
        }
    }

    private String truncate(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }

    @Override
    public Page<CommentVO> listComments(String articleId, CommentSortType sort, int pageNum, int pageSize) {
        articleMongoSupport.requireArticle(articleId);
        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize);

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
                    List<Comment> previewChildren = children.stream()
                            .sorted(childLikeComparator())
                            .limit(CHILD_PREVIEW_LIMIT)
                            .toList();
                    return CommentVO.from(parent, previewChildren, children.size());
                })
                .toList();

        return new PageImpl<>(result, pageable, parentPage.getTotalElements());
    }

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

    private BlogUser requireUser(Long userId) {
        BlogUser user = blogUserMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        return user;
    }

    private String normalizeParentId(String parentId) {
        return StringUtils.isEmpty(parentId) ? null : parentId;
    }

    private Comparator<Comment> childLikeComparator() {
        return Comparator
                .comparing(Comment::getLikeCount, Comparator.nullsFirst(Comparator.reverseOrder()))
                .thenComparing(Comment::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder()));
    }
}
