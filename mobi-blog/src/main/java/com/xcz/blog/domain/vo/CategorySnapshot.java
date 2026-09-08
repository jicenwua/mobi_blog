package com.xcz.blog.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类列表快照（用于 Redis 缓存，包装空列表避免 JsonJacksonCodec 反序列化失败）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> categories = new ArrayList<>();
}
