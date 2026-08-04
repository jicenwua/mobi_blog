<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/layout/AppHeader.vue'
import BlogSidebar from '@/layout/BlogSidebar.vue'
import BackToTop from '@/components/common/BackToTop.vue'
import SeasonThemeFab from '@/components/common/SeasonThemeFab.vue'
import SeasonParticles from '@/components/common/SeasonParticles.vue'
import { useSeasonTheme } from '@/composables/useSeasonTheme'

useSeasonTheme()

const route = useRoute()
const showSidebar = computed(() => !route.path.startsWith('/manage'))
</script>

<template>
  <div class="app-layout blog-grid-bg">
    <AppHeader />

    <main class="app-layout__main">
      <div class="app-layout__container">
        <div class="app-layout__content">
          <router-view />
        </div>
        <BlogSidebar v-if="showSidebar" />
      </div>
    </main>

    <SeasonParticles />
    <SeasonThemeFab />
    <BackToTop />
  </div>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
}

.app-layout__main {
  padding: 24px 20px 40px;
}

.app-layout__container {
  max-width: var(--blog-max-width);
  margin: 0 auto;
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.app-layout__content {
  flex: 1;
  min-width: 0;
}

@media (max-width: 992px) {
  .app-layout__container {
    flex-direction: column;
  }
}
</style>
