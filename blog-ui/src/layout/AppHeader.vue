<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import HeaderSearch from '@/components/common/HeaderSearch.vue'

import logoIcon from '@/assets/icon/dangao.svg'
import iconHot from '@/assets/icon/huoguo.svg'
import iconCategory from '@/assets/icon/shala.svg'
import iconFavorite from '@/assets/icon/hanbao.svg'
import iconAbout from '@/assets/icon/bingqilin.svg'
import iconManage from '@/assets/icon/jitui.svg'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const baseMenus = [
  { key: 'hot', label: '热门', route: '/', icon: iconHot },
  { key: 'category', label: '分类', route: '/category', icon: iconCategory },
  { key: 'favorite', label: '收藏', route: '/favorite', icon: iconFavorite },
  { key: 'about', label: '关于我', route: '/about', icon: iconAbout },
]

const menuItems = computed(() => {
  const items = [...baseMenus]
  if (userStore.isAdmin) {
    items.push({ key: 'manage', label: '管理', route: '/manage', icon: iconManage })
  }
  return items
})

const activeMenu = computed(() => route.meta.menu || 'hot')

const nickname = computed(() => userStore.userInfo?.nickname || '')

const avatarText = computed(() => {
  const name = nickname.value.trim()
  return name ? name.charAt(0).toUpperCase() : '?'
})

function navigate(item) {
  router.push(item.route)
}

function goHome() {
  router.push('/')
}

function openLogin() {
  router.push({
    path: '/login',
    query: { redirect: route.fullPath },
  })
}

function handleLogout() {
  userStore.logout()
}
</script>

<template>
  <header class="app-header">
    <div class="app-header__inner">
      <div class="app-header__brand">
        <button type="button" class="app-header__logo" aria-label="返回首页" @click="goHome">
          <img :src="logoIcon" alt="" class="app-header__logo-img" />
        </button>

        <span class="app-header__divider" aria-hidden="true" />

        <nav class="app-header__nav">
          <button
            v-for="item in menuItems"
            :key="item.key"
            type="button"
            class="nav-item"
            :class="{ 'is-active': activeMenu === item.key }"
            @click="navigate(item)"
          >
            <img :src="item.icon" alt="" class="nav-item__icon" />
            <span class="nav-item__label">{{ item.label }}</span>
            <span class="nav-item__indicator" />
          </button>
        </nav>
      </div>

      <div class="app-header__actions">
        <HeaderSearch />

        <template v-if="userStore.isLoggedIn">
          <el-dropdown trigger="click" @command="handleLogout">
            <button type="button" class="user-profile">
              <span class="user-profile__name">{{ nickname }}</span>
              <span class="user-profile__avatar">{{ avatarText }}</span>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <button
          v-else
          type="button"
          class="login-btn"
          @click="openLogin"
        >
          登录
        </button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 200;
  background: var(--blog-card-bg);
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);
}

.app-header__inner {
  max-width: var(--blog-max-width);
  margin: 0 auto;
  height: var(--blog-header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  gap: 24px;
}

.app-header__brand {
  display: flex;
  align-items: center;
  min-width: 0;
  flex: 1;
  gap: 20px;
}

.app-header__logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  flex-shrink: 0;
}

.app-header__logo-img {
  width: 36px;
  height: 36px;
  object-fit: contain;
}

.app-header__divider {
  width: 1px;
  height: 28px;
  background: var(--blog-border);
  flex-shrink: 0;
}

.app-header__nav {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
  overflow-x: auto;
  scrollbar-width: none;
}

.app-header__nav::-webkit-scrollbar {
  display: none;
}

.nav-item {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px 12px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--blog-text);
  font-size: 15px;
  white-space: nowrap;
  transition: color 0.2s ease;
}

.nav-item:hover,
.nav-item.is-active {
  color: var(--blog-primary);
}

.nav-item__icon {
  width: 22px;
  height: 22px;
  object-fit: contain;
  flex-shrink: 0;
}

.nav-item__indicator {
  position: absolute;
  left: 10px;
  right: 10px;
  bottom: 2px;
  height: 4px;
  border-radius: 999px;
  background: var(--blog-primary);
  opacity: 0;
  transform: scaleX(0.6);
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.nav-item:hover .nav-item__indicator,
.nav-item.is-active .nav-item__indicator {
  opacity: 1;
  transform: scaleX(1);
}

.app-header__actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.login-btn {
  height: 36px;
  padding: 0 18px;
  border: 1px solid var(--blog-primary);
  border-radius: 999px;
  background: transparent;
  color: var(--blog-primary);
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.login-btn:hover {
  background: var(--blog-primary);
  color: #fff;
}

.user-profile {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 4px 4px 4px 12px;
  border: 1px solid var(--blog-border);
  border-radius: 999px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.user-profile:hover {
  border-color: var(--blog-primary-light);
  box-shadow: 0 2px 8px rgba(255, 102, 0, 0.12);
}

.user-profile__name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  color: var(--blog-text);
}

.user-profile__avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--blog-primary);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .app-header__inner {
    gap: 12px;
    padding: 0 12px;
  }

  .app-header__brand {
    gap: 12px;
  }

  .nav-item {
    padding: 8px 10px 12px;
    font-size: 14px;
  }

  .nav-item__icon {
    width: 20px;
    height: 20px;
  }

  .user-profile__name {
    display: none;
  }
}
</style>
