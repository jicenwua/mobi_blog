package com.xcz.blog.support;

import com.xcz.commons.core.utils.StringUtils;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 标签规范化：忽略大小写与多余空格，避免重复标签。
 */
public final class TagUtils {

    private TagUtils() {
    }

    /**
     * 将标签规范化为统一形式：去首尾空格、合并中间空白、转小写。
     */
    public static String normalize(String tag) {
        if (StringUtils.isEmpty(tag)) {
            return "";
        }
        return tag.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * 规范化标签列表，并按规范化结果去重（保留首次出现顺序）。
     */
    public static List<String> normalizeList(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String tag : tags) {
            String value = normalize(tag);
            if (StringUtils.isNotEmpty(value)) {
                normalized.add(value);
            }
        }
        return new ArrayList<>(normalized);
    }

    /**
     * 构建与规范化标签等价的正则，用于匹配库中大小写/空格不一致的历史数据。
     */
    public static Pattern toEquivalentPattern(String tag) {
        String normalized = normalize(tag);
        if (StringUtils.isEmpty(normalized)) {
            return null;
        }
        String regex = Arrays.stream(normalized.split(" "))
                .map(Pattern::quote)
                .collect(Collectors.joining("\\s+"));
        return Pattern.compile("^\\s*" + regex + "\\s*$", Pattern.CASE_INSENSITIVE);
    }

    /**
     * 数组字段 tags 中是否存在与给定标签等价的元素。
     */
    public static Criteria tagEqualsCriteria(String tag) {
        Pattern pattern = toEquivalentPattern(tag);
        if (pattern == null) {
            return Criteria.where("tags").exists(false);
        }
        return Criteria.where("tags").regex(pattern);
    }
}
