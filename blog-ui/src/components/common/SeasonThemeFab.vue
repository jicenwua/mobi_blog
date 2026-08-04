<script setup>
import { ref } from 'vue'
import { EditPen } from '@element-plus/icons-vue'
import { useSeasonTheme } from '@/composables/useSeasonTheme'

const { SEASONS, activeSeason, selectSeason } = useSeasonTheme()
const hovered = ref(false)
</script>

<template>
  <div
    class="season-fab"
    @mouseenter="hovered = true"
    @mouseleave="hovered = false"
  >
    <div class="season-fab__actions" :class="{ 'is-expanded': hovered }">
      <button
        v-for="(season, index) in SEASONS"
        :key="season.key"
        class="season-fab__season-btn"
        :class="[
          `season-fab__season-btn--${season.key}`,
          { 'is-active': activeSeason === season.key },
        ]"
        :style="{ '--slide-index': SEASONS.length - 1 - index }"
        :title="season.label"
        @click="selectSeason(season.key)"
      >
        {{ season.label }}
      </button>
    </div>

    <button class="season-fab__trigger" :class="{ 'is-expanded': hovered }" title="季节主题">
      <el-icon :size="20">
        <EditPen />
      </el-icon>
    </button>
  </div>
</template>

<style scoped>
.season-fab {
  position: fixed;
  right: 32px;
  bottom: 32px;
  z-index: 100;
  display: flex;
  align-items: center;
  flex-direction: row;
  gap: 10px;
}

.season-fab__trigger {
  width: 48px;
  height: 48px;
  border: none;
  border-radius: 14px;
  background: var(--blog-primary);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px var(--blog-primary-shadow);
  transition:
    transform 0.45s cubic-bezier(0.34, 1.56, 0.64, 1),
    box-shadow 0.3s ease,
    background 0.3s ease;
}

.season-fab__trigger:hover {
  background: var(--blog-primary-hover);
  box-shadow: 0 6px 20px var(--blog-primary-shadow-hover);
}

.season-fab__trigger.is-expanded {
  transform: rotate(360deg);
}

.season-fab__actions {
  display: flex;
  align-items: center;
  flex-direction: row;
  gap: 10px;
}

.season-fab__season-btn {
  width: 44px;
  height: 44px;
  border: 2px solid transparent;
  border-radius: 12px;
  background: var(--blog-card-bg);
  color: var(--blog-text);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: var(--blog-shadow);
  opacity: 0;
  transform: translateX(24px) scale(0.6);
  pointer-events: none;
  transition:
    opacity 0.35s cubic-bezier(0.34, 1.56, 0.64, 1),
    transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1),
    background 0.25s ease,
    color 0.25s ease,
    border-color 0.25s ease,
    box-shadow 0.25s ease;
  transition-delay: calc(var(--slide-index) * 0.06s);
}

.season-fab__actions.is-expanded .season-fab__season-btn {
  opacity: 1;
  transform: translateX(0) scale(1);
  pointer-events: auto;
}

.season-fab__season-btn:hover {
  transform: translateX(0) scale(1.08);
}

.season-fab__season-btn--spring.is-active {
  background: #ffb6c1;
  border-color: #ff8fab;
  color: #8b3a62;
}

.season-fab__season-btn--summer.is-active {
  background: #67c23a;
  border-color: #529b2e;
  color: #fff;
}

.season-fab__season-btn--autumn.is-active {
  background: #ffb347;
  border-color: #e69520;
  color: #7a4a00;
}

.season-fab__season-btn--winter.is-active {
  background: #64b5f6;
  border-color: #42a5f5;
  color: #fff;
  box-shadow: 0 2px 12px rgba(66, 165, 245, 0.35);
}
</style>
