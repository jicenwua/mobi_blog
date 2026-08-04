package com.xcz.blog.service;

import com.xcz.blog.domain.dto.CommentDTO;
import com.xcz.blog.domain.enums.CommentSortType;
import com.xcz.blog.domain.mongo.Comment;
import com.xcz.blog.domain.vo.CommentVO;
import org.springframework.data.domain.Page;

/**
 * 评论服务
 */
public interface CommentService {

    /**
     * 发表评论
     *
     * @param userId 用户 ID
     * @param dto    评论信息
     * @return 评论 ID
     */
    String addComment(Long userId, CommentDTO dto);

    /**
     * 删除评论
     *
     * @param userId    用户 ID
     * @param commentId 评论 ID
     */
    void deleteComment(Long userId, String commentId);

    /**
     * 分页查询文章父评论（含子评论预览）
     *
     * @param articleId 文章 ID
     * @param sort      排序方式
     * @param pageNum   页码
     * @param pageSize  每页条数
     * @return 父评论分页结果
     */
    Page<CommentVO> listComments(String articleId, CommentSortType sort, int pageNum, int pageSize);

    /**
     * 分页查询子评论（按发布时间正序）
     *
     * @param parentId 父评论 ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 子评论分页结果
     */
    Page<Comment> listReplies(String parentId, int pageNum, int pageSize);
}
