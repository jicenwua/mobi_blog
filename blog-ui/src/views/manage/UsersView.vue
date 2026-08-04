<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import SectionTitle from '@/components/common/SectionTitle.vue'
import { fetchAdminUsers, updateUserRole, updateUserStatus } from '@/api/admin'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const users = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const isMaster = computed(() => userStore.isMaster)
const isAdmin = computed(() => userStore.isAdmin)
const currentUserId = computed(() => userStore.userInfo?.userId)

const statusOptions = [
  { label: '正常', value: '0' },
  { label: '停用', value: '1' },
]

const roleOptions = [
  { label: '普通用户', value: 1 },
  { label: '管理员', value: 2 },
  { label: '超级管理员', value: 3 },
]

const roleValueMap = { CUSTOMER: 1, ADMIN: 2, MASTER: 3 }

function roleLabel(role) {
  const map = { CUSTOMER: '普通用户', ADMIN: '管理员', MASTER: '超级管理员' }
  return map[role] || role
}

async function loadUsers() {
  loading.value = true
  try {
    const data = await fetchAdminUsers({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
    })
    users.value = data.content || []
    total.value = data.totalElements || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadUsers()
}

function handlePageChange(page) {
  pageNum.value = page
  loadUsers()
}

function canEditStatus(row) {
  if (!isAdmin.value) return false
  if (Number(row.userId) === Number(currentUserId.value)) return false
  if (row.role === 'MASTER' && !isMaster.value) return false
  return true
}

async function handleRoleChange(row, roleValue) {
  try {
    await updateUserRole(row.userId, roleValue)
    ElMessage.success('角色修改成功')
    loadUsers()
  } catch {
    loadUsers()
  }
}

async function handleStatusChange(row, status) {
  try {
    await updateUserStatus(row.userId, status)
    ElMessage.success('状态修改成功')
    loadUsers()
  } catch {
    loadUsers()
  }
}

onMounted(loadUsers)
</script>

<template>
  <section class="manage-page">
    <SectionTitle title="用户管理" />

    <div class="manage-toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索昵称或邮箱"
        clearable
        style="width: 260px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table v-loading="loading" :data="users" stripe>
      <el-table-column prop="nickName" label="昵称" width="140" />
      <el-table-column prop="email" label="邮箱" min-width="200" show-overflow-tooltip />
      <el-table-column label="角色" width="160">
        <template #default="{ row }">
          <el-select
            v-if="isMaster"
            :model-value="roleValueMap[row.role]"
            size="small"
            @change="(val) => handleRoleChange(row, val)"
          >
            <el-option
              v-for="opt in roleOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
          <el-tag v-else size="small">{{ roleLabel(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-select
            v-if="isAdmin"
            :model-value="row.status"
            size="small"
            :disabled="!canEditStatus(row)"
            @change="(val) => handleStatusChange(row, val)"
          >
            <el-option
              v-for="opt in statusOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
          <el-tag v-else :type="row.status === '0' ? 'success' : 'danger'" size="small">
            {{ row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
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
