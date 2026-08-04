<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SectionTitle from '@/components/common/SectionTitle.vue'
import { fetchAdminArticles, deleteArticle } from '@/api/admin'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const articles = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const isMaster = computed(() => userStore.isMaster)
const currentUserId = computed(() => userStore.userInfo?.userId)

function canDelete(row) {
  if (isMaster.value) return true
  return row.authorId === currentUserId.value
}

async function loadArticles() {
  loading.value = true
  try {
    const data = await fetchAdminArticles({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
    })
    articles.value = data.content || []
    total.value = data.totalElements || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadArticles()
}

function handlePageChange(page) {
  pageNum.value = page
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
      /* error handled by request interceptor */
    }
  }
}

onMounted(loadArticles)
</script>

<template>
  <section class="manage-page">
    <SectionTitle title="文章管理" />

    <div class="manage-toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索文章标题或摘要"
        clearable
        style="width: 260px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table v-loading="loading" :data="articles" stripe>
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="authorName" label="作者" width="120" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="viewCount" label="浏览" width="80" align="center" />
      <el-table-column prop="commentCount" label="评论" width="80" align="center" />
      <el-table-column prop="createTime" label="发布时间" width="170" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="canDelete(row)"
            type="danger"
            link
            @click="handleDelete(row)"
          >
            删除
          </el-button>
          <span v-else class="text-muted">无权限</span>
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
  gap: 12px;
  margin-bottom: 16px;
}

.manage-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.text-muted {
  font-size: 12px;
  color: var(--blog-text-secondary);
}
</style>
