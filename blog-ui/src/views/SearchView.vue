<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import ArticleList from '@/components/article/ArticleList.vue'

const route = useRoute()

const keyword = computed(() => String(route.query.q || '').trim())

const queryParams = computed(() => ({
  keyword: keyword.value,
}))

const title = computed(() =>
  keyword.value ? `搜索：${keyword.value}` : '搜索',
)
</script>

<template>
  <ArticleList
    v-if="keyword"
    :key="keyword"
    :title="title"
    :query-params="queryParams"
  />
  <el-empty v-else description="请输入关键词搜索文章" />
</template>
