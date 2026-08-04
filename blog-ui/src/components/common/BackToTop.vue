<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { Top } from '@element-plus/icons-vue'

const visible = ref(false)

function handleScroll() {
  visible.value = window.scrollY > 300
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => window.addEventListener('scroll', handleScroll))
onUnmounted(() => window.removeEventListener('scroll', handleScroll))
</script>

<template>
  <Transition name="fade">
    <el-button
      v-show="visible"
      class="back-to-top"
      type="primary"
      circle
      :icon="Top"
      @click="scrollToTop"
    />
  </Transition>
</template>

<style scoped>
.back-to-top {
  position: fixed;
  right: 32px;
  bottom: 96px;
  z-index: 100;
  --el-button-bg-color: var(--blog-primary);
  --el-button-border-color: var(--blog-primary);
  --el-button-hover-bg-color: var(--blog-primary-hover);
  --el-button-hover-border-color: var(--blog-primary-hover);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
