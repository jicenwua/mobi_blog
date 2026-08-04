package com.xcz.blog.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收藏夹表 blog_favorite
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("blog_favorite")
public class BlogFavorite implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long favoriteId;

    /** 所属用户 ID */
    private Long userId;

    /** 收藏夹名称 */
    private String favoriteName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private String remark;

    /** 是否为系统默认收藏夹（非数据库字段） */
    @TableField(exist = false)
    private Boolean defaultFolder;

    @TableLogic
    private Integer delFlag;
}
