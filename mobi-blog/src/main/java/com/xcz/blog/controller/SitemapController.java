package com.xcz.blog.controller;

import com.xcz.blog.domain.enums.ArticleStatus;
import com.xcz.blog.repository.ArticleRepository;
import com.xcz.commons.security.annotation.Release;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Sitemap 和 robots.txt 控制器
 */
@Release
@Slf4j
@RestController
@RequestMapping("/blog")
@RequiredArgsConstructor
public class SitemapController {

    private final ArticleRepository articleRepository;

    private static final String BASE_URL = "https://www.xcenz.xyz/blog/articles/";

    /**
     * 生成 Sitemap XML
     */
    @GetMapping(value = "sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String sitemap() {
        log.info("[Sitemap] 开始生成 sitemap.xml");
        StringBuilder xml = new StringBuilder();
        try {
            xml.append("""
                    <?xml version="1.0" encoding="UTF-8"?>
                    <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
                    """);

            // 首页
            xml.append(urlTag("https://www.xcenz.xyz/blog/", "1.0", "daily", formatDate(LocalDateTime.now())));

            // 查询文章 - 包 try-catch 防止 MongoDB 异常导致整个 sitemap 失败
            List<com.xcz.blog.domain.mongo.Article> articles = java.util.Collections.emptyList();
            try {
                articles = articleRepository
                        .findByStatusOrderByCreateTimeDesc(ArticleStatus.PUBLISHED.getCode(), PageRequest.of(0, 5000))
                        .getContent();
                log.info("[Sitemap] 查询到 {} 篇文章", articles.size());
            } catch (Exception e) {
                log.error("[Sitemap] 查询文章列表失败（MongoDB 可能未连接），sitemap 将仅包含首页", e);
            }

            for (com.xcz.blog.domain.mongo.Article article : articles) {
                String loc = BASE_URL + article.getId();
                String priority = "0.8";
                String changefreq = "weekly";
                String lastmod = formatDate(article.getUpdateTime());
                xml.append(urlTag(loc, priority, changefreq, lastmod));
            }

            xml.append("</urlset>");
            log.info("[Sitemap] 生成完成，长度={}", xml.length());
        } catch (Exception e) {
            log.error("[Sitemap] 生成失败", e);
            // 兜底：返回只包含首页的最小可用 sitemap
            return """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
                    <url>
                        <loc>https://www.xcenz.xyz/blog/</loc>
                        <priority>1.0</priority>
                        <changefreq>daily</changefreq>
                        <lastmod>%s</lastmod>
                    </url>
                    </urlset>
                    """.formatted(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        return xml.toString();
    }

    /**
     * robots.txt
     */
    @GetMapping(value = "robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String robots() {
        log.info("[Robots] 返回 robots.txt");
        return """
                User-agent: *
                Allow: /blog/
                Disallow: /blog/admin/
                Disallow: /blog/user/

                Sitemap: https://www.xcenz.xyz/blog/sitemap.xml
                """;
    }

    private String urlTag(String loc, String priority, String changefreq, String lastmod) {
        return """
                <url>
                    <loc>%s</loc>
                    <priority>%s</priority>
                    <changefreq>%s</changefreq>
                    <lastmod>%s</lastmod>
                </url>
                """.formatted(loc, priority, changefreq, lastmod);
    }

    private String formatDate(LocalDateTime dt) {
        if (dt == null) {
            return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return dt.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
