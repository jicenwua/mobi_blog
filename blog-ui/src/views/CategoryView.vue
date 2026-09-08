<script setup>
import { computed, onMounted, ref } from 'vue'
import SectionTitle from '@/components/common/SectionTitle.vue'
import ArticleList from '@/components/article/ArticleList.vue'
import CategoryCard from '@/components/category/CategoryCard.vue'
import TagIconCard from '@/components/category/TagIconCard.vue'
import { fetchAllCategories, fetchAllTags, fetchArticles } from '@/api/article'
import { getCategoryLabel } from '@/constants/categories'
import { getCategoryCover, getTagIcon, sortTags } from '@/utils/categoryAssets'

const emit = defineEmits(['articles-loaded'])

const viewMode = ref('category')
const selectedCategory = ref('')
const selectedTag = ref('')
const categories = ref([])
const categoryCounts = ref({})
const tags = ref([])
const loadingCategories = ref(false)
const loadingTags = ref(false)

const sortedTags = computed(() => sortTags(tags.value))

const activeCategoryLabel = computed(() => getCategoryLabel(selectedCategory.value))

function handleLoaded(articles) {
  emit('articles-loaded', articles)
}

function switchMode(mode) {
  viewMode.value = mode
  selectedCategory.value = ''
  selectedTag.value = ''
}

function openCategory(code) {
  selectedCategory.value = code
}

function openTag(tagName) {
  selectedTag.value = tagName
}

function backToCategoryGrid() {
  selectedCategory.value = ''
}

function backToTagGrid() {
  selectedTag.value = ''
}

async function loadCategories() {
  loadingCategories.value = true
  try {
    const codes = await fetchAllCategories() || []
    categories.value = codes

    const results = await Promise.all(
      codes.map(async (code) => {
        const data = await fetchArticles({
          category: code,
          pageNum: 1,
          pageSize: 1,
        })
        return [code, data?.totalElements || 0]
      }),
    )
    categoryCounts.value = Object.fromEntries(results)
  } catch {
    categories.value = []
    categoryCounts.value = {}
  } finally {
    loadingCategories.value = false
  }
}

async function loadTags() {
  loadingTags.value = true
  try {
    tags.value = await fetchAllTags() || []
  } catch {
    tags.value = []
  } finally {
    loadingTags.value = false
  }
}

onMounted(() => {
  loadCategories()
  loadTags()
})
</script>

<template>
  <section class="category-view">
    <aside class="category-view__nav">
      <button
        class="category-view__nav-item"
        :class="{ 'is-active': viewMode === 'category' && !selectedCategory }"
        @click="switchMode('category')"
      >
        分类
      </button>
      <button
        class="category-view__nav-item"
        :class="{ 'is-active': viewMode === 'tag' && !selectedTag }"
        @click="switchMode('tag')"
      >
        标签
      </button>
    </aside>

    <div class="category-view__main">
      <template v-if="viewMode === 'category' && !selectedCategory">
        <SectionTitle title="文章分类" />
        <div v-loading="loadingCategories" class="category-view__grid">
          <CategoryCard
            v-for="code in categories"
            :key="code"
            :label="getCategoryLabel(code)"
            :cover="getCategoryCover(code)"
            :count="categoryCounts[code] || 0"
            @click="openCategory(code)"
          />
          <el-empty
            v-if="!loadingCategories && !categories.length"
            class="category-view__empty"
            description="暂无分类"
          />
        </div>
      </template>

      <template v-else-if="viewMode === 'category' && selectedCategory">
        <button class="category-view__back" @click="backToCategoryGrid">
          ← 返回分类
        </button>
        <ArticleList
          :key="selectedCategory"
          :query-params="{ category: selectedCategory }"
          :title="activeCategoryLabel"
          @loaded="handleLoaded"
        />
      </template>

      <template v-else-if="viewMode === 'tag' && !selectedTag">
        <SectionTitle title="全部标签" />
        <div v-loading="loadingTags" class="category-view__grid category-view__grid--tags">
          <TagIconCard
            v-for="tag in sortedTags"
            :key="tag.name"
            :name="tag.name"
            :icon="getTagIcon(tag.name)"
            :count="tag.articleCount || 0"
            @click="openTag(tag.name)"
          />
          <el-empty
            v-if="!loadingTags && !sortedTags.length"
            class="category-view__empty"
            description="暂无标签"
          />
        </div>
      </template>

      <template v-else-if="viewMode === 'tag' && selectedTag">
        <button class="category-view__back" @click="backToTagGrid">
          ← 返回标签
        </button>
        <ArticleList
          :key="selectedTag"
          :query-params="{ tag: selectedTag, sortBy: 'view' }"
          :title="`标签：${selectedTag}`"
          @loaded="handleLoaded"
        />
      </template>
    </div>
  </section>
</template>

<style scoped>
.category-view {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.category-view__nav {
  width: 120px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  position: sticky;
  top: calc(var(--blog-header-height) + 24px);
}

.category-view__nav-item {
  border: none;
  border-radius: var(--blog-radius);
  padding: 12px 16px;
  background: var(--blog-card-bg);
  color: var(--blog-text);
  font-size: 14px;
  font-weight: 500;
  text-align: left;
  cursor: pointer;
  box-shadow: var(--blog-shadow);
  transition: background 0.2s ease, color 0.2s ease;
}

.category-view__nav-item:hover {
  color: var(--blog-primary);
}

.category-view__nav-item.is-active {
  background: var(--blog-primary);
  color: #fff;
}

.category-view__main {
  flex: 1;
  min-width: 0;
}

.category-view__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.category-view__grid--tags {
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
}

.category-view__back {
  border: none;
  background: none;
  color: var(--blog-primary);
  font-size: 14px;
  cursor: pointer;
  padding: 0;
  margin-bottom: 16px;
}

.category-view__back:hover {
  opacity: 0.8;
}

.category-view__empty {
  grid-column: 1 / -1;
}

@media (max-width: 768px) {
  .category-view {
    flex-direction: column;
  }

  .category-view__nav {
    width: 100%;
    flex-direction: row;
    position: static;
  }

  .category-view__nav-item {
    flex: 1;
    text-align: center;
  }
}
</style>
