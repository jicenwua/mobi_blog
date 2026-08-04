<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SectionTitle from '@/components/common/SectionTitle.vue'
import { fetchAdminComments, deleteComment } from '@/api/admin'

const loading = ref(false)
const comments = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')

async function loadComments() {
  loading.value = true
  try {
    const data = await fetchAdminComments({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
    })
    comments.value = data.content || []
    total.value = data.totalElements || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadComments()
}

function handlePageChange(page) {
  pageNum.value = page
  loadComments()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该评论吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteComment(row.id)
    ElMessage.success('删除成功')
    loadComments()
  } catch (e) {
    if (e !== 'cancel') {
      /* error handled by request interceptor */
    }
  }
}

onMounted(loadComments)
</script>

<template>
  <section class="manage-page">
    <SectionTitle title="评论管理" />

    <div class="manage-toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索评论内容"
        clearable
        style="width: 260px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table v-loading="loading" :data="comments" stripe>
      <el-table-column prop="userNickName" label="用户" width="120" />
      <el-table-column prop="content" label="评论内容" min-width="240" show-overflow-tooltip />
      <el-table-column prop="articleId" label="文章ID" width="140" show-overflow-tooltip />
      <el-table-column prop="likeCount" label="点赞" width="80" align="center" />
      <el-table-column prop="createTime" label="评论时间" width="170" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
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
  gap: 12px;
  margin-bottom: 16px;
}

.manage-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
