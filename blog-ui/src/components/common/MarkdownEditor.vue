<script setup>
import { onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { deleteArticleImage, uploadArticleImages } from '@/api/article'

defineProps({
  placeholder: {
    type: String,
    default: '请输入正文，支持 Markdown 语法，可直接粘贴 Markdown 内容',
  },
})

const model = defineModel({ type: String, default: '' })

const toolbars = [
  'bold',
  'underline',
  'italic',
  'strikeThrough',
  '-',
  'title',
  'sub',
  'sup',
  'quote',
  'unorderedList',
  'orderedList',
  'task',
  '-',
  'codeRow',
  'code',
  'link',
  'image',
  'table',
  'mermaid',
  'katex',
  '-',
  'revoke',
  'next',
  '-',
  'preview',
  'htmlPreview',
  'catalog',
  'fullscreen',
]

const MANAGED_IMAGE_PREFIX = 'blog/image/'
const managedUrls = ref(new Set())
let uploadGeneration = 0

function extractImageUrls(text) {
  if (!text) return new Set()
  const urls = new Set()
  const regex = /!\[[^\]]*\]\(([^)\s"']+)/g
  let match
  while ((match = regex.exec(text)) !== null) {
    urls.add(match[1])
  }
  return urls
}

function isManagedImageUrl(url) {
  return typeof url === 'string' && url.includes(MANAGED_IMAGE_PREFIX)
}

async function deleteManagedUrls(urls) {
  await Promise.allSettled(
    urls.filter(isManagedImageUrl).map((url) => deleteArticleImage(url)),
  )
}

async function syncRemovedImages(content) {
  const usedUrls = extractImageUrls(content)
  const removedUrls = [...managedUrls.value].filter((url) => !usedUrls.has(url))
  if (!removedUrls.length) return

  await deleteManagedUrls(removedUrls)
  removedUrls.forEach((url) => managedUrls.value.delete(url))
}

async function handleUploadImg(files, callback) {
  const generation = uploadGeneration

  try {
    const urls = await uploadArticleImages(files)

    if (generation !== uploadGeneration) {
      await deleteManagedUrls(urls)
      callback([])
      return
    }

    urls.forEach((url) => managedUrls.value.add(url))
    callback(urls)
  } catch {
    ElMessage.error('图片上传失败')
    callback([])
  }
}

watch(model, (content) => {
  if (!content?.trim()) {
    uploadGeneration += 1
  }
  syncRemovedImages(content)
})

onBeforeUnmount(() => {
  uploadGeneration += 1
  syncRemovedImages(model.value)
})
</script>

<template>
  <MdEditor
    v-model="model"
    language="zh-CN"
    :placeholder="placeholder"
    :toolbars="toolbars"
    preview-theme="default"
    :style="{ height: '520px' }"
    class="markdown-editor"
    @on-upload-img="handleUploadImg"
  />
</template>

<style scoped>
.markdown-editor {
  width: 100%;
  border-radius: var(--blog-radius);
  overflow: hidden;
}

.markdown-editor :deep(.md-editor) {
  --md-color: var(--blog-text);
  --md-border-color: var(--blog-border);
  --md-bk-color: var(--blog-card-bg);
  --md-bk-hover-color: var(--blog-primary-light);
}
</style>
