package com.xcz.blog.service;

import com.xcz.blog.domain.dto.ArticleDTO;
import com.xcz.blog.domain.dto.ArticleDraftDTO;
import com.xcz.blog.domain.mongo.Article;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文章服务
 */
public interface ArticleService {

    /**
     * 创建文章
     *
     * @param authorId 作者用户 ID
     * @param dto      文章信息
     * @return 文章 ID
     */
    String createArticle(Long authorId, ArticleDTO dto);

    /**
     * 更新文章
     *
     * @param authorId  作者用户 ID
     * @param articleId 文章 ID
     * @param dto       文章信息
     */
    void updateArticle(Long authorId, String articleId, ArticleDTO dto);

    /**
     * 删除文章
     *
     * @param authorId  作者用户 ID
     * @param articleId 文章 ID
     */
    void deleteArticle(Long authorId, String articleId);

    /**
     * 根据 ID 查询文章
     *
     * @param articleId 文章 ID
     * @return 文章详情
     */
    Article getArticleById(String articleId);

    /**
     * 文章浏览量 +1
     *
     * @param articleId 文章 ID
     */
    void incrementViewCount(String articleId);

    /**
     * 分页查询已发布文章
     *
     * @param category 分类（可选）
     * @param tag      标签（可选）
     * @param keyword  搜索关键词（可选，匹配标题与正文）
     * @param isHot    是否按浏览量排序（热门文章）
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 文章分页结果
     */
    Page<Article> listArticles(String category, String tag, String keyword, boolean isHot, String sortBy,
                                 int pageNum, int pageSize);

    /**
     * 分页查询当前用户发布的文章
     *
     * @param authorId 作者用户 ID
     * @param sortBy   排序方式：time 按发布时间，view 按浏览量
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 文章分页结果
     */
    Page<Article> listMyArticles(Long authorId, String sortBy, int pageNum, int pageSize);

    /**
     * 查询热门标签
     *
     * @param limit 返回条数
     * @return 热门标签列表
     */
    List<Map<String, Object>> listHotTags(int limit);

    /**
     * 查询全部标签（按名称排序）
     *
     * @return 全部标签列表
     */
    List<Map<String, Object>> listAllTags();

    /**
     *  保存文章图片
     * @param images    图片
     * @return          保存地址
     */
    List<String> upload(List<MultipartFile> images);

    void deleteImage(String imagePath);

    /**
     * 查询当前用户的文章草稿
     */
    Article getMyDraft(Long authorId);

    /**
     * 保存当前用户的文章草稿（存在则更新，否则新建）
     */
    Article saveDraft(Long authorId, ArticleDraftDTO dto);

    /**
     * 删除当前用户的文章草稿
     */
    void deleteMyDraft(Long authorId);
}
