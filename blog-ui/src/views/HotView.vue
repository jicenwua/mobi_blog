<script setup>
import ArticleList from '@/components/article/ArticleList.vue'

const emit = defineEmits(['articles-loaded'])

function handleLoaded(articles) {
  emit('articles-loaded', articles)
}
</script>

<template>
  <ArticleList
    title="热门文章"
    :query-params="{ isHot: true }"
    @loaded="handleLoaded"
  />
</template>
