<script setup>
defineProps({
  label: {
    type: String,
    required: true,
  },
  cover: {
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
  <article class="category-card" @click="emit('click')">
    <div class="category-card__cover">
      <div
        class="category-card__image"
        :style="{ backgroundImage: `url(${cover})` }"
        role="img"
        :aria-label="label"
      />
      <span class="category-card__badge">{{ count }}篇</span>
    </div>
    <div class="category-card__footer">
      {{ label }}
    </div>
  </article>
</template>

<style scoped>
.category-card {
  border-radius: 12px;
  overflow: hidden;
  background: var(--blog-card-bg);
  box-shadow: var(--blog-shadow);
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.category-card__cover {
  position: relative;
  height: 180px;
  background: var(--blog-border);
  overflow: hidden;
}

.category-card__image {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.category-card__badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 999px;
  background: linear-gradient(135deg, #4fd1c5 0%, #38b2ac 100%);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(56, 178, 172, 0.35);
}

.category-card__footer {
  padding: 14px 16px;
  text-align: center;
  font-size: 15px;
  font-weight: 500;
  color: var(--blog-text);
  background: rgba(255, 255, 255, 0.92);
  border-top: 1px solid var(--blog-border);
}
</style>
