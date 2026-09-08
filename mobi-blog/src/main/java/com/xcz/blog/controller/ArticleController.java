package com.xcz.blog.controller;

import com.xcz.blog.domain.dto.ArticleDTO;
import com.xcz.blog.domain.dto.ArticleDraftDTO;
import com.xcz.blog.domain.mongo.Article;
import com.xcz.blog.service.ArticleService;
import com.xcz.blog.support.ArticleMongoSupport;
import com.xcz.commons.core.domain.ResponseEntity;
import com.xcz.commons.core.utils.response.ResponseEntityUtils;
import com.xcz.commons.security.annotation.Release;
import com.xcz.commons.security.utils.SecurityUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文章接口
 */
@RestController
@RequestMapping("/blog/article")
public class ArticleController {

    @Resource
    private ArticleService articleService;
    @Resource
    private ArticleMongoSupport articleMongoSupport;

    /**
     * 创建文章
     *
     * @param dto 文章信息
     * @return 文章 ID
     */
    @PostMapping
    @PreAuthorize("@ss.hasAnyRole('MASTER,ADMIN')")
    public ResponseEntity<String> createArticle(@Valid @RequestBody ArticleDTO dto) {
        String articleId = articleService.createArticle(SecurityUtils.getUserId(), dto);
        return ResponseEntityUtils.ok(articleId);
    }

    /**
     * 查询当前用户的文章草稿
     */
    @GetMapping("/draft")
    @PreAuthorize("@ss.hasAnyRole('MASTER,ADMIN')")
    public ResponseEntity<Article> getMyDraft() {
        return ResponseEntityUtils.ok(articleService.getMyDraft(SecurityUtils.getUserId()));
    }

    /**
     * 保存当前用户的文章草稿
     */
    @PutMapping("/draft")
    @PreAuthorize("@ss.hasAnyRole('MASTER,ADMIN')")
    public ResponseEntity<Article> saveDraft(@Valid @RequestBody ArticleDraftDTO dto) {
        return ResponseEntityUtils.ok(articleService.saveDraft(SecurityUtils.getUserId(), dto));
    }

    /**
     * 删除当前用户的文章草稿
     */
    @DeleteMapping("/draft")
    @PreAuthorize("@ss.hasAnyRole('MASTER,ADMIN')")
    public ResponseEntity<Void> deleteMyDraft() {
        articleService.deleteMyDraft(SecurityUtils.getUserId());
        return ResponseEntityUtils.ok();
    }

    /**
     * 更新文章
     *
     * @param articleId 文章 ID
     * @param dto       文章信息
     */
    @PutMapping("/{articleId}")
    public ResponseEntity<Void> updateArticle(@PathVariable String articleId,
                                              @Valid @RequestBody ArticleDTO dto) {
        articleService.updateArticle(SecurityUtils.getUserId(), articleId, dto);
        return ResponseEntityUtils.ok();
    }

    /**
     * 删除文章
     *
     * @param articleId 文章 ID
     */
    @DeleteMapping("/{articleId}")
    @PreAuthorize("@ss.hasAnyRole('MASTER,ADMIN')")
    public ResponseEntity<Void> deleteArticle(@PathVariable String articleId) {
        articleService.deleteArticle(SecurityUtils.getUserId(), articleId);
        return ResponseEntityUtils.ok();
    }

    /**
     * 保存文章中的图片
     * @param images    图片文件
     * @return          保存访问地址
     */
    @PostMapping(value = "/image",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@ss.hasAnyRole('MASTER,ADMIN')")
    public ResponseEntity<List<String>> image(List<MultipartFile> images){
        List<String> urls = articleService.upload(images);
        return ResponseEntityUtils.ok(urls);
    }

    /**
     * 删除文章图片
     *
     * @param imagePath 上传接口返回的图片访问地址，或 OSS objectKey（blog/image/ 下）
     */
    @DeleteMapping("/image")
    @PreAuthorize("@ss.hasAnyRole('MASTER,ADMIN')")
    public ResponseEntity<Void> deleteImage(@RequestParam String imagePath) {
        articleService.deleteImage(imagePath);
        return ResponseEntityUtils.ok();
    }

    /**
     * 分页查询当前用户发布的文章
     *
     * @param sortBy   排序方式：time 按发布时间，view 按浏览量
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 文章分页结果
     */
    @GetMapping("/mine")
    @PreAuthorize("@ss.hasAnyRole('MASTER,ADMIN')")
    public ResponseEntity<Page<Article>> listMyArticles(@RequestParam(defaultValue = "time") String sortBy,
                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntityUtils.ok(
                articleService.listMyArticles(SecurityUtils.getUserId(), sortBy, pageNum, pageSize));
    }

    /**
     * 查询文章详情（同时增加浏览量）
     *
     * @param articleId 文章 ID
     * @return 文章详情
     */
    @Release
    @GetMapping("/detail/{articleId}")
    public ResponseEntity<Article> getArticle(@PathVariable String articleId) {
        Article article = articleService.getArticleById(articleId);
        articleService.incrementViewCount(articleId);
        return ResponseEntityUtils.ok(article);
    }

    /**
     * 分页查询文章列表
     *
     * @param category 分类（可选）
     * @param tag      标签（可选）
     * @param keyword  搜索关键词（可选，优先匹配标题，再匹配正文）
     * @param isHot    是否热门文章（按浏览量排序，keyword 为空时生效）
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 文章分页结果
     */
    @Release
    @GetMapping("/list")
    public ResponseEntity<Page<Article>> listArticles(@RequestParam(required = false) String category,
                                                      @RequestParam(required = false) String tag,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(defaultValue = "false") boolean isHot,
                                                      @RequestParam(required = false) String sortBy,
                                                      @RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntityUtils.ok(
                articleService.listArticles(category, tag, keyword, isHot, sortBy, pageNum, pageSize));
    }

    @Release
    @GetMapping("/tags")
    public ResponseEntity<List<Map<String, Object>>> listAllTags() {
        return ResponseEntityUtils.ok(articleService.listAllTags());
    }

    /**
     * 查询热门标签
     *
     * @param limit 返回条数
     * @return 热门标签列表
     */
    @Release
    @GetMapping("/tags/hot")
    public ResponseEntity<List<Map<String, Object>>> listHotTags(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntityUtils.ok(articleService.listHotTags(limit));
    }

    /**
     * 获取所有的分类
     * @return 系统中所有文章的分类列表
     */
    @Release
    @GetMapping("/categories")
    public ResponseEntity<List<String>>  listAllCategories() {
        return ResponseEntityUtils.ok(articleMongoSupport.getCategories());
    }
}
