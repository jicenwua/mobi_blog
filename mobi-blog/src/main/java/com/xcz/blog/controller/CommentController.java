package com.xcz.blog.controller;

import com.xcz.blog.domain.dto.CommentDTO;
import com.xcz.blog.domain.enums.CommentSortType;
import com.xcz.blog.domain.mongo.Comment;
import com.xcz.blog.domain.vo.CommentVO;
import com.xcz.blog.service.CommentService;
import com.xcz.commons.core.domain.ResponseEntity;
import com.xcz.commons.core.utils.response.ResponseEntityUtils;
import com.xcz.commons.security.annotation.Release;
import com.xcz.commons.security.utils.SecurityUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 评论接口
 */
@RestController
@RequestMapping("/blog/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    /**
     * 发表评论
     *
     * @param dto 评论信息
     * @return 评论 ID
     */
    @PostMapping
    public ResponseEntity<String> addComment(@Valid @RequestBody CommentDTO dto) {
        String commentId = commentService.addComment(SecurityUtils.getUserId(), dto);
        return ResponseEntityUtils.ok(commentId);
    }

    /**
     * 删除评论
     *
     * @param commentId 评论 ID
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable String commentId) {
        commentService.deleteComment(SecurityUtils.getUserId(), commentId);
        return ResponseEntityUtils.ok();
    }

    /**
     * 分页查询文章父评论
     *
     * @param articleId 文章 ID
     * @param sort      排序方式：HOT 按点赞数，TIME 按发布时间
     * @param pageNum   页码
     * @param pageSize  每页条数
     * @return 父评论分页结果（含点赞最高的 3 条子评论预览）
     */
    @Release
    @GetMapping("/list")
    public ResponseEntity<Page<CommentVO>> listComments(@RequestParam String articleId,
                                                        @RequestParam(defaultValue = "TIME") CommentSortType sort,
                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntityUtils.ok(commentService.listComments(articleId, sort, pageNum, pageSize));
    }

    /**
     * 分页查询子评论（展开全部）
     *
     * @param parentId 父评论 ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 子评论分页结果（按发布时间正序）
     */
    @Release
    @GetMapping("/{parentId}/replies")
    public ResponseEntity<Page<Comment>> listReplies(@PathVariable String parentId,
                                                     @RequestParam(defaultValue = "1") int pageNum,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntityUtils.ok(commentService.listReplies(parentId, pageNum, pageSize));
    }
}
