<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import SectionTitle from '@/components/common/SectionTitle.vue'
import MarkdownEditor from '@/components/common/MarkdownEditor.vue'
import { useArticleDraft } from '@/composables/useArticleDraft'
import { createArticle } from '@/api/article'
import { ARTICLE_CATEGORIES } from '@/constants/categories'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)

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

const { saving, lastSavedAt, saveDraft, clearDraft, markPublished } = useArticleDraft(form, parseTags)

const draftStatusText = computed(() => {
  if (saving.value) return '草稿保存中...'
  if (lastSavedAt.value) return `草稿已保存于 ${lastSavedAt.value}`
  return '编辑内容将自动保存为草稿'
})

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createArticle({
      title: form.title.trim(),
      summary: form.summary.trim(),
      content: form.content.trim(),
      category: form.category,
      tags: parseTags(form.tagsInput),
    })
    markPublished()
    ElMessage.success('文章发布成功')
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
    await clearDraft()
    formRef.value?.resetFields()
    form.tagsInput = ''
  } catch (e) {
    if (e !== 'cancel') {
      /* handled by interceptor */
    }
  }
}

async function handleSaveDraft() {
  await saveDraft({ silent: false })
}
</script>

<template>
  <section class="manage-page">
    <div class="publish-header">
      <SectionTitle title="发布文章" />
      <span class="draft-status" :class="{ 'is-saving': saving }">{{ draftStatusText }}</span>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" class="publish-form">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入文章标题" maxlength="200" show-word-limit />
      </el-form-item>

      <el-form-item label="分类" prop="category">
        <el-select v-model="form.category" placeholder="请选择分类" style="width: 240px">
          <el-option
            v-for="item in ARTICLE_CATEGORIES"
            :key="item.code"
            :label="item.label"
            :value="item.code"
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
        <el-button type="primary" :loading="submitting" @click="handleSubmit">发布</el-button>
        <el-button :loading="saving" @click="handleSaveDraft">保存草稿</el-button>
        <el-button @click="handleReset">重置</el-button>
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
