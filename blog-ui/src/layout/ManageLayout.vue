<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  DataAnalysis,
  ChatDotRound,
  Document,
  User,
  Notebook,
  EditPen,
  Collection,
  CircleClose,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const menuItems = [
  { key: 'stats', label: '统计', route: '/manage/stats', icon: DataAnalysis },
  { key: 'publish', label: '发布文章', route: '/manage/publish', icon: EditPen },
  { key: 'my-articles', label: '我的文章', route: '/manage/my-articles', icon: Collection },
  { key: 'comments', label: '评论管理', route: '/manage/comments', icon: ChatDotRound },
  { key: 'articles', label: '文章管理', route: '/manage/articles', icon: Document },
  { key: 'users', label: '用户管理', route: '/manage/users', icon: User },
  { key: 'logs', label: '日志记录', route: '/manage/logs', icon: Notebook },
  { key: 'blacklist', label: '黑名单', route: '/manage/blacklist', icon: CircleClose },
]

const activeMenu = computed(() => route.meta.manageMenu || 'stats')

function navigate(item) {
  if (route.path !== item.route) {
    router.push(item.route)
  }
}
</script>

<template>
  <div class="manage-layout">
    <aside class="manage-layout__sidebar">
      <div class="manage-layout__sidebar-title">博客管理</div>
      <nav class="manage-layout__nav">
        <button
          v-for="item in menuItems"
          :key="item.key"
          type="button"
          class="manage-nav-item"
          :class="{ 'is-active': activeMenu === item.key }"
          @click="navigate(item)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </button>
      </nav>
    </aside>

    <div class="manage-layout__content">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.manage-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  min-height: calc(100vh - var(--blog-header-height) - 64px);
}

.manage-layout__sidebar {
  width: 220px;
  flex-shrink: 0;
  background: var(--blog-card-bg);
  border-radius: var(--blog-radius);
  box-shadow: var(--blog-shadow);
  padding: 20px 12px;
  position: sticky;
  top: calc(var(--blog-header-height) + 24px);
}

.manage-layout__sidebar-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--blog-text);
  padding: 0 12px 16px;
  border-bottom: 1px solid var(--blog-border);
  margin-bottom: 12px;
}

.manage-layout__nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.manage-nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 12px 14px;
  border: none;
  border-radius: var(--blog-radius);
  background: transparent;
  color: var(--blog-text);
  font-size: 14px;
  cursor: pointer;
  text-align: left;
  transition: background 0.2s ease, color 0.2s ease;
}

.manage-nav-item:hover {
  background: var(--blog-primary-light);
  color: var(--blog-primary);
}

.manage-nav-item.is-active {
  background: var(--blog-primary-light);
  color: var(--blog-primary);
  font-weight: 600;
}

.manage-layout__content {
  flex: 1;
  min-width: 0;
}

@media (max-width: 768px) {
  .manage-layout {
    flex-direction: column;
  }

  .manage-layout__sidebar {
    width: 100%;
    position: static;
  }

  .manage-layout__nav {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .manage-nav-item {
    flex: 1 1 auto;
    justify-content: center;
    min-width: 100px;
  }
}
</style>
