<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import ArticleList from '@/components/article/ArticleList.vue'

const route = useRoute()
const tag = computed(() => route.params.tag)

const queryParams = computed(() => ({
  tag: tag.value,
  sortBy: 'view',
}))
</script>

<template>
  <ArticleList
    :title="`标签：${tag}`"
    :query-params="queryParams"
  />
</template>
