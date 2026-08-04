<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, Star, View } from '@element-plus/icons-vue'
import { getCategoryLabel } from '@/constants/categories'
import { formatDate } from '@/utils/format'
import { getArticleCover } from '@/utils/articleCovers'
import { openArticleDetail } from '@/utils/navigation'

const props = defineProps({
  article: {
    type: Object,
    required: true,
  },
})

const router = useRouter()

const cover = computed(() =>
  getArticleCover(props.article?.id || props.article?.title || ''),
)

function goDetail(article) {
  openArticleDetail(router, article?.id)
}
</script>

<template>
  <el-card class="article-card" shadow="hover" @click="goDetail(article)">
    <div class="article-card__body">
      <div
        class="article-card__cover"
        :style="{ backgroundImage: cover ? `url(${cover})` : undefined }"
        role="img"
        :aria-label="article.title"
      />

      <div class="article-card__content">
        <h3 class="article-card__title">{{ article.title }}</h3>
        <p class="article-card__summary">{{ article.summary }}</p>

        <div class="article-card__meta">
          <div class="article-card__stats">
            <span>{{ formatDate(article.createTime) }}</span>
            <span><el-icon><View /></el-icon>{{ article.viewCount ?? 0 }}</span>
            <span><el-icon><ChatDotRound /></el-icon>{{ article.commentCount ?? 0 }}</span>
            <span><el-icon><Star /></el-icon>{{ article.favoriteCount ?? 0 }}</span>
          </div>

          <div class="article-card__tags">
            <el-tag
              v-if="article.category"
              size="small"
              type="warning"
              effect="plain"
            >
              {{ getCategoryLabel(article.category) }}
            </el-tag>
            <el-tag
              v-for="tag in (article.tags || []).slice(0, 2)"
              :key="tag"
              size="small"
              effect="plain"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.article-card {
  margin-bottom: 16px;
  border: none;
  border-radius: var(--blog-radius);
  cursor: pointer;
}

.article-card :deep(.el-card__body) {
  padding: 16px;
}

.article-card__body {
  display: flex;
  gap: 20px;
}

.article-card__cover {
  flex-shrink: 0;
  width: 180px;
  height: 120px;
  border-radius: var(--blog-radius);
  background-color: var(--blog-border);
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.article-card__content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.article-card__title {
  margin: 0 0 8px;
  font-size: 17px;
  font-weight: 600;
  color: var(--blog-text);
  cursor: pointer;
  transition: color 0.2s;
}

.article-card__title:hover {
  color: var(--blog-primary);
}

.article-card__summary {
  margin: 0;
  flex: 1;
  font-size: 14px;
  color: var(--blog-text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  flex-wrap: wrap;
  gap: 8px;
}

.article-card__stats {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: var(--blog-text-secondary);
}

.article-card__stats span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.article-card__tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .article-card__body {
    flex-direction: column;
  }

  .article-card__cover {
    width: 100%;
    height: 160px;
  }
}
</style>
