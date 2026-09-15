package com.xcz.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xcz.blog.domain.BlogFavorite;
import com.xcz.blog.domain.BlogFavoriteArticle;
import com.xcz.blog.domain.dto.FavoriteFolderDTO;
import com.xcz.blog.mapper.BlogFavoriteArticleMapper;
import com.xcz.blog.mapper.BlogFavoriteMapper;
import com.xcz.blog.service.BlogFavoriteService;
import com.xcz.blog.support.ArticleMongoSupport;
import com.xcz.commons.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogFavoriteServiceImpl implements BlogFavoriteService {

    public static final String DEFAULT_FOLDER_NAME = "默认收藏夹";
    public static final String DEFAULT_FOLDER_REMARK = "DEFAULT";
    public static final long VIRTUAL_DEFAULT_FOLDER_ID = 0L;

    private final BlogFavoriteMapper blogFavoriteMapper;
    private final BlogFavoriteArticleMapper blogFavoriteArticleMapper;
    private final ArticleMongoSupport articleMongoSupport;

    /**
     * 创建收藏夹
     *
     * @param userId 用户 ID
     * @param dto    收藏夹信息
     * @return 收藏夹 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFolder(Long userId, FavoriteFolderDTO dto) {
        String folderName = dto.getFavoriteName().trim();
        if (DEFAULT_FOLDER_NAME.equals(folderName)) {
            throw new ServiceException("该名称为系统保留，请使用其他名称");
        }

        LocalDateTime now = LocalDateTime.now();
        BlogFavorite folder = BlogFavorite.builder()
                .userId(userId)
                .favoriteName(folderName)
                .createTime(now)
                .updateTime(now)
                .build();
        blogFavoriteMapper.insert(folder);
        return folder.getFavoriteId();
    }

    /**
     * 更新收藏夹
     *
     * @param userId     用户 ID
     * @param favoriteId 收藏夹 ID
     * @param dto        收藏夹信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFolder(Long userId, Long favoriteId, FavoriteFolderDTO dto) {
        BlogFavorite folder = requireFolder(userId, favoriteId);
        if (isDefaultFolder(folder)) {
            throw new ServiceException("默认收藏夹不可修改");
        }

        folder.setFavoriteName(dto.getFavoriteName().trim());
        folder.setUpdateTime(LocalDateTime.now());
        blogFavoriteMapper.updateById(folder);
    }

    /**
     * 删除收藏夹及其关联文章
     *
     * @param userId     用户 ID
     * @param favoriteId 收藏夹 ID
     */
    @Override
    public void deleteFolder(Long userId, Long favoriteId) {
        if (favoriteId == null || favoriteId == VIRTUAL_DEFAULT_FOLDER_ID) {
            throw new ServiceException("默认收藏夹不可删除");
        }

        BlogFavorite folder = requireFolder(userId, favoriteId);
        if (isDefaultFolder(folder)) {
            throw new ServiceException("默认收藏夹不可删除");
        }

        List<BlogFavoriteArticle> articles = blogFavoriteArticleMapper.selectList(
                new LambdaQueryWrapper<BlogFavoriteArticle>().eq(BlogFavoriteArticle::getFavoriteId, favoriteId));
        blogFavoriteMapper.deleteById(favoriteId);
        blogFavoriteArticleMapper.delete(new LambdaQueryWrapper<BlogFavoriteArticle>()
                .eq(BlogFavoriteArticle::getFavoriteId, favoriteId));

        for (BlogFavoriteArticle item : articles) {
            syncFavoriteCountOnRemove(userId, item.getArticleId());
        }
    }

    /**
     * 分页查询用户收藏夹列表
     *
     * @param userId   用户 ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 收藏夹分页结果
     */
    @Override
    public Page<BlogFavorite> listFolders(Long userId, int pageNum, int pageSize) {
        Page<BlogFavorite> page = new Page<>(pageNum, pageSize);
        Page<BlogFavorite> result = blogFavoriteMapper.selectPage(page, new LambdaQueryWrapper<BlogFavorite>()
                .eq(BlogFavorite::getUserId, userId)
                .orderByDesc(BlogFavorite::getCreateTime));

        List<BlogFavorite> records = new ArrayList<>(result.getRecords());
        BlogFavorite defaultFolder = findDefaultFolder(userId);

        if (defaultFolder == null) {
            records.removeIf(folder -> DEFAULT_FOLDER_NAME.equals(folder.getFavoriteName()));
            records.add(0, buildVirtualDefaultFolder(userId));
            result.setTotal(result.getTotal() + 1);
        } else {
            normalizeDefaultFolder(defaultFolder);
            defaultFolder.setDefaultFolder(true);
            Long defaultId = defaultFolder.getFavoriteId();
            records.removeIf(folder ->
                    defaultId.equals(folder.getFavoriteId())
                            || DEFAULT_FOLDER_NAME.equals(folder.getFavoriteName()));
            records.add(0, defaultFolder);
        }

        result.setRecords(records);
        return result;
    }

    /**
     * 将文章加入收藏夹
     *
     * @param userId     用户 ID
     * @param favoriteId 收藏夹 ID
     * @param articleId  文章 ID
     */
    @Override
    public void addArticle(Long userId, Long favoriteId, String articleId) {
        Long resolvedFavoriteId = resolveFavoriteId(userId, favoriteId);
        requireFolder(userId, resolvedFavoriteId);
        articleMongoSupport.requireArticle(articleId);

        if (blogFavoriteArticleMapper.selectByFavoriteIdAndArticleId(resolvedFavoriteId, articleId) != null) {
            throw new ServiceException("该收藏夹中已存在此文章");
        }

        boolean firstFavorite = blogFavoriteArticleMapper.countByUserIdAndArticleId(userId, articleId) == 0;
        BlogFavoriteArticle relation = BlogFavoriteArticle.builder()
                .favoriteId(resolvedFavoriteId)
                .articleId(articleId)
                .createTime(LocalDateTime.now())
                .build();
        blogFavoriteArticleMapper.insert(relation);

        if (firstFavorite) {
            articleMongoSupport.adjustFavoriteCount(articleId, 1);
        }
    }

    /**
     * 从收藏夹移除文章
     *
     * @param userId     用户 ID
     * @param favoriteId 收藏夹 ID
     * @param articleId  文章 ID
     */
    @Override
    public void removeArticle(Long userId, Long favoriteId, String articleId) {
        Long resolvedFavoriteId = resolveFavoriteId(userId, favoriteId, false);
        if (resolvedFavoriteId == null) {
            throw new ServiceException("收藏夹中不存在该文章");
        }

        requireFolder(userId, resolvedFavoriteId);

        BlogFavoriteArticle relation =
                blogFavoriteArticleMapper.selectByFavoriteIdAndArticleId(resolvedFavoriteId, articleId);
        if (relation == null) {
            throw new ServiceException("收藏夹中不存在该文章");
        }

        blogFavoriteArticleMapper.deleteById(relation.getId());
        syncFavoriteCountOnRemove(userId, articleId);
    }

    /**
     * 分页查询收藏夹内的文章
     *
     * @param userId     用户 ID
     * @param favoriteId 收藏夹 ID
     * @param pageNum    页码
     * @param pageSize   每页条数
     * @return 收藏文章分页结果
     */
    @Override
    public Page<BlogFavoriteArticle> listArticles(Long userId, Long favoriteId, int pageNum, int pageSize) {
        Long resolvedFavoriteId = resolveFavoriteId(userId, favoriteId, false);
        if (resolvedFavoriteId == null) {
            return new Page<>(pageNum, pageSize, 0);
        }

        requireFolder(userId, resolvedFavoriteId);
        Page<BlogFavoriteArticle> page = new Page<>(pageNum, pageSize);
        return blogFavoriteArticleMapper.selectPage(page, new LambdaQueryWrapper<BlogFavoriteArticle>()
                .eq(BlogFavoriteArticle::getFavoriteId, resolvedFavoriteId)
                .orderByDesc(BlogFavoriteArticle::getCreateTime));
    }

    /**
     * 判断用户是否已收藏该文章
     *
     * @param userId    用户 ID
     * @param articleId 文章 ID
     * @return true 已收藏 | false 未收藏
     */
    @Override
    public boolean isFavorited(Long userId, String articleId) {
        return blogFavoriteArticleMapper.countByUserIdAndArticleId(userId, articleId) > 0;
    }

    /**
     * 切换文章收藏状态
     *
     * @param userId    用户 ID
     * @param articleId 文章 ID
     * @return 切换后的收藏状态
     */
    @Override
    public boolean toggleFavorite(Long userId, String articleId) {
        if (isFavorited(userId, articleId)) {
            List<BlogFavoriteArticle> relations =
                    blogFavoriteArticleMapper.selectByUserIdAndArticleId(userId, articleId);
            for (BlogFavoriteArticle relation : relations) {
                removeArticle(userId, relation.getFavoriteId(), articleId);
            }
            return false;
        }

        addArticle(userId, VIRTUAL_DEFAULT_FOLDER_ID, articleId);
        return true;
    }

    /**
     * 首次收藏到默认收藏夹时懒创建
     */
    private Long ensureDefaultFolder(Long userId) {
        BlogFavorite existing = findDefaultFolder(userId);
        if (existing != null) {
            normalizeDefaultFolder(existing);
            return existing.getFavoriteId();
        }

        LocalDateTime now = LocalDateTime.now();
        BlogFavorite folder = BlogFavorite.builder()
                .userId(userId)
                .favoriteName(DEFAULT_FOLDER_NAME)
                .remark(DEFAULT_FOLDER_REMARK)
                .createTime(now)
                .updateTime(now)
                .build();
        blogFavoriteMapper.insert(folder);
        return folder.getFavoriteId();
    }

    private BlogFavorite findDefaultFolder(Long userId) {
        return blogFavoriteMapper.selectDefaultByUserId(userId);
    }

    private BlogFavorite buildVirtualDefaultFolder(Long userId) {
        BlogFavorite folder = BlogFavorite.builder()
                .favoriteId(VIRTUAL_DEFAULT_FOLDER_ID)
                .userId(userId)
                .favoriteName(DEFAULT_FOLDER_NAME)
                .build();
        folder.setDefaultFolder(true);
        return folder;
    }

    private Long resolveFavoriteId(Long userId, Long favoriteId) {
        return resolveFavoriteId(userId, favoriteId, true);
    }

    private Long resolveFavoriteId(Long userId, Long favoriteId, boolean createDefaultIfMissing) {
        if (favoriteId == null || favoriteId == VIRTUAL_DEFAULT_FOLDER_ID) {
            if (createDefaultIfMissing) {
                return ensureDefaultFolder(userId);
            }
            BlogFavorite defaultFolder = findDefaultFolder(userId);
            return defaultFolder != null ? defaultFolder.getFavoriteId() : null;
        }
        return favoriteId;
    }

    private boolean isDefaultFolder(BlogFavorite folder) {
        if (folder == null) {
            return false;
        }
        return DEFAULT_FOLDER_REMARK.equals(folder.getRemark())
                || DEFAULT_FOLDER_NAME.equals(folder.getFavoriteName());
    }

    private void normalizeDefaultFolder(BlogFavorite folder) {
        if (folder == null || DEFAULT_FOLDER_REMARK.equals(folder.getRemark())) {
            return;
        }
        folder.setRemark(DEFAULT_FOLDER_REMARK);
        folder.setFavoriteName(DEFAULT_FOLDER_NAME);
        folder.setUpdateTime(LocalDateTime.now());
        blogFavoriteMapper.updateById(folder);
    }

    /**
     * 校验收藏夹存在且属于当前用户
     *
     * @param userId     用户 ID
     * @param favoriteId 收藏夹 ID
     * @return 收藏夹信息
     */
    private BlogFavorite requireFolder(Long userId, Long favoriteId) {
        BlogFavorite folder = blogFavoriteMapper.selectByIdAndUserId(favoriteId, userId);
        if (folder == null) {
            throw new ServiceException("收藏夹不存在");
        }
        if (isDefaultFolder(folder)) {
            folder.setDefaultFolder(true);
        }
        return folder;
    }

    /**
     * 移除收藏后同步文章收藏量
     *
     * @param userId    用户 ID
     * @param articleId 文章 ID
     */
    private void syncFavoriteCountOnRemove(Long userId, String articleId) {
        if (blogFavoriteArticleMapper.countByUserIdAndArticleId(userId, articleId) == 0) {
            articleMongoSupport.adjustFavoriteCount(articleId, -1);
        }
    }
}
