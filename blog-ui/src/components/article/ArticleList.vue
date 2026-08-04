<script setup>
import { onMounted, watch } from 'vue'
import SectionTitle from '@/components/common/SectionTitle.vue'
import ArticleCard from '@/components/article/ArticleCard.vue'
import { useArticles } from '@/composables/useArticles'

const props = defineProps({
  title: {
    type: String,
    default: '最新文章',
  },
  queryParams: {
    type: Object,
    default: () => ({}),
  },
})

const emit = defineEmits(['loaded'])

const { articles, loading, total, pageNum, pageSize, loadArticles } = useArticles(props.queryParams)

async function refreshArticles() {
  await loadArticles()
  emit('loaded', articles.value)
}

onMounted(refreshArticles)

watch(
  () => props.queryParams,
  () => {
    pageNum.value = 1
    refreshArticles()
  },
  { deep: true }
)

function handlePageChange(page) {
  pageNum.value = page
  loadArticles()
}
</script>

<template>
  <section class="article-list">
    <SectionTitle :title="title" />

    <div v-loading="loading">
      <ArticleCard
        v-for="article in articles"
        :key="article.id"
        :article="article"
      />

      <el-empty v-if="!loading && !articles.length" description="暂无文章" />

      <div v-if="total > pageSize" class="article-list__pagination">
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </section>
</template>

<style scoped>
.article-list__pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.article-list__pagination :deep(.el-pagination.is-background .el-pager li.is-active) {
  background-color: var(--blog-primary);
}
</style>
