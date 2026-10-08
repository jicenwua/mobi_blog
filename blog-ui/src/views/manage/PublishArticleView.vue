<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import SectionTitle from '@/components/common/SectionTitle.vue'
import MarkdownEditor from '@/components/common/MarkdownEditor.vue'
import { useArticleDraft } from '@/composables/useArticleDraft'
import { createArticle, fetchAllCategories, fetchArticleDetail, updateArticle } from '@/api/article'
import { getCategoryLabel } from '@/constants/categories'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const loading = ref(false)
const categories = ref([])
const loadingCategories = ref(false)
const editId = computed(() => route.query.id || null)
const isEdit = computed(() => !!editId.value)

const categoryOptions = computed(() =>
  categories.value.map((code) => ({
    value: code,
    label: getCategoryLabel(code),
  })),
)

const form = reactive({
  title: '',
  summary: '',
  content: '',
  category: '',
  tagsInput: '',
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入正文', trigger: 'blur' }],
}

function parseTags(input) {
  if (!input?.trim()) return []
  return input
    .split(/[,，]/)
    .map((tag) => tag.trim())
    .filter(Boolean)
}

const { saving, lastSavedAt, saveDraft, clearDraft, markPublished } = useArticleDraft(
  form,
  parseTags,
  { editId: editId.value },
)

const draftStatusText = computed(() => {
  if (isEdit.value) {
    if (saving.value) return '保存中...'
    if (lastSavedAt.value) return `上次保存于 ${lastSavedAt.value}`
    return '修改发布后立即生效'
  }
  if (saving.value) return '草稿保存中...'
  if (lastSavedAt.value) return `草稿已保存于 ${lastSavedAt.value}`
  return '编辑内容将自动保存为草稿'
})

async function loadCategories() {
  loadingCategories.value = true
  try {
    categories.value = await fetchAllCategories() || []
  } catch {
    categories.value = []
  } finally {
    loadingCategories.value = false
  }
}

async function loadArticleForEdit() {
  if (!editId.value) return
  loading.value = true
  try {
    const article = await fetchArticleDetail(editId.value)
    if (!article) {
      ElMessage.error('未找到该文章')
      router.replace('/manage/my-articles')
      return
    }
    form.title = article.title || ''
    form.summary = article.summary || ''
    form.content = article.content || ''
    form.category = article.category || ''
    form.tagsInput = (article.tags || []).join(',')
    lastSavedAt.value = article.updateTime || article.createTime
  } catch {
    /* error handled by request interceptor */
    router.replace('/manage/my-articles')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      title: form.title.trim(),
      summary: form.summary.trim(),
      content: form.content.trim(),
      category: form.category,
      tags: parseTags(form.tagsInput),
    }
    if (isEdit.value) {
      await updateArticle(editId.value, payload)
      ElMessage.success('文章修改成功')
    } else {
      await createArticle(payload)
      markPublished()
      ElMessage.success('文章发布成功')
    }
    router.push('/manage/my-articles')
  } finally {
    submitting.value = false
  }
}

async function handleReset() {
  try {
    await ElMessageBox.confirm('确定清空当前内容吗？草稿也会被删除。', '重置确认', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
    if (isEdit.value) {
      // 编辑模式下重置：重新拉取原文
      await loadArticleForEdit()
    } else {
      await clearDraft()
      formRef.value?.resetFields()
      form.tagsInput = ''
    }
  } catch (e) {
    if (e !== 'cancel') {
      /* handled by interceptor */
    }
  }
}

async function handleSaveDraft() {
  if (isEdit.value) {
    // 编辑模式下，保存草稿等价于直接保存修改
    await handleSubmit()
    return
  }
  await saveDraft({ silent: false })
}

onMounted(async () => {
  await loadCategories()
  await loadArticleForEdit()
})
</script>

<template>
  <section class="manage-page" v-loading="loading">
    <div class="publish-header">
      <SectionTitle :title="isEdit ? '修改文章' : '发布文章'" />
      <span class="draft-status" :class="{ 'is-saving': saving }">{{ draftStatusText }}</span>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" class="publish-form">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入文章标题" maxlength="200" show-word-limit />
      </el-form-item>

      <el-form-item label="分类" prop="category">
        <el-select
          v-model="form.category"
          placeholder="请选择或输入分类"
          filterable
          allow-create
          default-first-option
          :loading="loadingCategories"
          style="width: 240px"
        >
          <el-option
            v-for="item in categoryOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="标签">
        <el-input
          v-model="form.tagsInput"
          placeholder="多个标签用逗号分隔，如：Java,Spring"
        />
      </el-form-item>

      <el-form-item label="摘要">
        <el-input
          v-model="form.summary"
          type="textarea"
          :rows="3"
          placeholder="请输入文章摘要（可选）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="正文" prop="content" class="content-item">
        <MarkdownEditor v-model="form.content" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '发布' }}
        </el-button>
        <el-button v-if="!isEdit" :loading="saving" @click="handleSaveDraft">保存草稿</el-button>
        <el-button @click="handleReset">{{ isEdit ? '撤销修改' : '重置' }}</el-button>
      </el-form-item>
    </el-form>
  </section>
</template>

<style scoped>
.manage-page {
  background: var(--blog-card-bg);
  border-radius: var(--blog-radius);
  box-shadow: var(--blog-shadow);
  padding: 24px;
}

.publish-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 8px;
}

.draft-status {
  flex-shrink: 0;
  font-size: 13px;
  color: var(--blog-text-secondary);
}

.draft-status.is-saving {
  color: var(--blog-primary);
}

.publish-form {
  max-width: 900px;
}

.content-item :deep(.el-form-item__content) {
  line-height: normal;
}

@media (max-width: 768px) {
  .publish-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
