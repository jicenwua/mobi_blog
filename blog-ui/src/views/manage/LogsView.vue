<script setup>
import { onMounted, ref } from 'vue'
import SectionTitle from '@/components/common/SectionTitle.vue'
import { fetchAdminLogs } from '@/api/admin'

const loading = ref(false)
const logs = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

function roleLabel(role) {
  const map = {
    MASTER: '超级管理员',
    ADMIN: '管理员',
    CUSTOMER: '普通用户',
    UNKNOWN: '未知',
  }
  return map[role] || role
}

function roleTagType(role) {
  if (role === 'MASTER') return 'danger'
  if (role === 'ADMIN') return 'warning'
  return 'info'
}

async function loadLogs() {
  loading.value = true
  try {
    const data = await fetchAdminLogs({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    })
    logs.value = data.content || []
    total.value = data.totalElements || 0
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  pageNum.value = page
  loadLogs()
}

onMounted(loadLogs)
</script>

<template>
  <section class="manage-page">
    <SectionTitle title="日志记录" />

    <el-table v-loading="loading" :data="logs" stripe>
      <el-table-column prop="operatorName" label="操作人" width="120">
        <template #default="{ row }">
          {{ row.operatorName || '未知用户' }}
        </template>
      </el-table-column>
      <el-table-column prop="operatorRole" label="角色" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="roleTagType(row.operatorRole)">
            {{ roleLabel(row.operatorRole) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="module" label="模块" width="80" />
      <el-table-column prop="operation" label="操作" width="100" />
      <el-table-column prop="targetId" label="目标ID" width="140" show-overflow-tooltip />
      <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createTime" label="操作时间" width="170" />
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

.manage-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
