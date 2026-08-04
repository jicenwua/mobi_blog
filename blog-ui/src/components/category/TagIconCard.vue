<script setup>
defineProps({
  name: {
    type: String,
    required: true,
  },
  icon: {
    type: String,
    required: true,
  },
  count: {
    type: Number,
    default: 0,
  },
})

const emit = defineEmits(['click'])
</script>

<template>
  <article class="tag-card" @click="emit('click')">
    <div class="tag-card__icon-wrap">
      <img :src="icon" :alt="name" class="tag-card__icon">
    </div>
    <div class="tag-card__name">{{ name }}</div>
    <div class="tag-card__count">{{ count }} 篇</div>
  </article>
</template>

<style scoped>
.tag-card {
  border-radius: 12px;
  padding: 20px 12px 16px;
  background: var(--blog-card-bg);
  box-shadow: var(--blog-shadow);
  cursor: pointer;
  text-align: center;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.tag-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.tag-card__icon-wrap {
  width: 72px;
  height: 72px;
  margin: 0 auto 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--blog-primary-light) 0%, var(--blog-card-bg) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.tag-card__icon {
  width: 44px;
  height: 44px;
  object-fit: contain;
}

.tag-card__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--blog-text);
  margin-bottom: 4px;
  word-break: break-all;
}

.tag-card__count {
  font-size: 12px;
  color: var(--blog-text-secondary);
}
</style>
