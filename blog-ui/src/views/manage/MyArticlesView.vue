<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import SectionTitle from '@/components/common/SectionTitle.vue'
import { fetchMyArticles, deleteArticle } from '@/api/article'
import { getCategoryLabel } from '@/constants/categories'

const router = useRouter()
const loading = ref(false)
const articles = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const sortBy = ref('time')

const sortOptions = [
  { label: '发布时间', value: 'time' },
  { label: '浏览热度', value: 'view' },
]

async function loadArticles() {
  loading.value = true
  try {
    const data = await fetchMyArticles({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      sortBy: sortBy.value,
    })
    articles.value = data.content || []
    total.value = data.totalElements || 0
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  pageNum.value = page
  loadArticles()
}

function handleSortChange() {
  pageNum.value = 1
  loadArticles()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除文章「${row.title}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteArticle(row.id)
    ElMessage.success('删除成功')
    loadArticles()
  } catch (e) {
    if (e !== 'cancel') {
      /* handled by interceptor */
    }
  }
}

watch(sortBy, handleSortChange)

onMounted(loadArticles)
</script>

<template>
  <section class="manage-page">
    <SectionTitle title="我的文章" />

    <div class="manage-toolbar">
      <span class="toolbar-label">排序方式</span>
      <el-radio-group v-model="sortBy">
        <el-radio-button
          v-for="opt in sortOptions"
          :key="opt.value"
          :value="opt.value"
        >
          {{ opt.label }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <el-table v-loading="loading" :data="articles" stripe>
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.status === 'draft'" type="info" size="small">草稿</el-tag>
          <el-tag v-else type="success" size="small">已发布</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分类" width="120">
        <template #default="{ row }">
          {{ getCategoryLabel(row.category) }}
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="80" align="center" />
      <el-table-column prop="commentCount" label="评论" width="80" align="center" />
      <el-table-column prop="favoriteCount" label="收藏" width="80" align="center" />
      <el-table-column prop="createTime" label="发布时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'draft'"
            type="primary"
            link
            @click="router.push('/manage/publish')"
          >
            继续编辑
          </el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="manage-pagination">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </section>
</template>

<style scoped>
.manage-page {
  background: var(--blog-card-bg);
  border-radius: var(--blog-radius);
  box-shadow: var(--blog-shadow);
  padding: 24px;
}

.manage-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.toolbar-label {
  font-size: 14px;
  color: var(--blog-text-secondary);
}

.manage-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
