<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchAllCategories, fetchArticles, fetchHotTags } from '@/api/article'
import UserProfileCard from '@/components/sidebar/UserProfileCard.vue'
import AnnouncementCard from '@/components/sidebar/AnnouncementCard.vue'
import LatestArticlesCard from '@/components/sidebar/LatestArticlesCard.vue'
import TagCloudCard from '@/components/sidebar/TagCloudCard.vue'
import LifeCountdownCard from '@/components/sidebar/LifeCountdownCard.vue'

const articleCount = ref(0)
const tagCount = ref(0)
const categoryCount = ref(0)

const profile = computed(() => ({
  nickname: '岑兹',
  bio: '热爱技术，分享生活。专注于后端架构与前端工程化。',
  github: 'https://github.com/jicenwua',
  categoryCount: categoryCount.value,
  articleCount: articleCount.value,
  tagCount: tagCount.value,
}))

async function loadStats() {
  try {
    const [articleData, hotTags, categories] = await Promise.all([
      fetchArticles({ pageNum: 1, pageSize: 1 }),
      fetchHotTags(100),
      fetchAllCategories(),
    ])
    articleCount.value = articleData?.totalElements || 0
    tagCount.value = hotTags?.length || 0
    categoryCount.value = categories?.length || 0
  } catch {
    articleCount.value = 0
    tagCount.value = 0
    categoryCount.value = 0
  }
}

onMounted(loadStats)
</script>

<template>
  <aside class="blog-sidebar">
    <UserProfileCard :profile="profile" />
    <AnnouncementCard />
    <LatestArticlesCard />
    <TagCloudCard />
    <LifeCountdownCard />
  </aside>
</template>

<style scoped>
.blog-sidebar {
  width: var(--blog-sidebar-width);
  flex-shrink: 0;
}

@media (max-width: 992px) {
  .blog-sidebar {
    width: 100%;
  }
}
</style>
