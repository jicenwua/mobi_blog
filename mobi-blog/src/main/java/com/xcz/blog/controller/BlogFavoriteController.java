package com.xcz.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xcz.blog.domain.BlogFavorite;
import com.xcz.blog.domain.BlogFavoriteArticle;
import com.xcz.blog.domain.dto.FavoriteFolderDTO;
import com.xcz.blog.service.BlogFavoriteService;
import com.xcz.commons.core.domain.ResponseEntity;
import com.xcz.commons.core.utils.response.ResponseEntityUtils;
import com.xcz.commons.security.utils.SecurityUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收藏夹接口
 */
@RestController
@RequestMapping("/blog/favorite")
public class BlogFavoriteController {

    @Resource
    private BlogFavoriteService blogFavoriteService;

    /**
     * 创建收藏夹
     *
     * @param dto 收藏夹信息
     * @return 收藏夹 ID
     */
    @PostMapping
    public ResponseEntity<Long> createFolder(@Valid @RequestBody FavoriteFolderDTO dto) {
        Long favoriteId = blogFavoriteService.createFolder(SecurityUtils.getUserId(), dto);
        return ResponseEntityUtils.ok(favoriteId);
    }

    /**
     * 更新收藏夹
     *
     * @param favoriteId 收藏夹 ID
     * @param dto        收藏夹信息
     */
    @PutMapping("/{favoriteId}")
    public ResponseEntity<Void> updateFolder(@PathVariable Long favoriteId,
                                             @Valid @RequestBody FavoriteFolderDTO dto) {
        blogFavoriteService.updateFolder(SecurityUtils.getUserId(), favoriteId, dto);
        return ResponseEntityUtils.ok();
    }

    /**
     * 删除收藏夹
     *
     * @param favoriteId 收藏夹 ID
     */
    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long favoriteId) {
        blogFavoriteService.deleteFolder(SecurityUtils.getUserId(), favoriteId);
        return ResponseEntityUtils.ok();
    }

    /**
     * 分页查询收藏夹列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 收藏夹分页结果
     */
    @GetMapping
    public ResponseEntity<Page<BlogFavorite>> listFolders(@RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntityUtils.ok(blogFavoriteService.listFolders(SecurityUtils.getUserId(), pageNum, pageSize));
    }

    /**
     * 将文章加入收藏夹
     *
     * @param favoriteId 收藏夹 ID
     * @param articleId  文章 ID
     */
    @PostMapping("/{favoriteId}/article/{articleId}")
    public ResponseEntity<Void> addArticle(@PathVariable Long favoriteId, @PathVariable String articleId) {
        blogFavoriteService.addArticle(SecurityUtils.getUserId(), favoriteId, articleId);
        return ResponseEntityUtils.ok();
    }

    /**
     * 从收藏夹移除文章
     *
     * @param favoriteId 收藏夹 ID
     * @param articleId  文章 ID
     */
    @DeleteMapping("/{favoriteId}/article/{articleId}")
    public ResponseEntity<Void> removeArticle(@PathVariable Long favoriteId, @PathVariable String articleId) {
        blogFavoriteService.removeArticle(SecurityUtils.getUserId(), favoriteId, articleId);
        return ResponseEntityUtils.ok();
    }

    /**
     * 分页查询收藏夹内的文章
     *
     * @param favoriteId 收藏夹 ID
     * @param pageNum    页码
     * @param pageSize   每页条数
     * @return 收藏文章分页结果
     */
    @GetMapping("/{favoriteId}/article")
    public ResponseEntity<Page<BlogFavoriteArticle>> listArticles(@PathVariable Long favoriteId,
                                                                  @RequestParam(defaultValue = "1") int pageNum,
                                                                  @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntityUtils.ok(
                blogFavoriteService.listArticles(SecurityUtils.getUserId(), favoriteId, pageNum, pageSize));
    }

    /**
     * 查询当前用户是否已收藏该文章
     *
     * @param articleId 文章 ID
     * @return true 已收藏 | false 未收藏
     */
    @GetMapping("/article/{articleId}/status")
    public ResponseEntity<Boolean> isFavorited(@PathVariable String articleId) {
        return ResponseEntityUtils.ok(blogFavoriteService.isFavorited(SecurityUtils.getUserId(), articleId));
    }

    /**
     * 切换文章收藏状态
     *
     * @param articleId 文章 ID
     * @return 切换后的收藏状态
     */
    @PostMapping("/article/{articleId}/toggle")
    public ResponseEntity<Boolean> toggleFavorite(@PathVariable String articleId) {
        return ResponseEntityUtils.ok(blogFavoriteService.toggleFavorite(SecurityUtils.getUserId(), articleId));
    }
}
