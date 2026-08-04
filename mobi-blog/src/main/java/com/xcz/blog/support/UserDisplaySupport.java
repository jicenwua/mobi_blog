package com.xcz.blog.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xcz.blog.domain.BlogUser;
import com.xcz.blog.domain.mongo.Article;
import com.xcz.blog.domain.mongo.Comment;
import com.xcz.blog.mapper.BlogUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 从 MySQL 批量补全用户展示信息（昵称等）
 */
@Component
@RequiredArgsConstructor
public class UserDisplaySupport {

    private final BlogUserMapper blogUserMapper;

    /**
     * 为单篇文章补全作者昵称
     *
     * @param article 文章对象
     */
    public void enrichArticle(Article article) {
        if (article == null || article.getAuthorId() == null) {
            return;
        }
        article.setAuthorName(loadNickNames(Set.of(article.getAuthorId())).get(article.getAuthorId()));
    }

    /**
     * 批量为文章列表补全作者昵称
     *
     * @param articles 文章列表
     */
    public void enrichArticles(Collection<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            return;
        }
        Map<Long, String> nickNames = loadNickNames(collectAuthorIds(articles));
        for (Article article : articles) {
            if (article.getAuthorId() != null) {
                article.setAuthorName(nickNames.get(article.getAuthorId()));
            }
        }
    }

    /**
     * 为单条评论补全用户昵称
     *
     * @param comment 评论对象
     */
    public void enrichComment(Comment comment) {
        if (comment == null || comment.getUserId() == null) {
            return;
        }
        comment.setUserNickName(loadNickNames(Set.of(comment.getUserId())).get(comment.getUserId()));
    }

    /**
     * 批量为评论列表补全用户昵称
     *
     * @param comments 评论列表
     */
    public void enrichComments(Collection<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }
        Map<Long, String> nickNames = loadNickNames(collectUserIds(comments));
        for (Comment comment : comments) {
            if (comment.getUserId() != null) {
                comment.setUserNickName(nickNames.get(comment.getUserId()));
            }
        }
    }

    /**
     * 根据用户 ID 批量查询昵称
     *
     * @param userIds 用户 ID 集合
     * @return userId → nickName 映射
     */
    private Map<Long, String> loadNickNames(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<Long> distinctIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
        if (distinctIds.isEmpty()) {
            return Map.of();
        }
        return blogUserMapper.selectList(new LambdaQueryWrapper<BlogUser>()
                        .in(BlogUser::getUserId, distinctIds)).stream()
                .collect(Collectors.toMap(BlogUser::getUserId, BlogUser::getNickName, (left, right) -> left));
    }

    /**
     * 从文章列表中收集作者 ID
     *
     * @param articles 文章列表
     * @return 去重后的作者 ID 集合
     */
    private Set<Long> collectAuthorIds(Collection<Article> articles) {
        return articles.stream()
                .map(Article::getAuthorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 从评论列表中收集用户 ID
     *
     * @param comments 评论列表
     * @return 去重后的用户 ID 集合
     */
    private Set<Long> collectUserIds(Collection<Comment> comments) {
        return comments.stream()
                .map(Comment::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
