package com.xcz.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xcz.blog.domain.BlogFavorite;
import com.xcz.blog.domain.BlogFavoriteArticle;
import com.xcz.blog.domain.dto.FavoriteFolderDTO;

/**
 * 用户收藏夹服务
 */
public interface BlogFavoriteService {

    /**
     * 创建收藏夹
     *
     * @param userId 用户 ID
     * @param dto    收藏夹信息
     * @return 收藏夹 ID
     */
    Long createFolder(Long userId, FavoriteFolderDTO dto);

    /**
     * 更新收藏夹
     *
     * @param userId     用户 ID
     * @param favoriteId 收藏夹 ID
     * @param dto        收藏夹信息
     */
    void updateFolder(Long userId, Long favoriteId, FavoriteFolderDTO dto);

    /**
     * 删除收藏夹及其关联文章
     *
     * @param userId     用户 ID
     * @param favoriteId 收藏夹 ID
     */
    void deleteFolder(Long userId, Long favoriteId);

    /**
     * 分页查询用户收藏夹列表
     *
     * @param userId   用户 ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 收藏夹分页结果
     */
    Page<BlogFavorite> listFolders(Long userId, int pageNum, int pageSize);

    /**
     * 将文章加入收藏夹
     *
     * @param userId     用户 ID
     * @param favoriteId 收藏夹 ID
     * @param articleId  文章 ID
     */
    void addArticle(Long userId, Long favoriteId, String articleId);

    /**
     * 从收藏夹移除文章
     *
     * @param userId     用户 ID
     * @param favoriteId 收藏夹 ID
     * @param articleId  文章 ID
     */
    void removeArticle(Long userId, Long favoriteId, String articleId);

    /**
     * 分页查询收藏夹内的文章
     *
     * @param userId     用户 ID
     * @param favoriteId 收藏夹 ID
     * @param pageNum    页码
     * @param pageSize   每页条数
     * @return 收藏文章分页结果
     */
    Page<BlogFavoriteArticle> listArticles(Long userId, Long favoriteId, int pageNum, int pageSize);

    /**
     * 判断用户是否已收藏该文章
     *
     * @param userId    用户 ID
     * @param articleId 文章 ID
     * @return true 已收藏 | false 未收藏
     */
    boolean isFavorited(Long userId, String articleId);

    /**
     * 切换文章收藏状态（收藏至默认收藏夹 / 取消全部收藏）
     *
     * @param userId    用户 ID
     * @param articleId 文章 ID
     * @return 切换后的收藏状态
     */
    boolean toggleFavorite(Long userId, String articleId);
}
