<script setup>
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { fetchArticles } from '@/api/article'
import { useRouter } from 'vue-router'
import { openArticleDetail } from '@/utils/navigation'

const visible = defineModel({ type: Boolean, default: false })

const keyword = ref('')
const loading = ref(false)
const results = ref([])
const router = useRouter()

async function handleSearch() {
  const value = keyword.value.trim()
  if (!value) return

  loading.value = true
  try {
    const data = await fetchArticles({ keyword: value, pageNum: 1, pageSize: 10 })
    results.value = data?.content || []
  } catch {
    results.value = []
  } finally {
    loading.value = false
  }
}

function goArticle(article) {
  visible.value = false
  openArticleDetail(router, article.id)
}

function handleClose() {
  keyword.value = ''
  results.value = []
}
</script>

<template>
  <el-dialog
    v-model="visible"
    title="搜索文章"
    width="520px"
    destroy-on-close
    @close="handleClose"
  >
    <el-input
      v-model="keyword"
      placeholder="输入关键词搜索文章..."
      clearable
      @keyup.enter="handleSearch"
    >
      <template #append>
        <el-button :icon="Search" :loading="loading" @click="handleSearch">
          搜索
        </el-button>
      </template>
    </el-input>

    <div v-loading="loading" class="search-results">
      <template v-if="results.length">
        <div
          v-for="item in results"
          :key="item.id"
          class="search-item"
          @click="goArticle(item)"
        >
          <div class="search-item__title">{{ item.title }}</div>
          <div class="search-item__summary">{{ item.summary }}</div>
        </div>
      </template>
      <el-empty v-else-if="keyword && !loading" description="暂无搜索结果" :image-size="80" />
    </div>
  </el-dialog>
</template>

<style scoped>
.search-results {
  margin-top: 16px;
  min-height: 120px;
}

.search-item {
  padding: 12px;
  border-radius: var(--blog-radius);
  cursor: pointer;
  transition: background 0.2s;
}

.search-item:hover {
  background: var(--blog-primary-light);
}

.search-item__title {
  font-weight: 600;
  margin-bottom: 4px;
}

.search-item__summary {
  font-size: 13px;
  color: var(--blog-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
