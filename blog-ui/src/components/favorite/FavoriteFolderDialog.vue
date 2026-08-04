<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Folder, Plus } from '@element-plus/icons-vue'
import CreateFolderDialog from '@/components/favorite/CreateFolderDialog.vue'
import {
  addArticleToFolder,
  fetchFavoriteFolders,
  fetchFolderArticles,
  removeArticleFromFolder,
} from '@/api/favorite'
import { dedupeFavoriteFolders, isDefaultFolder } from '@/constants/favorite'

const visible = defineModel({ type: Boolean, default: false })

const props = defineProps({
  articleId: {
    type: String,
    required: true,
  },
})

const emit = defineEmits(['updated'])

const loading = ref(false)
const saving = ref(false)
const createDialogVisible = ref(false)
const folders = ref([])
const folderStates = ref({})

const sortedFolders = computed(() => dedupeFavoriteFolders(folders.value))

async function loadFolderStates(folderList) {
  const states = {}
  await Promise.all(
    folderList.map(async (folder) => {
      try {
        const articles = await fetchFolderArticles(folder.favoriteId, {
          pageNum: 1,
          pageSize: 200,
        })
        const items = articles?.records || articles?.content || []
        states[folder.favoriteId] = {
          contains: items.some((item) => item.articleId === props.articleId),
          count: articles?.total ?? articles?.totalElements ?? items.length,
        }
      } catch {
        states[folder.favoriteId] = { contains: false, count: 0 }
      }
    }),
  )
  folderStates.value = states
}

async function loadFolders() {
  if (!props.articleId) return

  loading.value = true
  try {
    const data = await fetchFavoriteFolders()
    folders.value = dedupeFavoriteFolders(data?.records || data?.content || [])
    await loadFolderStates(folders.value)
  } catch {
    folders.value = []
    folderStates.value = {}
  } finally {
    loading.value = false
  }
}

async function handleSelectFolder(folder) {
  if (!props.articleId || saving.value) return

  const state = folderStates.value[folder.favoriteId]
  saving.value = true
  try {
    if (state?.contains) {
      await removeArticleFromFolder(folder.favoriteId, props.articleId)
      ElMessage.success('已从收藏夹移除')
    } else {
      await addArticleToFolder(folder.favoriteId, props.articleId)
      ElMessage.success('已加入收藏夹')
    }
    await loadFolders()
    emitUpdated()
  } finally {
    saving.value = false
  }
}

async function handleFolderCreated(folder) {
  createDialogVisible.value = false
  await loadFolders()

  if (!props.articleId) return

  saving.value = true
  try {
    await addArticleToFolder(folder.favoriteId, props.articleId)
    ElMessage.success('已加入收藏夹')
    await loadFolders()
    emitUpdated()
  } finally {
    saving.value = false
  }
}

function emitUpdated() {
  const isFavorited = Object.values(folderStates.value).some((item) => item.contains)
  emit('updated', { isFavorited })
}

watch(
  () => [visible.value, props.articleId],
  ([open]) => {
    if (open) {
      loadFolders()
    }
  },
)
</script>

<template>
  <el-dialog
    v-model="visible"
    title="收藏到"
    width="420px"
    destroy-on-close
  >
    <div v-loading="loading" class="favorite-dialog">
      <div class="favorite-dialog__list">
        <button
          v-for="folder in sortedFolders"
          :key="folder.favoriteId"
          type="button"
          class="favorite-dialog__item"
          :class="{ 'is-selected': folderStates[folder.favoriteId]?.contains }"
          :disabled="saving"
          @click="handleSelectFolder(folder)"
        >
          <span class="favorite-dialog__item-main">
            <el-icon class="favorite-dialog__folder-icon"><Folder /></el-icon>
            <span class="favorite-dialog__name">{{ folder.favoriteName }}</span>
            <span v-if="isDefaultFolder(folder)" class="favorite-dialog__tag">默认</span>
          </span>
          <span class="favorite-dialog__meta">
            <span class="favorite-dialog__count">
              {{ folderStates[folder.favoriteId]?.count ?? 0 }} 篇
            </span>
            <el-icon
              v-if="folderStates[folder.favoriteId]?.contains"
              class="favorite-dialog__check"
            >
              <Check />
            </el-icon>
          </span>
        </button>
      </div>

      <div class="favorite-dialog__footer">
        <el-button type="primary" plain :icon="Plus" @click="createDialogVisible = true">
          新建收藏夹
        </el-button>
      </div>
    </div>

    <CreateFolderDialog
      v-model="createDialogVisible"
      @created="handleFolderCreated"
    />
  </el-dialog>
</template>

<style scoped>
.favorite-dialog {
  min-height: 120px;
}

.favorite-dialog__list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 320px;
  overflow-y: auto;
}

.favorite-dialog__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--blog-border);
  border-radius: var(--blog-radius);
  background: #fff;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.favorite-dialog__item:hover {
  border-color: var(--blog-primary-light);
  background: var(--blog-primary-light);
}

.favorite-dialog__item.is-selected {
  border-color: var(--blog-primary);
  background: rgba(255, 102, 0, 0.06);
}

.favorite-dialog__item:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.favorite-dialog__item-main {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.favorite-dialog__folder-icon {
  color: var(--blog-primary);
  font-size: 18px;
  flex-shrink: 0;
}

.favorite-dialog__name {
  font-size: 14px;
  font-weight: 500;
  color: var(--blog-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.favorite-dialog__tag {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--blog-primary-light);
  color: var(--blog-primary);
  font-size: 11px;
  font-weight: 600;
}

.favorite-dialog__meta {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.favorite-dialog__count {
  font-size: 12px;
  color: var(--blog-text-secondary);
}

.favorite-dialog__check {
  color: var(--blog-primary);
  font-size: 16px;
}

.favorite-dialog__footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--blog-border);
}
</style>
