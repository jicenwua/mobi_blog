# Spring Boot 集成 MongoDB 实战指南

> 基于 **Spring Boot 3.2** + **Spring Data MongoDB** + **Java 21**  
> 本文档结合 `mobi-blog` 项目中的真实用法，覆盖从入门到高级搜索的完整知识体系。

---

## 目录

1. [概述](#1-概述)
2. [快速集成](#2-快速集成)
3. [实体映射](#3-实体映射)
4. [MongoRepository 基础查询](#4-mongorepository-基础查询)
5. [MongoTemplate 与 Criteria 查询](#5-mongotemplate-与-criteria-查询)
6. [Document（BSON）原生文档](#6-documentbson原生文档)
7. [数据更新与批量操作](#7-数据更新与批量操作)
8. [聚合管道 Aggregation](#8-聚合管道-aggregation)
9. [全文搜索与文本索引](#9-全文搜索与文本索引)
10. [分页与排序](#10-分页与排序)
11. [索引策略](#11-索引策略)
12. [高级特性](#12-高级特性)
13. [生产实践建议](#13-生产实践建议)
14. [附录：项目代码索引](#14-附录项目代码索引)

---

## 1. 概述

### 1.1 什么是 MongoDB

MongoDB 是一种**文档型 NoSQL 数据库**，以 BSON（Binary JSON）格式存储数据。与关系型数据库的「表 + 行」模型不同，MongoDB 使用「集合（Collection）+ 文档（Document）」模型。

| 关系型数据库 | MongoDB |
|-------------|---------|
| Database    | Database |
| Table       | Collection |
| Row         | Document |
| Column      | Field |
| JOIN        | 嵌入文档 / 引用 + 应用层组装 |

### 1.2 Spring Data MongoDB 架构

```
Controller / Service
        ↓
┌───────────────────────────────────────┐
│  MongoRepository（声明式查询）          │
│  MongoTemplate（编程式查询）            │
│    ├── Criteria（过滤条件 / $match）   │
│    └── Document（BSON 原生聚合表达式）  │
└───────────────────────────────────────┘
        ↓
  MongoDB Driver（官方 Java 驱动）
        ↓
      MongoDB Server
```

**选型建议：**

| 场景 | 推荐方式 |
|------|---------|
| 简单 CRUD、按字段名查询 | `MongoRepository` |
| 复杂条件、原子更新、聚合 | `MongoTemplate` |
| 动态多条件拼接 | `Criteria` + `Query` |
| 自定义排序评分、统计 | `Aggregation` |
| 复杂聚合表达式（$cond、$addFields） | `Document` + `AggregationOperation` |

---

## 2. 快速集成

### 2.1 Maven 依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

> `mobi-blog` 项目已引入该依赖，版本由 Spring Boot Parent `3.2.0` 统一管理。

### 2.2 连接配置

**本地开发（application.yaml）：**

```yaml
spring:
  data:
    mongodb:
      # 方式一：URI（推荐）
      uri: mongodb://localhost:27017/mobi_blog

      # 方式二：分项配置
      # host: localhost
      # port: 27017
      # database: mobi_blog
      # username: admin
      # password: secret
      # authentication-database: admin
```

**带认证与副本集：**

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://user:pass@host1:27017,host2:27017/mobi_blog?replicaSet=rs0&authSource=admin
```

**Nacos 远程配置：**  
`mobi-blog` 将 MongoDB 连接信息放在 Nacos 配置中心，本地 `application.yaml` 仅保留服务发现等基础配置，数据库连接通过 `optional:nacos:mobi-blog.yaml` 导入。

### 2.3 启用 Repository 扫描

Spring Boot 自动配置会扫描主类所在包及子包下的 `MongoRepository` 接口，无需额外 `@EnableMongoRepositories`（除非自定义扫描路径）。

```java
@SpringBootApplication
public class MobiBlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(MobiBlogApplication.class, args);
    }
}
```

### 2.4 验证连接

```java
@Autowired
private MongoTemplate mongoTemplate;

@GetMapping("/health/mongo")
public String check() {
    return mongoTemplate.getDb().getName(); // 返回数据库名即表示连接成功
}
```

---

## 3. 实体映射

### 3.1 核心注解

| 注解 | 作用 |
|------|------|
| `@Document` | 映射到 MongoDB 集合 |
| `@Id` | 主键，对应 `_id` 字段 |
| `@Indexed` | 单字段索引（启动时可自动创建） |
| `@CompoundIndex` | 复合索引（加在类上） |
| `@Field("custom_name")` | 自定义 BSON 字段名 |
| `@Transient` | 不持久化，仅运行时使用 |

### 3.2 项目示例：Article 实体

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "blog_article")
public class Article {

    @Id
    private String id;                    // 未指定时 MongoDB 自动生成 ObjectId 字符串

    private String title;
    private String summary;
    private String content;

    @Indexed
    private Long authorId;                // 按作者查询时走索引

    @Transient
    private String authorName;            // 从 MySQL 补全，不写入 MongoDB

    @Indexed
    private String category;

    @Indexed
    private List<String> tags;            // 数组字段，支持 $in / contains 查询

    private Long viewCount;
    private Long favoriteCount;
    private Long commentCount;

    @Indexed
    private String status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

### 3.3 主键策略

```java
// 默认：String 类型 → ObjectId 的十六进制字符串
@Id
private String id;

// 自定义 ObjectId
@Id
private ObjectId id;

// 业务自定义 ID（如雪花算法）
@Id
private Long id;
```

### 3.4 嵌套文档与引用

**嵌入（适合一对少、读多写少）：**

```java
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    private List<OrderItem> items;  // 嵌套数组
}

public class OrderItem {
    private String productId;
    private Integer quantity;
}
```

**引用（适合一对多、独立生命周期）：**

```java
// 评论独立集合，通过 articleId 关联文章（mobi-blog 采用此方式）
@Document(collection = "blog_comment")
public class Comment {
    @Id
    private String id;
    @Indexed
    private String articleId;
    private String content;
}
```

---

## 4. MongoRepository 基础查询

### 4.1 定义 Repository

```java
public interface ArticleRepository extends MongoRepository<Article, String> {
    // 方法名即查询，Spring Data 自动解析
}
```

`MongoRepository` 已内置：`save`、`findById`、`findAll`、`deleteById`、`count` 等。

### 4.2 方法名派生查询（Query Derivation）

命名规则：`find + By + 字段名 + 条件关键字 + OrderBy + 排序字段`

| 关键字 | 示例 | 生成的查询语义 |
|--------|------|---------------|
| `And` / `Or` | `findByStatusAndCategory` | `$and` |
| `Is` / `Equals` | `findByStatus` | `status = ?` |
| `Between` | `findByCreateTimeBetween` | 范围 |
| `LessThan` / `GreaterThan` | `findByViewCountGreaterThan` | 比较 |
| `Like` / `Containing` | `findByTitleContaining` | 正则模糊 |
| `In` | `findByStatusIn` | `$in` |
| `True` / `False` | `findByPublishedTrue` | 布尔 |
| `OrderByXxxDesc` | `OrderByCreateTimeDesc` | 降序 |

### 4.3 项目中的 Repository 示例

```java
public interface ArticleRepository extends MongoRepository<Article, String> {

    Page<Article> findByStatusOrderByCreateTimeDesc(String status, Pageable pageable);

    Page<Article> findByStatusAndCategoryOrderByCreateTimeDesc(
            String status, String category, Pageable pageable);

    Page<Article> findByStatusAndTagsContainingOrderByCreateTimeDesc(
            String status, String tag, Pageable pageable);

    Page<Article> findByStatusOrderByViewCountDesc(String status, Pageable pageable);
}
```

**调用示例：**

```java
Pageable pageable = PageRequest.of(0, 10);
Page<Article> page = articleRepository
        .findByStatusAndTagsContainingOrderByCreateTimeDesc("PUBLISHED", "Redis", pageable);
```

### 4.4 @Query 注解（自定义查询）

当方法名过长或需要原生操作符时：

```java
@Query("{ 'status': ?0, 'viewCount': { $gte: ?1 } }")
List<Article> findHotArticles(String status, long minViews);

@Query(value = "{ 'authorId': ?0 }", fields = "{ 'title': 1, 'createTime': 1 }")
List<Article> findTitlesByAuthor(Long authorId);

@Query("{ 'title': { $regex: ?0, $options: 'i' } }")
Page<Article> searchByTitleRegex(String keyword, Pageable pageable);
```

### 4.5 @Aggregation 注解

简单聚合可直接写在 Repository：

```java
@Aggregation(pipeline = {
    "{ $match: { status: ?0 } }",
    "{ $group: { _id: '$category', count: { $sum: 1 } } }",
    "{ $sort: { count: -1 } }"
})
List<CategoryCount> countByCategory(String status);
```

---

## 5. MongoTemplate 与 Criteria 查询

当 Repository 方法名无法满足需求时，使用 `MongoTemplate` 进行编程式查询。

### 5.1 基本查询

```java
@Autowired
private MongoTemplate mongoTemplate;

// 按 ID 查询
Article article = mongoTemplate.findById(articleId, Article.class);

// 单条件
Query query = Query.query(Criteria.where("status").is("PUBLISHED"));
List<Article> list = mongoTemplate.find(query, Article.class);

// 多条件 AND
Criteria criteria = new Criteria().andOperator(
    Criteria.where("status").is("PUBLISHED"),
    Criteria.where("category").is("TECH"),
    Criteria.where("viewCount").gte(100)
);
List<Article> hot = mongoTemplate.find(Query.query(criteria), Article.class);
```

### 5.2 常用 Criteria 操作符

```java
// 等于 / 不等于
Criteria.where("status").is("PUBLISHED")
Criteria.where("status").ne("DRAFT")

// 比较
Criteria.where("viewCount").gt(0).lte(10000)

// IN / NIN
Criteria.where("category").in("TECH", "LIFE")

// 数组包含（tags 数组中有 "Redis"）
Criteria.where("tags").is("Redis")           // 精确匹配数组元素
Criteria.where("tags").all("Java", "Spring") // 同时包含多个

// OR
new Criteria().orOperator(
    Criteria.where("title").regex(pattern),
    Criteria.where("content").regex(pattern)
)

// 存在性
Criteria.where("summary").exists(true)

// 正则（模糊搜索）
Pattern pattern = Pattern.compile("Spring", Pattern.CASE_INSENSITIVE);
Criteria.where("title").regex(pattern)

// 安全正则：转义用户输入的特殊字符
Pattern safe = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
```

### 5.3 字段投影（只查部分字段）

```java
Query query = Query.query(Criteria.where("status").is("PUBLISHED"));
query.fields()
     .include("title", "summary", "createTime")
     .exclude("_id");  // 可选
List<Article> titles = mongoTemplate.find(query, Article.class);
```

### 5.4 排序与限制

```java
Query query = Query.query(Criteria.where("status").is("PUBLISHED"));
query.with(Sort.by(Sort.Direction.DESC, "viewCount", "createTime"));
query.limit(10);
query.skip(20);
```

---

## 6. Document（BSON）原生文档

`org.bson.Document` 是 MongoDB Java 驱动提供的 **BSON 文档类**，本质上就是一个 `Map<String, Object>`，可以表示 MongoDB 中的任意 JSON/BSON 结构。

Spring Data 的 `Criteria`、`Query`、`Update` 等高层 API 在底层最终也会被转换成 `Document` 发给 MongoDB。当你需要写 **Criteria 无法表达的聚合表达式** 时，就需要直接操作 `Document`。

> **注意区分两个「Document」：**
> - `@Document`（`org.springframework.data.mongodb.core.mapping.Document`）→ 实体类注解，映射 Java 对象到集合
> - `Document`（`org.bson.Document`）→ BSON 数据结构，用于原生查询/聚合

### 6.1 Document 与 Criteria 的分工

| 维度 | `Criteria` | `org.bson.Document` |
|------|-----------|---------------------|
| 所属层次 | Spring Data 查询抽象 | MongoDB 驱动原生 BSON |
| 主要用途 | 过滤条件（WHERE） | 聚合管道阶段、复杂表达式 |
| 典型场景 | `find`、`update`、`$match` | `$addFields`、`$project`、`$group` 自定义逻辑 |
| 支持的操作 | `$eq`、`$regex`、`$and`、`$or` 等查询操作符 | `$cond`、`$toLower`、`$indexOfCP` 等聚合表达式 |
| 类型安全 | 较好（链式 API） | 无（字符串 key，运行时校验） |

**选型口诀：**

- **「找哪些文档」** → 用 `Criteria`
- **「找到后怎么算」** → 用 `Document`

### 6.2 Document 基本用法

```java
import org.bson.Document;

// 构建查询文档（等价于 Criteria.where("status").is("PUBLISHED")）
Document query = new Document("status", "PUBLISHED");

// 嵌套操作符
Document regexQuery = new Document("title",
    new Document("$regex", "Spring").append("$options", "i"));

// 组合 AND 条件
Document andQuery = new Document("$and", List.of(
    new Document("status", "PUBLISHED"),
    new Document("viewCount", new Document("$gte", 100))
));

// 读取字段
String status = doc.getString("status");
long total = doc.get("total", Number.class).longValue();

// 链式 append
Document stage = new Document("$match", new Document("status", "PUBLISHED"));
```

**用 MongoTemplate 执行原生查询：**

```java
// 原生 find（一般不推荐，优先用 Criteria + Query）
List<Document> docs = mongoTemplate.getCollection("blog_article")
    .find(new Document("status", "PUBLISHED"))
    .into(new ArrayList<>());

// 更常见的做法：聚合结果映射为 Document
Document result = mongoTemplate
    .aggregate(aggregation, Article.class, Document.class)
    .getUniqueMappedResult();
```

### 6.3 在聚合管道中使用 Document

聚合管道的每一个 Stage 都是一个 BSON 文档。Spring Data 内置了 `Aggregation.match()`、`Aggregation.sort()` 等快捷方法，但对于复杂 Stage，可以通过 **自定义 `AggregationOperation`** 直接返回 `Document`：

```java
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

// 方式一：Lambda 简写
AggregationOperation addScores = context -> buildSearchScoreFields(keywordLower);

// 方式二：匿名类（等效）
AggregationOperation customStage = new AggregationOperation() {
    @Override
    public Document toDocument(AggregationOperationContext context) {
        return buildSearchScoreFields(keywordLower);
    }
};

Aggregation agg = Aggregation.newAggregation(
    Aggregation.match(matchCriteria),   // Criteria  → $match
    addScores,                           // Document  → $addFields
    Aggregation.sort(sort),
    Aggregation.skip(offset),
    Aggregation.limit(size)
);
```

`AggregationOperation` 的 `toDocument()` 方法要求返回 **完整的 pipeline stage**，例如：

```json
{ "$addFields": { "titleScore": { ... }, "contentScore": { ... } } }
```

而不是只返回 stage 内部的字段部分。

### 6.4 项目实战：buildSearchScoreFields 详解

以下是 `ArticleMongoSupport` 中使用 `Document` 构建自定义评分逻辑的完整示例及逐行说明：

```java
private Document buildSearchScoreFields(String keywordLower) {
    // $toLower：将标题转为小写；$ifNull：null 时当空串
    Document titleLower = new Document("$toLower",
            new Document("$ifNull", List.of("$title", "")));

    // $indexOfCP：按 Unicode 码点查找子串位置，-1 表示未命中
    Document titleIndex = new Document("$indexOfCP", List.of(titleLower, keywordLower));

    Document contentLower = new Document("$toLower",
            new Document("$ifNull", List.of("$content", "")));
    Document contentIndex = new Document("$indexOfCP", List.of(contentLower, keywordLower));

    // $cond：[条件, 真值, 假值] — 嵌套实现多级评分
    Document titleScore = new Document("$cond", List.of(
            new Document("$eq", List.of(titleLower, keywordLower)),  // 完全匹配
            100,
            new Document("$cond", List.of(
                    new Document("$gte", List.of(titleIndex, 0)),  // 包含关键词
                    new Document("$max", List.of(60,
                            new Document("$subtract", List.of(90, titleIndex)))), // 越靠前分越高
                    0                                                // 未命中
            ))
    ));

    Document contentScore = new Document("$cond", List.of(
            new Document("$gte", List.of(contentIndex, 0)),
            new Document("$max", List.of(20,
                    new Document("$subtract", List.of(50,
                            new Document("$min", List.of(30,
                                    new Document("$divide", List.of(contentIndex, 10)))))))),
            0
    ));

    // 返回完整的 $addFields stage
    return new Document("$addFields", new Document()
            .append("titleScore", titleScore)
            .append("contentScore", contentScore));
}
```

**对应的 MongoDB 聚合表达式（等效 JSON）：**

```json
{
  "$addFields": {
    "titleScore": {
      "$cond": [
        { "$eq": ["$titleLower", "spring"] },
        100,
        { "$cond": [
          { "$gte": ["$titleIndex", 0] },
          { "$max": [60, { "$subtract": [90, "$titleIndex"] }] },
          0
        ]}
      ]
    },
    "contentScore": { ... }
  }
}
```

**为什么这里不能用 Criteria？**

`Criteria` 只能表达「字段 vs 值」的匹配关系，无法表达 `$cond`、`$indexOfCP`、`$toLower` 这类 **在聚合管道中对字段做计算的表达式**。这些属于 Aggregation Expression，必须用 `Document` 或 Spring Data 的 `AggregationExpression` 系列 API。

### 6.5 接收聚合结果：Document 作为返回类型

当聚合结果无法映射到实体类时，用 `Document.class` 接收：

```java
// count 结果：{ "total": 42 }
Document countResult = mongoTemplate
    .aggregate(countAggregation, Article.class, Document.class)
    .getUniqueMappedResult();
long total = countResult == null ? 0L : countResult.get("total", Number.class).longValue();

// group 结果：{ "_id": "TECH", "count": 15 }
List<Document> groups = mongoTemplate
    .aggregate(groupAgg, Article.class, Document.class)
    .getMappedResults();
for (Document doc : groups) {
    String category = doc.getString("_id");
    int count = doc.getInteger("count");
}
```

**何时用实体类 vs Document：**

| 返回类型 | 适用场景 |
|---------|---------|
| `Article.class` | 结果字段与实体一致（临时字段如 `titleScore` 会被忽略） |
| `Document.class` | 统计结果、分组结果、字段结构与实体不一致 |
| 自定义 DTO | 结构固定且需类型安全时（如 `CategoryCount`） |

### 6.6 Document vs Spring Data 聚合 Fluent API

Spring Data 也提供了部分聚合表达式的 Java API，与 `Document` 二选一：

```java
// Fluent API 写法（简单场景）
Aggregation.addFields()
    .addField("fullName")
    .withValue(StringOperators.Concat.valueOf("$firstName").concat(" ").concatValueOf("$lastName"))
    .build();

// Document 写法（复杂嵌套场景，更直观）
new Document("$addFields", new Document("fullName",
    new Document("$concat", List.of("$firstName", " ", "$lastName"))));
```

| 场景 | 推荐 |
|------|------|
| 简单 `$addFields`、`$project` | Fluent API |
| 多层 `$cond` 嵌套、自定义评分 | `Document` |
| 需要与 mongo shell 文档对照调试 | `Document`（JSON 结构一致） |

### 6.7 调试技巧

在开发阶段，可将 `Document` 直接打印为 JSON 对照 mongo shell 验证：

```java
Document stage = buildSearchScoreFields("spring");
System.out.println(stage.toJson());  // 输出完整 BSON，可复制到 Compass / shell 测试
```

在 MongoDB Compass 的 Aggregation 面板中粘贴 pipeline JSON，可快速验证逻辑是否正确，无需重启应用。

---

## 7. 数据更新与批量操作

### 7.1 原子自增（$inc）

适合计数器场景，避免「读-改-写」竞态：

```java
// 浏览量 +1
mongoTemplate.updateFirst(
    Query.query(Criteria.where("_id").is(articleId)),
    new Update().inc("viewCount", 1),
    Article.class
);

// 收藏量增减
mongoTemplate.updateFirst(
    Query.query(Criteria.where("_id").is(articleId)),
    new Update().inc("favoriteCount", delta),  // delta 可为负数
    Article.class
);
```

### 7.2 其他 Update 操作

```java
Update update = new Update()
    .set("title", "新标题")
    .set("updateTime", LocalDateTime.now())
    .unset("deprecatedField")           // 删除字段
    .push("tags").value("新标签")        // 数组追加
    .pull("tags", "旧标签")             // 数组移除
    .addToSet("tags").each("A", "B")    // 去重追加
    .inc("viewCount", 1)
    .mul("score", 1.1)                  // 乘法
    .min("viewCount", 0);               // 保证不小于 0

mongoTemplate.updateFirst(query, update, Article.class);
```

### 7.3 upsert（不存在则插入）

```java
mongoTemplate.upsert(
    Query.query(Criteria.where("_id").is(id)),
    new Update().set("viewCount", 0).setOnInsert("createTime", LocalDateTime.now()),
    Article.class
);
```

### 7.4 批量写入 BulkOperations

一次网络往返处理多条更新，适合高吞吐场景：

```java
BulkOperations bulkOps = mongoTemplate.bulkOps(
    BulkOperations.BulkMode.UNORDERED,  // 无序：某条失败不影响其他
    Article.class
);

for (Map.Entry<String, Long> entry : viewCounts.entrySet()) {
    bulkOps.updateOne(
        Query.query(Criteria.where("_id").is(entry.getKey())),
        new Update().inc("viewCount", entry.getValue())
    );
}
bulkOps.execute();
```

| BulkMode | 说明 |
|----------|------|
| `ORDERED` | 有序执行，遇错停止 |
| `UNORDERED` | 并行执行，性能更好 |

---

## 8. 聚合管道 Aggregation

聚合管道是 MongoDB 最强大的数据分析与复杂搜索工具，数据依次经过多个 Stage 处理。

### 8.1 常用 Stage

| Stage | 作用 |
|-------|------|
| `$match` | 过滤文档（类似 WHERE） |
| `$project` | 字段投影、计算新字段 |
| `$group` | 分组统计 |
| `$sort` | 排序 |
| `$skip` / `$limit` | 分页 |
| `$lookup` | 左连接（类似 JOIN） |
| `$unwind` | 展开数组 |
| `$addFields` | 添加计算字段 |
| `$count` | 计数 |
| `$facet` | 单次查询多路聚合 |

### 8.2 项目实战：关键词搜索 + 自定义评分排序

> 自定义评分阶段使用 `Document` 构建，详见 [第 6 节 Document（BSON）原生文档](#6-documentbson原生文档)。

`ArticleMongoSupport.searchPublishedArticles` 实现了：

1. 过滤：已发布 + 标题/正文命中关键词 + 可选分类/标签
2. 打分：标题匹配优先于正文匹配
3. 排序：标题分 > 浏览量 > 正文分 > 创建时间
4. 分页：先 count 总数，再 skip/limit

**核心代码结构：**

```java
// 1. 组装 match 条件
Criteria matchCriteria = new Criteria().andOperator(
    Criteria.where("status").is("PUBLISHED"),
    new Criteria().orOperator(
        Criteria.where("title").regex(pattern),
        Criteria.where("content").regex(pattern)
    ),
    // 可选：category、tag
);

// 2. 统计总数
Aggregation countAgg = Aggregation.newAggregation(
    Aggregation.match(matchCriteria),
    Aggregation.count().as("total")
);
Document countResult = mongoTemplate
    .aggregate(countAgg, Article.class, Document.class)
    .getUniqueMappedResult();
long total = countResult.get("total", Number.class).longValue();

// 3. 搜索 + 打分 + 排序 + 分页
Aggregation searchAgg = Aggregation.newAggregation(
    Aggregation.match(matchCriteria),
    context -> buildSearchScoreFields(keywordLower),  // 自定义 $addFields
    Aggregation.sort(Sort.by(
        Sort.Order.desc("titleScore"),
        Sort.Order.desc("viewCount"),
        Sort.Order.desc("contentScore"),
        Sort.Order.desc("createTime")
    )),
    Aggregation.skip(pageable.getOffset()),
    Aggregation.limit(pageable.getPageSize())
);
List<Article> articles = mongoTemplate
    .aggregate(searchAgg, Article.class, Article.class)
    .getMappedResults();

return new PageImpl<>(articles, pageable, total);
```

**自定义 $addFields 打分逻辑（简化说明）：**

> 完整 `Document` 构建代码见 [6.4 节](#64-项目实战buildsearchscorefields-详解)。

```
titleScore:
  - 标题完全等于关键词 → 100
  - 标题包含关键词     → 60~90（越靠前分越高）
  - 未命中              → 0

contentScore:
  - 正文包含关键词     → 20~50（越靠前分越高）
  - 未命中              → 0
```

### 8.3 分组统计示例

```java
// 统计各分类文章数量
Aggregation agg = Aggregation.newAggregation(
    Aggregation.match(Criteria.where("status").is("PUBLISHED")),
    Aggregation.group("category").count().as("count"),
    Aggregation.sort(Sort.Direction.DESC, "count")
);

List<Document> results = mongoTemplate
    .aggregate(agg, Article.class, Document.class)
    .getMappedResults();
// 结果: [{ _id: "TECH", count: 42 }, ...]
```

### 8.4 $lookup 关联查询

```java
// 查询文章及其评论数（评论在独立集合时）
Aggregation agg = Aggregation.newAggregation(
    Aggregation.match(Criteria.where("status").is("PUBLISHED")),
    Aggregation.lookup("blog_comment", "_id", "articleId", "comments"),
    Aggregation.addFields()
        .addField("commentCount")
        .withValue(ArrayOperators.Size.lengthOfArray("comments"))
        .build(),
    Aggregation.project("title", "viewCount", "commentCount")
);
```

### 8.5 $facet 一次查询返回多组结果

```java
// 同时返回「最新 5 篇」和「热门 5 篇」
Aggregation agg = Aggregation.newAggregation(
    Aggregation.match(Criteria.where("status").is("PUBLISHED")),
    Aggregation.facet()
        .and(Aggregation.sort(Sort.Direction.DESC, "createTime"),
             Aggregation.limit(5)).as("latest")
        .and(Aggregation.sort(Sort.Direction.DESC, "viewCount"),
             Aggregation.limit(5)).as("hot")
);
```

---

## 9. 全文搜索与文本索引

### 9.1 MongoDB 文本索引（$text / $search）

适合对多个文本字段做分词搜索，支持相关性评分 `$meta: "textScore"`。

**创建文本索引：**

```java
@Document(collection = "blog_article")
@CompoundIndex(
    name = "text_search_idx",
    def = "{'title': 'text', 'content': 'text', 'summary': 'text'}"
)
public class Article { ... }
```

或通过 `MongoTemplate`：

```java
IndexOperations indexOps = mongoTemplate.indexOps(Article.class);
indexOps.ensureIndex(new Index()
    .on("title", Sort.Direction.ASC)
    .on("content", Sort.Direction.ASC)
    .named("article_text_idx"));
// 文本索引需使用 TextIndexDefinition
indexOps.ensureIndex(new TextIndexDefinition.TextIndexDefinitionBuilder()
    .onField("title")
    .onField("content")
    .build());
```

**文本搜索查询：**

```java
TextCriteria criteria = TextCriteria.forDefaultLanguage()
    .matchingAny("Spring", "MongoDB");  // 任一词命中

Query query = TextQuery.queryText(criteria)
    .sortByScore()
    .with(PageRequest.of(0, 10));

List<Article> results = mongoTemplate.find(query, Article.class);
```

**Repository 方式：**

```java
@Query("{ $text: { $search: ?0 } }")
List<Article> searchByText(String keyword);
```

### 9.2 正则搜索 vs 文本索引

| 方式 | 优点 | 缺点 | 适用场景 |
|------|------|------|---------|
| `$regex` | 灵活、支持子串、可自定义评分 | 全表扫描（无索引时）、性能差 | 小数据量、自定义排序逻辑 |
| `$text` | 分词、有 textScore、可走索引 | 不支持子串、中文分词依赖语言设置 | 关键词搜索、文章检索 |
| **Atlas Search** | 功能最强（同义词、高亮、中文分词） | 需 MongoDB Atlas | 生产级全文检索 |

> `mobi-blog` 当前使用 **regex + 聚合自定义评分**，在数据量适中时灵活可控；数据量增长到百万级建议迁移到文本索引或 Atlas Search。

### 9.3 中文搜索注意点

- MongoDB 默认文本索引对中文分词支持有限（按空格/标点切分）
- 中文生产环境常见方案：
  - **Elasticsearch / OpenSearch** 做搜索专库
  - **MongoDB Atlas Search** 内置 Lucene 分词
  - 应用层用 **regex**（小数据量）或 **预分词字段**（存储关键词数组）

---

## 10. 分页与排序

### 10.1 Repository 分页

```java
Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
Page<Article> page = articleRepository.findByStatusOrderByCreateTimeDesc("PUBLISHED", pageable);

// 读取结果
List<Article> content = page.getContent();
long total = page.getTotalElements();
int totalPages = page.getTotalPages();
```

### 10.2 MongoTemplate 分页

```java
Query query = Query.query(criteria).with(pageable);
List<Article> list = mongoTemplate.find(query, Article.class);
long total = mongoTemplate.count(Query.query(criteria), Article.class);
return new PageImpl<>(list, pageable, total);
```

### 10.3 聚合分页（大结果集推荐）

聚合中分页需**先 count 再 skip/limit**（与 `mobi-blog` 搜索实现一致），避免 `$facet` 不必要的内存开销：

```java
// Step 1: count
Aggregation countAgg = Aggregation.newAggregation(
    Aggregation.match(criteria),
    Aggregation.count().as("total")
);

// Step 2: data
Aggregation dataAgg = Aggregation.newAggregation(
    Aggregation.match(criteria),
    Aggregation.sort(sort),
    Aggregation.skip(offset),
    Aggregation.limit(pageSize)
);
```

---

## 11. 索引策略

### 11.1 单字段索引

```java
@Indexed
private String status;

@Indexed(unique = true)
private String slug;
```

### 11.2 复合索引

```java
@Document(collection = "blog_article")
@CompoundIndex(name = "status_category_time", def = "{'status': 1, 'category': 1, 'createTime': -1}")
public class Article { ... }
```

**ESR 原则（Equality → Sort → Range）：**

查询 `{ status: "PUBLISHED", category: "TECH", createTime: { $gte: ... } }`  
推荐索引：`{ status: 1, category: 1, createTime: -1 }`

### 11.3 查看执行计划

```java
// 在 mongo shell 中
db.blog_article.find({ status: "PUBLISHED" }).explain("executionStats")
```

关注 `totalDocsExamined` vs `nReturned`，比值接近 1 说明索引有效。

### 11.4 mobi-blog 推荐索引

```javascript
// blog_article
db.blog_article.createIndex({ status: 1, createTime: -1 })
db.blog_article.createIndex({ status: 1, category: 1, createTime: -1 })
db.blog_article.createIndex({ status: 1, tags: 1, createTime: -1 })
db.blog_article.createIndex({ status: 1, viewCount: -1 })

// blog_comment
db.blog_comment.createIndex({ articleId: 1, createTime: -1 })
db.blog_comment.createIndex({ parentId: 1 })
```

---

## 12. 高级特性

### 12.1 事务（Multi-Document Transaction）

MongoDB 4.0+ 副本集支持多文档事务，Spring 中使用 `@Transactional`：

```java
@Configuration
public class MongoConfig {
    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
}

@Service
public class ArticleService {
    @Transactional
    public void publishWithLog(Article article, PublishLog log) {
        articleRepository.save(article);
        logRepository.save(log);
        // 任一失败则全部回滚
    }
}
```

> 事务有性能开销，仅在强一致性跨集合写入时使用。

### 12.2 Change Streams（变更流）

监听集合实时变更，适合缓存失效、同步搜索索引：

```java
@Component
public class ArticleChangeListener {

    @PostConstruct
    public void watch() {
        ChangeStreamRequest<Document> request = ChangeStreamRequest.builder(
            () -> new RequestStream<Document>() {
                @Override
                public ChangeStreamIterable<Document> stream() {
                    return mongoTemplate.getCollection("blog_article")
                        .watch(Document.class);
                }
            }
        ).build();

        // 或使用 ReactiveMongoTemplate 的 changeStream 更简洁
    }
}
```

### 12.3 GridFS（大文件存储）

适合存储超过 16MB 的文件（视频、大图），`mobi-blog` 使用 OSS，此处了解即可：

```java
@Autowired
private GridFsTemplate gridFsTemplate;

// 存储
ObjectId id = gridFsTemplate.store(inputStream, "photo.jpg", "image/jpeg");

// 读取
GridFsResource resource = gridFsTemplate.getResource(id.toString());
```

### 12.4 审计字段（@CreatedDate / @LastModifiedDate）

```java
@EnableMongoAuditing
@SpringBootApplication
public class Application { }

@Document(collection = "blog_article")
public class Article {
    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;
}
```

### 12.5 Reactive MongoDB（响应式）

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
</dependency>
```

```java
public interface ArticleReactiveRepository extends ReactiveMongoRepository<Article, String> {}

// 返回 Mono / Flux
Mono<Article> findById(String id);
Flux<Article> findByStatus(String status);
```

### 12.6 多数据源 MongoDB

```java
@Configuration
@EnableMongoRepositories(
    basePackages = "com.xcz.blog.repository",
    mongoTemplateRef = "blogMongoTemplate"
)
public class BlogMongoConfig {
    @Bean
    @Primary
    public MongoDatabaseFactory blogMongoFactory() {
        return new SimpleMongoClientDatabaseFactory("mongodb://localhost:27017/mobi_blog");
    }

    @Bean(name = "blogMongoTemplate")
    public MongoTemplate blogMongoTemplate() {
        return new MongoTemplate(blogMongoFactory());
    }
}
```

### 12.7 地理空间查询（了解）

```java
// 附近的文章（假设有 location 字段）
NearQuery nearQuery = NearQuery.near(new GeoJsonPoint(116.4, 39.9))
    .maxDistance(new Distance(5, Metrics.KILOMETERS));
GeoResults<Article> results = mongoTemplate.geoNear(nearQuery, Article.class);
```

---

## 13. 生产实践建议

### 13.1 设计原则

1. **按访问模式建集合**：高频一起读的数据可嵌入，独立更新的数据用引用
2. **避免过大文档**：单文档上限 16MB，评论、日志等应独立集合
3. **计数器用 `$inc`**：不要用 read-modify-write
4. **敏感字段**：`authorName` 等跨库数据用 `@Transient`，查询后补全

### 13.2 性能清单

- [ ] 为高频查询字段建复合索引
- [ ] 避免无索引的 `$regex` 前缀通配（`/.*keyword.*/`）
- [ ] 大列表用投影减少网络传输
- [ ] 批量操作用 `BulkOperations`
- [ ] 监控慢查询（`profiler.setLevel(1)`）

### 13.3 安全

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://user:${MONGO_PASSWORD}@host:27017/db?authSource=admin
```

- 生产禁用无认证暴露
- 使用最小权限数据库用户
- 敏感配置放 Nacos / 环境变量，不入 Git

### 13.4 与 MySQL 混用（mobi-blog 架构）

```
MySQL (commons-database)     MongoDB
├── blog_user (用户)    ←──→  Article.authorId
├── blog_favorite       ←──→  Article.id
└── 事务性强数据              Comment.articleId

查询流程：MongoDB 查文章/评论 → UserDisplaySupport 批量补全昵称
```

这种「MongoDB 存内容 + MySQL 存关系」是博客/内容系统的常见架构。

---

## 14. 附录：项目代码索引

| 文件 | 说明 |
|------|------|
| `domain/mongo/Article.java` | 文章实体映射 |
| `domain/mongo/Comment.java` | 评论实体映射 |
| `repository/ArticleRepository.java` | 声明式分页查询 |
| `support/ArticleMongoSupport.java` | 原子更新、批量写入、聚合搜索（Criteria + Document） |
| `pom.xml` | `spring-boot-starter-data-mongodb` 依赖 |

### 快速参考：四种方式对照

```java
// ① Repository — 简单分页列表
articleRepository.findByStatusOrderByCreateTimeDesc("PUBLISHED", pageable);

// ② Criteria + MongoTemplate — 条件查询 / 原子更新
mongoTemplate.updateFirst(
    Query.query(Criteria.where("_id").is(id)),
    new Update().inc("viewCount", 1),
    Article.class);

// ③ Document + Aggregation — 复杂聚合表达式（自定义评分）
AggregationOperation addScores = context -> buildSearchScoreFields(keywordLower);

// ④ 组合使用 — 搜索（Criteria 过滤 + Document 打分 + 排序分页）
articleMongoSupport.searchPublishedArticles(keyword, category, tag, pageable);
```

---

## 参考链接

- [Spring Data MongoDB 官方文档](https://docs.spring.io/spring-data/mongodb/reference/)
- [MongoDB 官方文档](https://www.mongodb.com/docs/)
- [MongoDB 聚合管道](https://www.mongodb.com/docs/manual/core/aggregation-pipeline/)
- [MongoDB 索引策略](https://www.mongodb.com/docs/manual/applications/indexes/)

---

*文档更新时间：2026-07-09 | 对应项目：mobi-blog (Spring Boot 3.2.0)*
