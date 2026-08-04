<script setup>
import { Delete, Folder } from '@element-plus/icons-vue'

defineProps({
  name: {
    type: String,
    required: true,
  },
  count: {
    type: Number,
    default: 0,
  },
  defaultFolder: {
    type: Boolean,
    default: false,
  },
  deletable: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['click', 'delete'])
</script>

<template>
  <article class="favorite-folder-card" @click="emit('click')">
    <button
      v-if="deletable"
      type="button"
      class="favorite-folder-card__delete"
      aria-label="删除收藏夹"
      @click.stop="emit('delete')"
    >
      <el-icon><Delete /></el-icon>
    </button>

    <div class="favorite-folder-card__icon-wrap">
      <el-icon class="favorite-folder-card__icon"><Folder /></el-icon>
    </div>
    <div class="favorite-folder-card__name">
      {{ name }}
      <span v-if="defaultFolder" class="favorite-folder-card__tag">默认</span>
    </div>
    <div class="favorite-folder-card__count">{{ count }} 篇</div>
  </article>
</template>

<style scoped>
.favorite-folder-card {
  position: relative;
  border-radius: 12px;
  padding: 24px 16px 18px;
  background: var(--blog-card-bg);
  box-shadow: var(--blog-shadow);
  cursor: pointer;
  text-align: center;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.favorite-folder-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.favorite-folder-card__delete {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.95);
  color: var(--blog-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 1;
  transition: color 0.2s ease, background 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  z-index: 1;
}

.favorite-folder-card__delete:hover {
  color: #f56c6c;
  background: #fff;
}

.favorite-folder-card__icon-wrap {
  width: 72px;
  height: 72px;
  margin: 0 auto 12px;
  border-radius: 18px;
  background: linear-gradient(135deg, var(--blog-primary-light) 0%, var(--blog-card-bg) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.favorite-folder-card__icon {
  font-size: 34px;
  color: var(--blog-primary);
}

.favorite-folder-card__name {
  font-size: 15px;
  font-weight: 600;
  color: var(--blog-text);
  margin-bottom: 6px;
  word-break: break-all;
}

.favorite-folder-card__tag {
  display: inline-block;
  margin-left: 4px;
  padding: 1px 6px;
  border-radius: 999px;
  background: var(--blog-primary-light);
  color: var(--blog-primary);
  font-size: 11px;
  font-weight: 600;
  vertical-align: middle;
}

.favorite-folder-card__count {
  font-size: 12px;
  color: var(--blog-text-secondary);
}
</style>
