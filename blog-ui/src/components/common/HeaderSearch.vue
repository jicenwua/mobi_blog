<script setup>
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'

const expanded = ref(false)
const keyword = ref('')
const searchRef = ref(null)
const inputRef = ref(null)
const router = useRouter()
const route = useRoute()

function openSearch() {
  expanded.value = true
  nextTick(() => inputRef.value?.focus())
}

function closeSearch() {
  expanded.value = false
  keyword.value = ''
}

function toggleSearch(event) {
  event.stopPropagation()
  if (expanded.value) {
    handleSearch()
    return
  }
  openSearch()
}

function handleSearch() {
  const value = keyword.value.trim()
  if (!value) return

  closeSearch()
  router.push({ name: 'Search', query: { q: value } })
}

function handleClickOutside(event) {
  if (expanded.value && searchRef.value && !searchRef.value.contains(event.target)) {
    closeSearch()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  if (route.name === 'Search' && route.query.q) {
    keyword.value = String(route.query.q)
    expanded.value = true
  }
})

onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>

<template>
  <div
    ref="searchRef"
    class="header-search"
    :class="{ 'is-expanded': expanded }"
    @click.stop
  >
    <input
      ref="inputRef"
      v-model="keyword"
      class="header-search__input"
      type="text"
      placeholder="搜索文章..."
      @keyup.enter="handleSearch"
    />
    <button
      type="button"
      class="header-search__btn"
      :aria-label="expanded ? '搜索' : '展开搜索'"
      @click="toggleSearch"
    >
      <el-icon :size="16"><Search /></el-icon>
    </button>
  </div>
</template>

<style scoped>
.header-search {
  position: relative;
  display: inline-flex;
  flex-direction: row-reverse;
  align-items: center;
  width: 40px;
  height: 40px;
  border-radius: 999px;
  background: var(--blog-primary);
  overflow: hidden;
  transition: width 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
}

.header-search.is-expanded {
  width: 240px;
}

.header-search__input {
  flex: 0 0 0;
  width: 0;
  min-width: 0;
  height: 100%;
  padding: 0;
  border: none;
  outline: none;
  background: transparent;
  color: #fff;
  font-size: 14px;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.25s ease, flex 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.header-search.is-expanded .header-search__input {
  flex: 1 1 auto;
  width: auto;
  padding: 0 8px 0 16px;
  opacity: 1;
  pointer-events: auto;
}

.header-search__input::placeholder {
  color: rgba(255, 255, 255, 0.75);
}

.header-search__btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  color: #fff;
  cursor: pointer;
  flex-shrink: 0;
}

.header-search__btn :deep(.el-icon) {
  color: #fff;
  font-size: 16px;
}

@media (max-width: 768px) {
  .header-search.is-expanded {
    width: 180px;
  }
}
</style>
