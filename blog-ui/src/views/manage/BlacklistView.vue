<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SectionTitle from '@/components/common/SectionTitle.vue'
import { createBlacklist, deleteBlacklist, fetchBlacklist } from '@/api/blacklist'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const form = reactive({
  blackIp: '',
  blackUserId: '',
  blackContent: '',
})

function resetForm() {
  form.blackIp = ''
  form.blackUserId = ''
  form.blackContent = ''
}

async function loadList() {
  loading.value = true
  try {
    const data = await fetchBlacklist({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    })
    list.value = data.content || []
    total.value = data.totalElements || 0
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

async function handleSubmit() {
  const blackIp = form.blackIp.trim()
  const blackUserId = form.blackUserId === '' ? null : Number(form.blackUserId)
  const blackContent = form.blackContent.trim()

  if (!blackIp && blackUserId == null && !blackContent) {
    ElMessage.warning('请至少填写 IP、用户 ID 或违禁词正则之一')
    return
  }
  if (form.blackUserId !== '' && Number.isNaN(blackUserId)) {
    ElMessage.warning('用户 ID 必须为数字')
    return
  }

  submitting.value = true
  try {
    await createBlacklist({
      blackIp: blackIp || undefined,
      blackUserId,
      blackContent: blackContent || undefined,
    })
    ElMessage.success('黑名单规则已添加')
    dialogVisible.value = false
    pageNum.value = 1
    await loadList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该黑名单规则吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteBlacklist(row.id)
    ElMessage.success('已删除')
    await loadList()
  } catch (e) {
    if (e !== 'cancel') {
      /* handled by interceptor */
    }
  }
}

function handlePageChange(page) {
  pageNum.value = page
  loadList()
}

onMounted(loadList)
</script>

<template>
  <section class="manage-page">
    <div class="manage-page__header">
      <SectionTitle title="黑名单管理" />
      <el-button type="primary" @click="openCreateDialog">新增规则</el-button>
    </div>

    <el-alert
      class="manage-tip"
      type="info"
      :closable="false"
      show-icon
      title="IP 与用户 ID 会全局拦截；违禁词正则仅在发表评论时校验。"
    />

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="blackIp" label="黑名单 IP" min-width="140">
        <template #default="{ row }">
          {{ row.blackIp || '—' }}
        </template>
      </el-table-column>
      <el-table-column prop="blackUserId" label="用户 ID" width="100">
        <template #default="{ row }">
          {{ row.blackUserId ?? '—' }}
        </template>
      </el-table-column>
      <el-table-column prop="blackContent" label="违禁词正则" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.blackContent || '—' }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
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

    <el-dialog v-model="dialogVisible" title="新增黑名单规则" width="520px" destroy-on-close>
      <el-form label-width="110px">
        <el-form-item label="黑名单 IP">
          <el-input v-model="form.blackIp" placeholder="例如：192.168.1.100" clearable />
        </el-form-item>
        <el-form-item label="用户 ID">
          <el-input v-model="form.blackUserId" placeholder="blog_user.user_id" clearable />
        </el-form-item>
        <el-form-item label="违禁词正则">
          <el-input
            v-model="form.blackContent"
            type="textarea"
            :rows="3"
            placeholder="正则表达式，如：广告|spam"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.manage-page {
  background: var(--blog-card-bg);
  border-radius: var(--blog-radius);
  box-shadow: var(--blog-shadow);
  padding: 24px;
}

.manage-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 8px;
}

.manage-tip {
  margin-bottom: 16px;
}

.manage-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 768px) {
  .manage-page__header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
