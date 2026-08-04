<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
})
</script>

<template>
  <div class="section-title">
    <span class="section-title__bar" />
    <span class="section-title__text">{{ title }}</span>
  </div>
</template>

<style scoped>
.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.section-title__bar {
  width: 4px;
  height: 20px;
  background: var(--blog-primary);
  border-radius: 2px;
}

.section-title__text {
  font-size: 18px;
  font-weight: 600;
  color: var(--blog-text);
}
</style>
