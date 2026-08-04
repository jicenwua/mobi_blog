<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import SectionTitle from '@/components/common/SectionTitle.vue'
import ArticleCard from '@/components/article/ArticleCard.vue'
import CreateFolderDialog from '@/components/favorite/CreateFolderDialog.vue'
import FavoriteFolderCard from '@/components/favorite/FavoriteFolderCard.vue'
import { fetchArticleDetail } from '@/api/article'
import {
  deleteFavoriteFolder,
  fetchFavoriteFolders,
  fetchFolderArticles,
} from '@/api/favorite'
import { dedupeFavoriteFolders, folderKey, isDefaultFolder } from '@/constants/favorite'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const folders = ref([])
const folderCounts = ref({})
const loadingFolders = ref(false)
const selectedFolder = ref(null)
const createDialogVisible = ref(false)

const articles = ref([])
const loadingArticles = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const sortedFolders = computed(() => dedupeFavoriteFolders(folders.value))
const activeFolderName = computed(() => selectedFolder.value?.favoriteName || '')

async function loadFolderCounts(folderList) {
  const counts = {}
  await Promise.all(
    folderList.map(async (folder) => {
      try {
        const articlesData = await fetchFolderArticles(folder.favoriteId, {
          pageNum: 1,
          pageSize: 1,
        })
        counts[folder.favoriteId] =
          articlesData?.total ?? articlesData?.totalElements ?? 0
      } catch {
        counts[folder.favoriteId] = 0
      }
    }),
  )
  folderCounts.value = counts
}

async function loadFolders() {
  if (!userStore.isLoggedIn) return

  loadingFolders.value = true
  try {
    const data = await fetchFavoriteFolders({ pageNum: 1, pageSize: 100 })
    folders.value = dedupeFavoriteFolders(data?.records || data?.content || [])
    await loadFolderCounts(folders.value)
  } catch {
    folders.value = []
    folderCounts.value = {}
  } finally {
    loadingFolders.value = false
  }
}

async function loadFolderArticles() {
  if (!selectedFolder.value) return

  loadingArticles.value = true
  try {
    const data = await fetchFolderArticles(selectedFolder.value.favoriteId, {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    })
    const relations = data?.records || data?.content || []
    total.value = data?.total ?? data?.totalElements ?? relations.length

    const details = await Promise.all(
      relations.map(async (item) => {
        try {
          return await fetchArticleDetail(item.articleId)
        } catch {
          return null
        }
      }),
    )
    articles.value = details.filter(Boolean)
  } catch {
    articles.value = []
    total.value = 0
  } finally {
    loadingArticles.value = false
  }
}

function openFolder(folder) {
  selectedFolder.value = folder
  pageNum.value = 1
  loadFolderArticles()
}

function backToFolders() {
  selectedFolder.value = null
  articles.value = []
  loadFolders()
}

function handlePageChange(page) {
  pageNum.value = page
  loadFolderArticles()
}

function handleFolderCreated() {
  createDialogVisible.value = false
  loadFolders()
}

async function handleDeleteFolder(folder) {
  if (isDefaultFolder(folder)) {
    ElMessage.warning('默认收藏夹不可删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定删除收藏夹「${folder.favoriteName}」吗？其中的收藏记录也会被移除。`,
      '删除收藏夹',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消',
      },
    )
    await deleteFavoriteFolder(folder.favoriteId)
    ElMessage.success('收藏夹已删除')
    if (selectedFolder.value?.favoriteId === folder.favoriteId) {
      selectedFolder.value = null
      articles.value = []
    }
    loadFolders()
  } catch {
    // 用户取消或请求失败
  }
}

function goLogin() {
  router.push({
    name: 'Login',
    query: { redirect: route.fullPath },
  })
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    loadFolders()
  }
})

watch(
  () => userStore.isLoggedIn,
  (loggedIn) => {
    if (loggedIn) {
      loadFolders()
      return
    }
    selectedFolder.value = null
    folders.value = []
    articles.value = []
  },
)
</script>

<template>
  <section class="favorites-view">
    <template v-if="!userStore.isLoggedIn">
      <SectionTitle title="我的收藏" />
      <div class="favorites-view__login">
        <el-empty description="请先登录">
          <el-button type="primary" @click="goLogin">去登录</el-button>
        </el-empty>
      </div>
    </template>

    <template v-else-if="!selectedFolder">
      <div class="favorites-view__header">
        <SectionTitle title="我的收藏夹" />
        <el-button type="primary" plain :icon="Plus" @click="createDialogVisible = true">
          新建收藏夹
        </el-button>
      </div>

      <div v-loading="loadingFolders" class="favorites-view__grid">
        <FavoriteFolderCard
          v-for="folder in sortedFolders"
          :key="folderKey(folder)"
          :name="folder.favoriteName"
          :count="folderCounts[folder.favoriteId] || 0"
          :default-folder="isDefaultFolder(folder)"
          :deletable="!isDefaultFolder(folder)"
          @click="openFolder(folder)"
          @delete="handleDeleteFolder(folder)"
        />
      </div>
    </template>

    <template v-else>
      <div class="favorites-view__detail-header">
        <button type="button" class="favorites-view__back" @click="backToFolders">
          ← 返回收藏夹列表
        </button>
        <SectionTitle :title="activeFolderName" />
      </div>

      <div v-loading="loadingArticles">
        <ArticleCard
          v-for="article in articles"
          :key="article.id"
          :article="article"
        />
        <el-empty v-if="!loadingArticles && !articles.length" description="该收藏夹暂无文章" />

        <div v-if="total > pageSize" class="favorites-view__pagination">
          <el-pagination
            v-model:current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            background
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </template>

    <CreateFolderDialog
      v-model="createDialogVisible"
      @created="handleFolderCreated"
    />
  </section>
</template>

<style scoped>
.favorites-view__login {
  padding: 48px 0;
}

.favorites-view__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 4px;
}

.favorites-view__header :deep(.section-title) {
  margin-bottom: 0;
}

.favorites-view__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.favorites-view__back {
  border: none;
  background: none;
  color: var(--blog-primary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  padding: 0;
  margin-bottom: 12px;
  display: inline-flex;
  align-items: center;
}

.favorites-view__back:hover {
  opacity: 0.8;
}

.favorites-view__detail-header {
  margin-bottom: 8px;
}

.favorites-view__detail-header :deep(.section-title) {
  margin-bottom: 0;
}

.favorites-view__pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.favorites-view__pagination :deep(.el-pagination.is-background .el-pager li.is-active) {
  background-color: var(--blog-primary);
}
</style>
