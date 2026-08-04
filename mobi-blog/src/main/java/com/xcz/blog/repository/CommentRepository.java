package com.xcz.blog.repository;

import com.xcz.blog.domain.mongo.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

/**
 * 评论 MongoDB Repository
 */
public interface CommentRepository extends MongoRepository<Comment, String> {

    /**
     * 按文章 ID 分页查询父评论，按创建时间倒序
     */
    Page<Comment> findByArticleIdAndParentIdIsNullOrderByCreateTimeDesc(String articleId, Pageable pageable);

    /**
     * 按文章 ID 分页查询父评论，按点赞数倒序、创建时间倒序
     */
    Page<Comment> findByArticleIdAndParentIdIsNullOrderByLikeCountDescCreateTimeDesc(String articleId, Pageable pageable);

    /**
     * 批量查询子评论
     */
    List<Comment> findByParentIdIn(Collection<String> parentIds);

    /**
     * 分页查询子评论，按发布时间正序
     */
    Page<Comment> findByParentIdOrderByCreateTimeAsc(String parentId, Pageable pageable);

    /**
     * 统计子评论数量
     */
    long countByParentId(String parentId);

    /**
     * 删除指定父评论下的所有子评论
     */
    void deleteByParentId(String parentId);

    /**
     * 统计文章评论数量
     */
    long countByArticleId(String articleId);

    /**
     * 分页查询全部评论，按创建时间倒序
     */
    Page<Comment> findAllByOrderByCreateTimeDesc(Pageable pageable);
}
