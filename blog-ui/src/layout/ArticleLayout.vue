<script setup>
import { useSeasonTheme } from '@/composables/useSeasonTheme'

useSeasonTheme()
</script>

<template>
  <div class="article-layout blog-grid-bg">
    <main class="article-layout__main">
      <div class="article-layout__container">
        <router-view />
      </div>
    </main>
  </div>
</template>

<style scoped>
.article-layout {
  min-height: 100vh;
}

.article-layout__main {
  padding: 32px 20px 48px;
}

.article-layout__container {
  max-width: 960px;
  margin: 0 auto;
}
</style>
