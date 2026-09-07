<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Bell,
  ChatDotRound,
  CopyDocument,
  Link,
  Share,
  Star,
  StarFilled,
} from '@element-plus/icons-vue'
import { MdCatalog, MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { fetchArticleDetail } from '@/api/article'
import { fetchFavoriteStatus } from '@/api/favorite'
import FavoriteFolderDialog from '@/components/favorite/FavoriteFolderDialog.vue'
import ArticleCommentSection from '@/components/article/ArticleCommentSection.vue'
import { getCategoryLabel } from '@/constants/categories'
import { SITE_NAME } from '@/constants/site'
import { useUserStore } from '@/store/user'
import { countWords, formatDate, formatMonthDay } from '@/utils/format'

const PREVIEW_ID = 'article-detail-preview'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const article = ref(null)
const loading = ref(false)
const isFavorited = ref(false)
const favoriteDialogVisible = ref(false)
const hasCatalog = ref(false)
const commentSectionRef = ref(null)

const articleId = computed(() => route.params.id)
const wordCount = computed(() => countWords(article.value?.content))
const articleUrl = computed(() => {
  if (typeof window === 'undefined') return ''
  return `${window.location.origin}${import.meta.env.BASE_URL}articles/${articleId.value}`
})

async function loadArticle() {
  if (!articleId.value) return
  loading.value = true
  hasCatalog.value = false
  try {
    article.value = await fetchArticleDetail(articleId.value)
    document.title = `${article.value.title} - ${SITE_NAME}`
    await loadFavoriteStatus()
  } catch {
    article.value = null
  } finally {
    loading.value = false
  }
}

async function loadFavoriteStatus() {
  if (!userStore.isLoggedIn || !articleId.value) {
    isFavorited.value = false
    return
  }
  try {
    isFavorited.value = Boolean(await fetchFavoriteStatus(articleId.value))
  } catch {
    isFavorited.value = false
  }
}

async function handleOpenFavoriteDialog() {
  if (!userStore.isLoggedIn) {
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  favoriteDialogVisible.value = true
}

function handleFavoriteUpdated({ isFavorited: favorited }) {
  const wasFavorited = isFavorited.value
  isFavorited.value = favorited
  if (article.value && wasFavorited !== favorited) {
    const delta = favorited ? 1 : -1
    article.value.favoriteCount = Math.max(0, (article.value.favoriteCount ?? 0) + delta)
  }
}

function scrollToComments() {
  commentSectionRef.value?.$el?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

async function copyArticleLink() {
  try {
    await navigator.clipboard.writeText(articleUrl.value)
    ElMessage.success('链接已复制')
  } catch {
    ElMessage.error('复制失败，请手动复制地址栏链接')
  }
}

function handleGetCatalog(list) {
  hasCatalog.value = Array.isArray(list) && list.length > 0
}

onMounted(loadArticle)

watch(articleId, loadArticle)

watch(() => userStore.isLoggedIn, loadFavoriteStatus)
</script>

<template>
  <section v-loading="loading" class="article-detail">
    <el-empty v-if="!loading && !article" description="文章不存在或已被删除" />

    <template v-if="article">
      <aside v-if="hasCatalog" class="article-detail__toc">
        <div class="article-detail__toc-title">目录</div>
        <MdCatalog
          :editor-id="PREVIEW_ID"
          scroll-element="html"
          :scroll-element-offset-top="32"
          :offset-top="32"
          class="article-detail__catalog"
        />
      </aside>

      <aside class="article-detail__actions">
        <button
          type="button"
          class="action-btn"
          :class="{ 'action-btn--active': isFavorited }"
          :title="isFavorited ? '已收藏' : '收藏'"
          @click="handleOpenFavoriteDialog"
        >
          <el-icon>
            <StarFilled v-if="isFavorited" />
            <Star v-else />
          </el-icon>
        </button>
        <button type="button" class="action-btn" title="评论" @click="scrollToComments">
          <el-icon><ChatDotRound /></el-icon>
        </button>
        <button type="button" class="action-btn" title="分享" @click="copyArticleLink">
          <el-icon><Share /></el-icon>
        </button>
      </aside>

      <div class="article-detail__main">
      <article class="article-detail__card">
        <div class="article-detail__tags">
          <el-tag v-if="article.category" size="small" effect="dark" round>
            {{ getCategoryLabel(article.category) }}
          </el-tag>
          <el-tag
            v-for="tag in article.tags || []"
            :key="tag"
            size="small"
            effect="dark"
            round
          >
            {{ tag }}
          </el-tag>
        </div>

        <h1 class="article-detail__title">{{ article.title }}</h1>

        <div class="article-detail__meta">
          <div class="article-detail__meta-left">
            <el-avatar :size="36" class="article-detail__avatar">
              {{ article.authorName?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="article-detail__meta-info">
              <span class="article-detail__author">{{ article.authorName || '匿名用户' }}</span>
              <div class="article-detail__stats">
                <span>{{ formatDate(article.createTime) }}</span>
                <button type="button" class="article-detail__comment-link" @click="scrollToComments">
                  {{ article.commentCount ?? 0 }} 评论
                </button>
                <span>{{ article.favoriteCount ?? 0 }} 收藏</span>
                <span>{{ article.viewCount ?? 0 }} 阅读</span>
                <span>{{ wordCount }} 字</span>
              </div>
            </div>
          </div>
          <div class="article-detail__date-badge">
            {{ formatMonthDay(article.createTime) }}
          </div>
        </div>

        <div class="article-detail__divider" />

        <div class="article-detail__notice">
          <el-icon class="article-detail__notice-icon"><Bell /></el-icon>
          <p>
            温馨提示：本文最后更新于 {{ formatDate(article.updateTime || article.createTime) }}，
            转载请注明出处并保留原文链接。
          </p>
        </div>

        <div class="article-detail__content">
          <MdPreview
            :editor-id="PREVIEW_ID"
            :model-value="article.content || ''"
            preview-theme="default"
            code-theme="atom"
            :on-get-catalog="handleGetCatalog"
          />
        </div>

        <div v-if="article.tags?.length" class="article-detail__footer-tags">
          <el-tag
            v-for="tag in article.tags"
            :key="tag"
            size="small"
            effect="plain"
            round
          >
            {{ tag }}
          </el-tag>
        </div>

        <div class="article-detail__copyright">
          <div class="copyright-row">
            <el-icon><Star /></el-icon>
            <span>版权归属：{{ article.authorName || '匿名用户' }}</span>
          </div>
          <div class="copyright-row">
            <el-icon><Link /></el-icon>
            <span>本文链接：{{ articleUrl }}</span>
          </div>
          <div class="copyright-row">
            <el-icon><CopyDocument /></el-icon>
            <span>许可协议：署名-非商业性使用-禁止演绎 4.0 国际</span>
          </div>
        </div>
      </article>

      <ArticleCommentSection
        ref="commentSectionRef"
        :article-id="articleId"
        @comment-added="loadArticle"
      />

      <FavoriteFolderDialog
        v-model="favoriteDialogVisible"
        :article-id="articleId"
        @updated="handleFavoriteUpdated"
      />
      </div>
    </template>
  </section>
</template>

<style scoped>
.article-detail {
  position: relative;
  max-width: 960px;
  margin: 0 auto;
}

.article-detail__main {
  min-width: 0;
}

.article-detail__toc {
  position: fixed;
  left: max(16px, calc((100vw - 960px) / 2 - 220px));
  top: 120px;
  width: 200px;
  max-height: calc(100vh - 160px);
  overflow-y: auto;
  z-index: 10;
  padding: 16px;
  background: var(--blog-card-bg);
  border-radius: 12px;
  box-shadow: var(--blog-shadow);
}

.article-detail__toc-title {
  margin-bottom: 12px;
  padding-bottom: 10px;
  font-size: 14px;
  font-weight: 600;
  color: var(--blog-text);
  border-bottom: 1px solid var(--blog-border);
}

.article-detail__catalog :deep(.md-editor-catalog) {
  position: relative;
}

.article-detail__catalog :deep(.md-editor-catalog-indicator) {
  display: none;
}

.article-detail__catalog :deep(.md-editor-catalog-link) {
  padding-block: 2px;
}

.article-detail__catalog :deep(.md-editor-catalog-wrapper > .md-editor-catalog-link:first-of-type),
.article-detail__catalog :deep(.md-editor-catalog-wrapper > .md-editor-catalog-link:last-of-type) {
  padding-block: 2px;
}

.article-detail__catalog :deep(.md-editor-catalog > .md-editor-catalog-link) {
  padding-inline-start: 0;
}

.article-detail__catalog :deep(.md-editor-catalog-wrapper) {
  margin-top: 2px;
}

.article-detail__catalog :deep(.md-editor-catalog-wrapper > .md-editor-catalog-link) {
  padding-inline-start: 12px;
}

.article-detail__catalog :deep(.md-editor-catalog-wrapper .md-editor-catalog-wrapper > .md-editor-catalog-link) {
  padding-inline-start: 24px;
}

.article-detail__catalog :deep(.md-editor-catalog-wrapper .md-editor-catalog-wrapper .md-editor-catalog-wrapper > .md-editor-catalog-link) {
  padding-inline-start: 36px;
}

.article-detail__catalog :deep(.md-editor-catalog-wrapper .md-editor-catalog-wrapper .md-editor-catalog-wrapper .md-editor-catalog-wrapper > .md-editor-catalog-link) {
  padding-inline-start: 48px;
}

.article-detail__catalog :deep(.md-editor-catalog > .md-editor-catalog-link > span) {
  font-size: 13px;
  font-weight: 600;
  color: var(--blog-text);
}

.article-detail__catalog :deep(.md-editor-catalog-wrapper > .md-editor-catalog-link > span) {
  font-size: 12px;
  font-weight: 500;
  color: var(--blog-text-secondary);
}

.article-detail__catalog :deep(.md-editor-catalog-wrapper .md-editor-catalog-wrapper > .md-editor-catalog-link > span) {
  font-size: 12px;
  font-weight: 400;
  color: var(--blog-text-secondary);
}

.article-detail__catalog :deep(.md-editor-catalog-wrapper .md-editor-catalog-wrapper .md-editor-catalog-wrapper > .md-editor-catalog-link > span) {
  font-size: 11px;
  font-weight: 400;
  color: #aaa;
}

.article-detail__catalog :deep(.md-editor-catalog-link span) {
  line-height: 1.6;
  padding: 3px 0;
  transition: color 0.2s;
}

.article-detail__catalog :deep(.md-editor-catalog-link span:hover) {
  color: var(--blog-primary);
}

.article-detail__catalog :deep(.md-editor-catalog-active > span) {
  color: var(--blog-primary) !important;
  font-weight: 600;
}

.article-detail__actions {
  position: fixed;
  right: max(16px, calc((100vw - 960px) / 2 - 72px));
  left: auto;
  top: 120px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 10;
}

.action-btn {
  width: 48px;
  height: 48px;
  border: none;
  border-radius: 50%;
  background: var(--blog-card-bg);
  box-shadow: var(--blog-shadow);
  color: var(--blog-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s, transform 0.2s, box-shadow 0.2s;
}

.action-btn--active {
  color: var(--blog-primary);
}

.action-btn:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.action-btn:hover {
  color: var(--blog-primary);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.action-btn .el-icon {
  font-size: 18px;
}

.article-detail__card {
  background: var(--blog-card-bg);
  border-radius: 12px;
  box-shadow: var(--blog-shadow);
  padding: 32px 40px;
}

.article-detail__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.article-detail__tags :deep(.el-tag) {
  border: none;
  background: #409eff;
}

.article-detail__title {
  margin: 0 0 24px;
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--blog-text);
}

.article-detail__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.article-detail__meta-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.article-detail__avatar {
  flex-shrink: 0;
  background: var(--blog-primary-light);
  color: var(--blog-primary);
  font-weight: 600;
}

.article-detail__author {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: var(--blog-primary);
  margin-bottom: 4px;
}

.article-detail__stats {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 13px;
  color: var(--blog-text-secondary);
}

.article-detail__comment-link {
  border: none;
  background: none;
  padding: 0;
  font-size: 13px;
  color: var(--blog-text-secondary);
  cursor: pointer;
  transition: color 0.2s;
}

.article-detail__comment-link:hover {
  color: var(--blog-primary);
}

.article-detail__date-badge {
  flex-shrink: 0;
  font-size: 42px;
  font-weight: 300;
  color: #ddd;
  line-height: 1;
  letter-spacing: 2px;
}

.article-detail__divider {
  height: 1px;
  background: var(--blog-border);
  margin-bottom: 24px;
}

.article-detail__notice {
  display: flex;
  gap: 10px;
  padding: 14px 16px;
  margin-bottom: 28px;
  border: 1px solid rgba(255, 102, 0, 0.35);
  border-radius: var(--blog-radius);
  background: rgba(255, 102, 0, 0.06);
}

.article-detail__notice-icon {
  flex-shrink: 0;
  margin-top: 2px;
  color: var(--blog-primary);
  font-size: 16px;
}

.article-detail__notice p {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--blog-text-secondary);
}

.article-detail__content {
  margin-bottom: 28px;
}

.article-detail__content :deep(.md-editor-preview-wrapper) {
  padding: 0;
}

.article-detail__content :deep(.md-editor-preview) {
  font-size: 15px;
  line-height: 1.85;
  color: var(--blog-text);
}

.article-detail__content :deep(.md-editor-preview a) {
  color: var(--blog-primary);
}

.article-detail__content :deep(.md-editor-preview blockquote) {
  border-left: 4px solid var(--blog-primary);
  background: var(--blog-primary-light);
  padding: 12px 16px;
  margin: 16px 0;
  border-radius: 0 var(--blog-radius) var(--blog-radius) 0;
}

.article-detail__content :deep(.md-editor-preview .md-editor-code) {
  margin: 16px 0;
  border-radius: var(--blog-radius);
  overflow: hidden;
}

.article-detail__content :deep(.md-editor-preview .md-editor-code pre) {
  margin: 0;
  border-radius: 0;
}

.article-detail__footer-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}

.article-detail__copyright {
  padding: 20px 24px;
  border-radius: var(--blog-radius);
  background: rgba(100, 181, 246, 0.08);
  border: 1px solid rgba(100, 181, 246, 0.2);
}

.copyright-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  color: var(--blog-text-secondary);
  line-height: 1.6;
}

.copyright-row + .copyright-row {
  margin-top: 10px;
}

.copyright-row .el-icon {
  flex-shrink: 0;
  margin-top: 3px;
  color: #64b5f6;
}

@media (max-width: 1200px) {
  .article-detail__toc {
    display: none;
  }
}

@media (max-width: 992px) {
  .article-detail__actions {
    position: static;
    flex-direction: row;
    justify-content: center;
    margin-bottom: 16px;
  }

  .action-btn {
    width: 44px;
    height: 44px;
  }

  .article-detail__card {
    padding: 24px 20px;
  }

  .article-detail__title {
    font-size: 22px;
  }

  .article-detail__date-badge {
    font-size: 28px;
  }
}
</style>
