package com.xcz.blog.service.impl;

import com.xcz.blog.constant.BlogConstants;
import com.xcz.blog.domain.BlogUser;
import com.xcz.blog.domain.dto.ArticleDTO;
import com.xcz.blog.domain.dto.ArticleDraftDTO;
import com.xcz.blog.domain.enums.ArticleCategory;
import com.xcz.blog.domain.enums.ArticleStatus;
import com.xcz.blog.domain.enums.RoleStatue;
import com.xcz.blog.domain.mongo.Article;
import com.xcz.blog.mapper.BlogUserMapper;
import com.xcz.blog.repository.ArticleRepository;
import com.xcz.blog.service.AdminLogService;
import com.xcz.blog.service.ArticleService;
import com.xcz.blog.support.ArticleMongoSupport;
import com.xcz.blog.support.TagUtils;
import com.xcz.blog.support.UserDisplaySupport;
import com.xcz.blog.support.ViewSupport;
import com.xcz.commons.core.exception.ServiceException;
import com.xcz.commons.core.utils.ServletUtils;
import com.xcz.commons.core.utils.StringUtils;
import com.xcz.commons.core.utils.ip.Ipv6Utils;
import com.xcz.commons.oss.service.UploadService;
import com.xcz.commons.security.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final BlogUserMapper blogUserMapper;
    private final ArticleMongoSupport articleMongoSupport;
    private final UserDisplaySupport userDisplaySupport;
    private final UploadService uploadService;
    private final AdminLogService adminLogService;

    /**
     * 创建文章
     *
     * @param authorId 作者用户 ID
     * @param dto      文章信息
     * @return 文章 ID
     */
    @Override
    public String createArticle(Long authorId, ArticleDTO dto) {
        requireUser(authorId);
        validateCategory(dto.getCategory());

        LocalDateTime now = LocalDateTime.now();
        Article article = articleRepository
                .findByAuthorIdAndStatus(authorId, ArticleStatus.DRAFT.getCode())
                .orElse(null);

        if (article == null) {
            article = Article.builder()
                    .authorId(authorId)
                    .viewCount(0L)
                    .favoriteCount(0L)
                    .commentCount(0L)
                    .createTime(now)
                    .build();
        }

        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCategory(dto.getCategory());
        article.setTags(normalizeTags(dto.getTags()));
        article.setStatus(ArticleStatus.PUBLISHED.getCode());
        article.setUpdateTime(now);
        String articleId = articleRepository.save(article).getId();
        adminLogService.record(authorId, "文章", "发布文章", articleId,
                String.format("发布文章《%s》", dto.getTitle()));
        return articleId;
    }

    /**
     * 更新文章
     *
     * @param authorId  作者用户 ID
     * @param articleId 文章 ID
     * @param dto       文章信息
     */
    @Override
    public void updateArticle(Long authorId, String articleId, ArticleDTO dto) {
        Article article = articleMongoSupport.requireArticle(articleId);
        assertAuthor(authorId, article);
        validateCategory(dto.getCategory());

        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCategory(dto.getCategory());
        article.setTags(normalizeTags(dto.getTags()));
        article.setUpdateTime(LocalDateTime.now());
        articleRepository.save(article);
    }

    /**
     * 删除文章
     *
     * @param authorId  作者用户 ID
     * @param articleId 文章 ID
     */
    @Override
    public void deleteArticle(Long authorId, String articleId) {
        BlogUser blogUser = blogUserMapper.selectById(authorId);
        if (blogUser == null) {
            throw new ServiceException("用户不存在");
        }
        Article article = articleMongoSupport.requireArticle(articleId);
        if (RoleStatue.isAdmin(blogUser.getRole())) {
            if (!Objects.equals(article.getAuthorId(), authorId)) {
                throw new ServiceException("没有权限删除该文章");
            }
            articleRepository.deleteById(articleId);
        } else if (RoleStatue.isMaster(blogUser.getRole())) {
            articleRepository.deleteById(articleId);
        } else {
            throw new ServiceException("没有权限删除该文章");
        }
        adminLogService.record(authorId, "文章", "删除文章", articleId,
                String.format("删除文章《%s》", article.getTitle()));
    }

    /**
     * 根据 ID 查询文章
     *
     * @param articleId 文章 ID
     * @return 文章详情
     */
    @Override
    public Article getArticleById(String articleId) {
        Article article = articleMongoSupport.requireArticle(articleId);
        if (ArticleStatus.DRAFT.getCode().equals(article.getStatus())) {
            throw new ServiceException("文章不存在");
        }
        userDisplaySupport.enrichArticle(article);
        return article;
    }

    /**
     * 文章浏览量 +1
     *
     * @param articleId 文章 ID
     */
    @Override
    public void incrementViewCount(String articleId) {
        String id;
        try {
            Long userId = SecurityUtils.getUserId();
            id = String.valueOf(userId);
        }catch (Exception e){
            id = Ipv6Utils.getClientIp(ServletUtils.getRequest());
        }
        ViewSupport.addView(articleId,id);
    }

    /**
     * 分页查询已发布文章
     *
     * @param category 分类（可选）
     * @param tag      标签（可选）
     * @param keyword  搜索关键词（可选）
     * @param isHot    是否查询热门文章
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 文章分页结果
     */
    @Override
    public Page<Article> listArticles(String category, String tag, String keyword, boolean isHot, String sortBy,
                                      int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize);
        String status = ArticleStatus.PUBLISHED.getCode();
        boolean hasCategory = StringUtils.isNotEmpty(category);
        boolean hasTag = StringUtils.isNotEmpty(tag);
        boolean hasKeyword = StringUtils.isNotEmpty(keyword);
        boolean sortByView = "view".equalsIgnoreCase(sortBy);

        if (hasCategory) {
            validateCategory(category);
        }

        if (hasKeyword) {
            return enrichArticlePage(articleMongoSupport.searchPublishedArticles(
                    keyword.trim(),
                    hasCategory ? category : null,
                    hasTag ? tag : null,
                    pageable
            ));
        }

        if (hasCategory && hasTag) {
            return enrichArticlePage(articleMongoSupport.findPublishedByTag(
                    tag, category, pageable, Sort.by(Sort.Order.desc("createTime"))));
        }
        if (hasCategory) {
            return enrichArticlePage(articleRepository.findByStatusAndCategoryOrderByCreateTimeDesc(status, category, pageable));
        }
        if (hasTag) {
            Sort sort = sortByView
                    ? Sort.by(Sort.Order.desc("viewCount"), Sort.Order.desc("createTime"))
                    : Sort.by(Sort.Order.desc("createTime"));
            return enrichArticlePage(articleMongoSupport.findPublishedByTag(tag, null, pageable, sort));
        }
        if (isHot) {
            return enrichArticlePage(articleRepository.findByStatusOrderByViewCountDesc(status, pageable));
        }
        return enrichArticlePage(articleRepository.findByStatusOrderByCreateTimeDesc(status, pageable));
    }

    /**
     * 分页查询当前用户发布的文章
     */
    @Override
    public Page<Article> listMyArticles(Long authorId, String sortBy, int pageNum, int pageSize) {
        requireUser(authorId);
        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize);
        Page<Article> page = "view".equalsIgnoreCase(sortBy)
                ? articleRepository.findByAuthorIdOrderByViewCountDesc(authorId, pageable)
                : articleRepository.findByAuthorIdOrderByCreateTimeDesc(authorId, pageable);
        return enrichArticlePage(page);
    }

    @Override
    public List<Map<String, Object>> listAllTags() {
        return articleMongoSupport.listAllTags().stream()
                .map(this::toTagMap)
                .toList();
    }

    @Override
    public List<Map<String, Object>> listHotTags(int limit) {
        return articleMongoSupport.listHotTags(limit).stream()
                .map(this::toTagMap)
                .toList();
    }

    private Map<String, Object> toTagMap(Document doc) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", doc.getString("name"));
        item.put("articleCount", doc.get("articleCount", Number.class).longValue());
        item.put("viewCount", doc.get("viewCount", Number.class).longValue());
        return item;
    }

    /**
     * 为文章分页结果补全作者昵称
     *
     * @param page 文章分页结果
     * @return 补全后的分页结果
     */
    private Page<Article> enrichArticlePage(Page<Article> page) {
        userDisplaySupport.enrichArticles(page.getContent());
        return page;
    }

    @Override
    public List<String> upload(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> urls = new ArrayList<>(images.size());
        try {
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                String objectKey = BlogConstants.IMAGE_PATH + file.getOriginalFilename();
                String uploadedKey = uploadService.simpleUpload(file, objectKey);
                urls.add(uploadService.getEnteralUrl(uploadedKey));
            }
        } catch (Exception e) {
            for (String uploadedKey : urls) {
                uploadService.delete(uploadedKey);
            }
            throw new ServiceException("图片上传失败");
        }
        return urls;
    }

    @Override
    public void deleteImage(String imagePath) {
        if (StringUtils.isEmpty(imagePath)) {
            throw new ServiceException("图片路径不能为空");
        }
        if (!imagePath.contains(BlogConstants.IMAGE_PATH)) {
            throw new ServiceException("非法的图片路径");
        }
        if (!uploadService.delete(imagePath)) {
            throw new ServiceException("图片删除失败");
        }
    }

    @Override
    public Article getMyDraft(Long authorId) {
        requireUser(authorId);
        return articleRepository
                .findByAuthorIdAndStatus(authorId, ArticleStatus.DRAFT.getCode())
                .orElse(null);
    }

    @Override
    public Article saveDraft(Long authorId, ArticleDraftDTO dto) {
        requireUser(authorId);
        if (isDraftEmpty(dto)) {
            throw new ServiceException("草稿内容不能为空");
        }
        if (StringUtils.isNotEmpty(dto.getCategory())) {
            validateCategory(dto.getCategory());
        }

        LocalDateTime now = LocalDateTime.now();
        Article article = articleRepository
                .findByAuthorIdAndStatus(authorId, ArticleStatus.DRAFT.getCode())
                .orElse(null);

        if (article == null) {
            article = Article.builder()
                    .authorId(authorId)
                    .viewCount(0L)
                    .favoriteCount(0L)
                    .commentCount(0L)
                    .status(ArticleStatus.DRAFT.getCode())
                    .createTime(now)
                    .build();
        }

        article.setTitle(StringUtils.trimToEmpty(dto.getTitle()));
        article.setSummary(StringUtils.trimToEmpty(dto.getSummary()));
        article.setContent(StringUtils.trimToEmpty(dto.getContent()));
        article.setCategory(StringUtils.trimToEmpty(dto.getCategory()));
        article.setTags(normalizeTags(dto.getTags()));
        article.setUpdateTime(now);
        return articleRepository.save(article);
    }

    @Override
    public void deleteMyDraft(Long authorId) {
        requireUser(authorId);
        articleRepository
                .findByAuthorIdAndStatus(authorId, ArticleStatus.DRAFT.getCode())
                .ifPresent(articleRepository::delete);
    }

    private boolean isDraftEmpty(ArticleDraftDTO dto) {
        boolean hasText = StringUtils.isNotEmpty(dto.getTitle())
                || StringUtils.isNotEmpty(dto.getSummary())
                || StringUtils.isNotEmpty(dto.getContent())
                || StringUtils.isNotEmpty(dto.getCategory());
        boolean hasTags = dto.getTags() != null && dto.getTags().stream().anyMatch(StringUtils::isNotEmpty);
        return !hasText && !hasTags;
    }

    /**
     * 规范化标签列表（去空、去重、保序）
     *
     * @param tags 原始标签列表
     * @return 规范化后的标签列表
     */
    private List<String> normalizeTags(List<String> tags) {
        return TagUtils.normalizeList(tags);
    }

    /**
     * 校验用户存在且具有发布权限
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    private BlogUser requireUser(Long userId) {
        BlogUser user = blogUserMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        boolean canPublish = RoleStatue.isCanPublish(user.getRole());
        if (!canPublish) {
            throw new ServiceException("没有权限发布文章");
        }
        return user;
    }

    /**
     * 校验当前用户是否为文章作者
     *
     * @param authorId 作者用户 ID
     * @param article  文章
     */
    private void assertAuthor(Long authorId, Article article) {
        if (!authorId.equals(article.getAuthorId())) {
            throw new ServiceException("无权操作该文章");
        }
    }

    /**
     * 校验文章分类是否合法
     *
     * @param category 分类编码
     */
    private void validateCategory(String category) {
        boolean valid = Arrays.stream(ArticleCategory.values())
                .anyMatch(item -> item.getCode().equals(category));
        if (!valid) {
            throw new ServiceException("文章分类不合法");
        }
    }
}
