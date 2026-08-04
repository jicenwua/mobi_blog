package com.xcz.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xcz.blog.domain.BlogFavoriteArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏夹文章关联 Mapper
 */
@Mapper
public interface BlogFavoriteArticleMapper extends BaseMapper<BlogFavoriteArticle> {

    /**
     * 查询收藏夹与文章的关联记录
     *
     * @param favoriteId 收藏夹 ID
     * @param articleId  文章 ID
     * @return 关联记录，不存在时返回 null
     */
    BlogFavoriteArticle selectByFavoriteIdAndArticleId(@Param("favoriteId") Long favoriteId,
                                                       @Param("articleId") String articleId);

    /**
     * 统计用户对某篇文章的收藏次数
     *
     * @param userId    用户 ID
     * @param articleId 文章 ID
     * @return 收藏次数
     */
    int countByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") String articleId);

    /**
     * 查询用户对某篇文章的全部收藏关联
     *
     * @param userId    用户 ID
     * @param articleId 文章 ID
     * @return 收藏关联列表
     */
    List<BlogFavoriteArticle> selectByUserIdAndArticleId(@Param("userId") Long userId,
                                                         @Param("articleId") String articleId);
}
