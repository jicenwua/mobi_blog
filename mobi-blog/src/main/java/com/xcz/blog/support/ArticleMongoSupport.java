package com.xcz.blog.support;

import com.xcz.blog.domain.enums.ArticleStatus;
import com.xcz.blog.domain.mongo.Article;
import com.xcz.blog.repository.ArticleRepository;
import com.xcz.commons.core.exception.ServiceException;
import com.xcz.commons.core.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 文章 MongoDB 辅助操作
 */
@Component
@RequiredArgsConstructor
public class ArticleMongoSupport {

    private final ArticleRepository articleRepository;
    private final MongoTemplate mongoTemplate;

    /**
     * 查询文章，不存在时抛出异常
     *
     * @param articleId 文章 ID
     * @return 文章详情
     */
    public Article requireArticle(String articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new ServiceException("文章不存在"));
    }

    /**
     * 文章浏览量 +1
     *
     * @param articleId 文章 ID
     */
    public void incrementViewCount(String articleId) {
        incrementViewCount(articleId, 1L);
    }

    /**
     * 添加文章指定数量观看数
     * @param articleId 文章id
     * @param count     观看数
     */
    public void incrementViewCount(String articleId, Long count) {
        requireArticle(articleId);
        // 按 _id 定位文档，对 viewCount 字段做原子自增
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(articleId)),
                new Update().inc("viewCount", count),
                Article.class
        );
    }

    /**
     * 批量累加文章浏览量（一次 bulkWrite 请求）
     *
     * @param viewCounts 文章 ID -> 浏览增量
     */
    public void incrementViewCountBatch(Map<String, Long> viewCounts) {
        if (viewCounts == null || viewCounts.isEmpty()) {
            return;
        }
        // UNORDERED 模式：各条更新互不依赖，失败时不影响其他条目
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Article.class);
        int ops = 0;
        for (Map.Entry<String, Long> entry : viewCounts.entrySet()) {
            Long count = entry.getValue();
            if (count == null || count <= 0) {
                continue;
            }
            bulkOps.updateOne(
                    Query.query(Criteria.where("_id").is(entry.getKey())),
                    new Update().inc("viewCount", count)
            );
            ops++;
        }
        // 至少有一条有效操作时才发起网络请求
        if (ops > 0) {
            bulkOps.execute();
        }
    }

    /**
     * 调整文章收藏量
     *
     * @param articleId 文章 ID
     * @param delta     增减量（正数增加，负数减少）
     */
    public void adjustFavoriteCount(String articleId, long delta) {
        requireArticle(articleId);
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(articleId)),
                new Update().inc("favoriteCount", delta),
                Article.class
        );
    }

    /**
     * 调整文章评论数
     *
     * @param articleId 文章 ID
     * @param delta     增减量（正数增加，负数减少）
     */
    public void adjustCommentCount(String articleId, long delta) {
        requireArticle(articleId);
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(articleId)),
                new Update().inc("commentCount", delta),
                Article.class
        );
    }

    /**
     * 按关键词搜索已发布文章。
     * 匹配标题或正文，排序优先级：标题匹配度 > 浏览量 > 正文匹配度。
     *
     * @param keyword  搜索关键词
     * @param category 分类（可选）
     * @param tag      标签（可选）
     * @param pageable 分页参数
     * @return 搜索结果分页
     */
    public Page<Article> searchPublishedArticles(String keyword, String category, String tag, Pageable pageable) {
        String trimmedKeyword = keyword.trim();
        if (StringUtils.isEmpty(trimmedKeyword)) {
            return Page.empty(pageable);
        }

        String status = ArticleStatus.PUBLISHED.getCode();
        // Pattern.quote 转义特殊字符，避免用户输入 . * 等破坏正则
        Pattern pattern = Pattern.compile(Pattern.quote(trimmedKeyword), Pattern.CASE_INSENSITIVE);
        // 评分阶段统一用小写比较，与 regex 的 CASE_INSENSITIVE 保持一致
        String keywordLower = trimmedKeyword.toLowerCase(Locale.ROOT);

        // 组装过滤条件：已发布 + 标题或正文命中关键词 + 可选分类/标签
        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.add(Criteria.where("status").is(status));
        andCriteria.add(new Criteria().orOperator(
                Criteria.where("title").regex(pattern),
                Criteria.where("content").regex(pattern)
        ));
        if (StringUtils.isNotEmpty(category)) {
            andCriteria.add(Criteria.where("category").is(category));
        }
        if (StringUtils.isNotEmpty(tag)) {
            andCriteria.add(TagUtils.tagEqualsCriteria(tag));
        }
        Criteria matchCriteria = new Criteria().andOperator(andCriteria.toArray(Criteria[]::new));

        // 自定义聚合阶段：为每条文档计算 titleScore / contentScore
        AggregationOperation addScores = context -> buildSearchScoreFields(keywordLower);

        // 先单独 count，拿到分页所需的 total
        Aggregation countAggregation = Aggregation.newAggregation(
                Aggregation.match(matchCriteria),
                Aggregation.count().as("total")
        );
        Document countResult = mongoTemplate.aggregate(countAggregation, Article.class, Document.class)
                .getUniqueMappedResult();
        long total = countResult == null ? 0L : countResult.get("total", Number.class).longValue();
        if (total == 0L) {
            return Page.empty(pageable);
        }

        // 主查询：过滤 → 打分 → 排序 → 分页
        Aggregation searchAggregation = Aggregation.newAggregation(
                Aggregation.match(matchCriteria),
                addScores,
                Aggregation.sort(Sort.by(
                        Sort.Order.desc("titleScore"),   // 标题命中优先
                        Sort.Order.desc("viewCount"),    // 同标题匹配度下，浏览量高的靠前
                        Sort.Order.desc("contentScore"), // 仅正文命中时，按正文匹配度排
                        Sort.Order.desc("createTime")    // 以上都相同时，新文章靠前
                )),
                Aggregation.skip(pageable.getOffset()),
                Aggregation.limit(pageable.getPageSize())
        );
        List<Article> articles = mongoTemplate.aggregate(searchAggregation, Article.class, Article.class)
                .getMappedResults();
        // titleScore / contentScore 是聚合临时字段，Article 实体无对应属性，映射时自动忽略
        return new PageImpl<>(articles, pageable, total);
    }

    /**
     * 构建 MongoDB $addFields 阶段，计算标题与正文的匹配得分。
     * 得分仅用于排序，不会持久化到 Article 文档。
     */
    private Document buildSearchScoreFields(String keywordLower) {
        // 标题转小写，null 当空串处理
        Document titleLower = new Document("$toLower",
                new Document("$ifNull", List.of("$title", "")));
        // 关键词在标题中首次出现的位置，-1 表示未命中
        Document titleIndex = new Document("$indexOfCP", List.of(titleLower, keywordLower));

        //查询文章内容匹配度
        Document contentLower = new Document("$toLower",
                new Document("$ifNull", List.of("$content", "")));
        Document contentIndex = new Document("$indexOfCP", List.of(contentLower, keywordLower));

        // 标题得分：完全相等 100；包含关键词则 60~90，越靠前分越高；未命中 0
        Document titleScore = new Document("$cond", List.of(
                new Document("$eq", List.of(titleLower, keywordLower)),
                100,
                new Document("$cond", List.of(
                        new Document("$gte", List.of(titleIndex, 0)),
                        new Document("$max", List.of(60,
                                new Document("$subtract", List.of(90, titleIndex)))),
                        0
                ))
        ));

        // 正文得分：包含关键词则 20~50，出现位置越靠前分越高；未命中 0
        Document contentScore = new Document("$cond", List.of(
                new Document("$gte", List.of(contentIndex, 0)),
                new Document("$max", List.of(20,
                        new Document("$subtract", List.of(50,
                                new Document("$min", List.of(30,
                                        new Document("$divide", List.of(contentIndex, 10)))))))),
                0
        ));


        return new Document("$addFields", new Document()
                .append("titleScore", titleScore)
                .append("contentScore", contentScore));
    }

    /**
     * 按规范化标签分页查询已发布文章。
     */
    public Page<Article> findPublishedByTag(String tag, String category, Pageable pageable, Sort sort) {
        Pattern pattern = TagUtils.toEquivalentPattern(tag);
        if (pattern == null) {
            return Page.empty(pageable);
        }

        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.add(Criteria.where("status").is(ArticleStatus.PUBLISHED.getCode()));
        andCriteria.add(Criteria.where("tags").regex(pattern));
        if (StringUtils.isNotEmpty(category)) {
            andCriteria.add(Criteria.where("category").is(category));
        }

        Query query = Query.query(new Criteria().andOperator(andCriteria.toArray(Criteria[]::new)));
        long total = mongoTemplate.count(query, Article.class);
        if (total == 0L) {
            return Page.empty(pageable);
        }

        query.with(pageable);
        if (sort != null) {
            query.with(sort);
        }
        List<Article> articles = mongoTemplate.find(query, Article.class);
        return new PageImpl<>(articles, pageable, total);
    }

    /**
     * 查询全部标签（按名称排序）
     *
     * @return 标签名称、文章数、总浏览量
     */
    public List<Document> listAllTags() {
        return mergeTagStats(listPublishedTagRows(), Comparator.comparing(doc -> doc.getString("name")));
    }

    /**
     * 查询热门标签（按关联文章总浏览量排序）
     *
     * @param limit 返回条数
     * @return 标签名称、文章数、总浏览量
     */
    public List<Document> listHotTags(int limit) {
        int size = Math.max(limit, 1);
        Comparator<Document> byViewDesc = Comparator
                .comparingLong((Document doc) -> doc.get("viewCount", Number.class).longValue())
                .reversed()
                .thenComparing(doc -> doc.getString("name"));
        return mergeTagStats(listPublishedTagRows(), byViewDesc).stream()
                .limit(size)
                .toList();
    }

    private List<Document> listPublishedTagRows() {
        String status = ArticleStatus.PUBLISHED.getCode();
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("status").is(status)),
                Aggregation.unwind("tags"),
                Aggregation.project("viewCount").and("tags").as("name")
        );
        return mongoTemplate.aggregate(aggregation, Article.class, Document.class).getMappedResults();
    }

    private List<Document> mergeTagStats(List<Document> rows, Comparator<Document> comparator) {
        Map<String, Document> merged = new LinkedHashMap<>();
        for (Document row : rows) {
            String key = TagUtils.normalize(row.getString("name"));
            if (StringUtils.isEmpty(key)) {
                continue;
            }
            long viewCount = 0L;
            Number view = row.get("viewCount", Number.class);
            if (view != null) {
                viewCount = view.longValue();
            }

            Document existing = merged.get(key);
            if (existing == null) {
                Document created = new Document();
                created.put("name", key);
                created.put("articleCount", 1L);
                created.put("viewCount", viewCount);
                merged.put(key, created);
                continue;
            }

            existing.put("articleCount", existing.get("articleCount", Number.class).longValue() + 1L);
            existing.put("viewCount", existing.get("viewCount", Number.class).longValue() + viewCount);
        }

        return merged.values().stream()
                .sorted(comparator)
                .toList();
    }
}
