package com.xcz.blog.repository;

import com.xcz.blog.domain.mongo.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * 文章 MongoDB Repository
 */
public interface ArticleRepository extends MongoRepository<Article, String> {

    /**
     * 按状态分页查询文章，按创建时间倒序
     *
     * @param status   发布状态
     * @param pageable 分页参数
     * @return 文章分页结果
     */
    Page<Article> findByStatusOrderByCreateTimeDesc(String status, Pageable pageable);

    /**
     * 按状态和分类分页查询文章，按创建时间倒序
     *
     * @param status   发布状态
     * @param category 分类编码
     * @param pageable 分页参数
     * @return 文章分页结果
     */
    Page<Article> findByStatusAndCategoryOrderByCreateTimeDesc(String status, String category, Pageable pageable);

    /**
     * 按状态和标签分页查询文章，按创建时间倒序
     *
     * @param status   发布状态
     * @param tag      标签
     * @param pageable 分页参数
     * @return 文章分页结果
     */
    Page<Article> findByStatusAndTagsContainingOrderByCreateTimeDesc(String status, String tag, Pageable pageable);

    /**
     * 按状态和标签分页查询文章，按浏览量倒序
     */
    Page<Article> findByStatusAndTagsContainingOrderByViewCountDesc(String status, String tag, Pageable pageable);

    /**
     * 按状态、分类和标签分页查询文章，按创建时间倒序
     *
     * @param status   发布状态
     * @param category 分类编码
     * @param tag      标签
     * @param pageable 分页参数
     * @return 文章分页结果
     */
    Page<Article> findByStatusAndCategoryAndTagsContainingOrderByCreateTimeDesc(String status, String category,
                                                                                String tag, Pageable pageable);

    /**
     * 按状态分页查询热门文章，按浏览量倒序
     *
     * @param status   发布状态
     * @param pageable 分页参数
     * @return 按浏览量排序的文章分页结果
     */
    Page<Article> findByStatusOrderByViewCountDesc(String status, Pageable pageable);

    /**
     * 分页查询全部文章，按创建时间倒序
     */
    Page<Article> findAllByOrderByCreateTimeDesc(Pageable pageable);

    /**
     * 按作者分页查询文章，按创建时间倒序
     */
    Page<Article> findByAuthorIdOrderByCreateTimeDesc(Long authorId, Pageable pageable);

    /**
     * 按作者分页查询文章，按浏览量倒序
     */
    Page<Article> findByAuthorIdOrderByViewCountDesc(Long authorId, Pageable pageable);

    /**
     * 查询作者指定状态的草稿（每位作者仅保留一份草稿）
     */
    Optional<Article> findByAuthorIdAndStatus(Long authorId, String status);
}
