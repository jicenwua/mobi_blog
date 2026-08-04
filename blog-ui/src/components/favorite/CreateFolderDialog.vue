<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createFavoriteFolder } from '@/api/favorite'
import { DEFAULT_FOLDER_NAME } from '@/constants/favorite'

const visible = defineModel({ type: Boolean, default: false })

const emit = defineEmits(['created'])

const folderName = ref('')
const creating = ref(false)

async function handleCreate() {
  const name = folderName.value.trim()
  if (!name || creating.value) return

  if (name === DEFAULT_FOLDER_NAME) {
    ElMessage.warning('该名称为系统保留，请使用其他名称')
    return
  }

  creating.value = true
  try {
    const favoriteId = await createFavoriteFolder(name)
    emit('created', { favoriteId, favoriteName: name })
    visible.value = false
    ElMessage.success('收藏夹已创建')
  } finally {
    creating.value = false
  }
}

function handleClose() {
  folderName.value = ''
}

watch(visible, (open) => {
  if (!open) {
    folderName.value = ''
  }
})
</script>

<template>
  <el-dialog
    v-model="visible"
    title="新建收藏夹"
    width="400px"
    append-to-body
    destroy-on-close
    @close="handleClose"
  >
    <el-input
      v-model="folderName"
      maxlength="32"
      show-word-limit
      placeholder="请输入收藏夹名称"
      clearable
      @keyup.enter="handleCreate"
    />

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="creating" @click="handleCreate">
        创建
      </el-button>
    </template>
  </el-dialog>
</template>
