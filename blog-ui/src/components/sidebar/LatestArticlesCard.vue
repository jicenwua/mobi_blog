<script setup>
import { onMounted, ref } from 'vue'
import { Link } from '@element-plus/icons-vue'
import SectionTitle from '@/components/common/SectionTitle.vue'
import { fetchArticles } from '@/api/article'

const articles = ref([])
const loading = ref(false)

async function loadLatestArticles() {
  loading.value = true
  try {
    const data = await fetchArticles({ pageNum: 1, pageSize: 5 })
    articles.value = data?.content || []
  } catch {
    articles.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadLatestArticles)
</script>

<template>
  <el-card class="sidebar-card" shadow="never">
    <SectionTitle title="最新文章" />
    <ul v-loading="loading" class="latest-list">
      <li v-for="item in articles" :key="item.id" class="latest-list__item">
        <el-icon class="latest-list__icon"><Link /></el-icon>
        <span class="latest-list__title">{{ item.title }}</span>
      </li>
      <li v-if="!loading && !articles.length" class="latest-list__empty">暂无文章</li>
    </ul>
    <div class="latest-list__more-wrap">
      <router-link :to="{ name: 'LatestArticles' }" class="latest-list__more">
        更多>>
      </router-link>
    </div>
  </el-card>
</template>

<style scoped>
.sidebar-card {
  border: none;
  border-radius: var(--blog-radius);
  margin-bottom: 16px;
}

.sidebar-card :deep(.section-title) {
  margin-bottom: 12px;
}

.latest-list {
  list-style: none;
  margin: 0;
  padding: 0;
  min-height: 40px;
}

.latest-list__item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px dashed var(--blog-border);
  transition: color 0.2s;
}

.latest-list__item:last-child {
  border-bottom: none;
}

.latest-list__item:hover {
  color: var(--blog-primary);
}

.latest-list__icon {
  flex-shrink: 0;
  color: var(--blog-primary);
}

.latest-list__title {
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.latest-list__empty {
  font-size: 13px;
  color: var(--blog-text-secondary);
  padding: 8px 0;
}

.latest-list__more-wrap {
  margin-top: 8px;
  text-align: right;
}

.latest-list__more {
  display: inline-block;
  font-size: 13px;
  color: var(--blog-text-secondary);
  text-decoration: none;
  transition: color 0.2s ease;
}

.latest-list__more:hover {
  color: var(--blog-primary);
}
</style>
