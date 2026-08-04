package com.xcz.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xcz.blog.domain.BlogFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户收藏夹 Mapper
 */
@Mapper
public interface BlogFavoriteMapper extends BaseMapper<BlogFavorite> {

    /**
     * 根据收藏夹 ID 和用户 ID 查询收藏夹
     *
     * @param favoriteId 收藏夹 ID
     * @param userId     用户 ID
     * @return 收藏夹信息，不存在时返回 null
     */
    BlogFavorite selectByIdAndUserId(@Param("favoriteId") Long favoriteId, @Param("userId") Long userId);

    /**
     * 查询用户默认收藏夹
     *
     * @param userId 用户 ID
     * @return 默认收藏夹，不存在时返回 null
     */
    BlogFavorite selectDefaultByUserId(@Param("userId") Long userId);
}
