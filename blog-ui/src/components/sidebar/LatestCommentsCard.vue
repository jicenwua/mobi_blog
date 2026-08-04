<script setup>
import SectionTitle from '@/components/common/SectionTitle.vue'
import { formatDate } from '@/utils/format'

defineProps({
  comments: {
    type: Array,
    default: () => [],
  },
})
</script>

<template>
  <el-card class="sidebar-card" shadow="never">
    <SectionTitle title="最新回复" />
    <div class="comment-list">
      <div v-for="item in comments" :key="item.id" class="comment-item">
        <el-avatar :size="32" class="comment-item__avatar">
          {{ item.userNickName?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="comment-item__body">
          <div class="comment-item__header">
            <span class="comment-item__name">{{ item.userNickName }}</span>
            <span class="comment-item__date">{{ formatDate(item.createTime) }}</span>
          </div>
          <p class="comment-item__content">{{ item.content }}</p>
        </div>
      </div>
      <div v-if="!comments.length" class="comment-list__empty">暂无回复</div>
    </div>
  </el-card>
</template>

<style scoped>
.sidebar-card {
  border: none;
  border-radius: var(--blog-radius);
  margin-bottom: 16px;
}

.sidebar-card :deep(.section-title) {
  margin-bottom: 12px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.comment-item {
  display: flex;
  gap: 10px;
}

.comment-item__avatar {
  flex-shrink: 0;
  background: var(--blog-primary-light);
  color: var(--blog-primary);
}

.comment-item__body {
  flex: 1;
  min-width: 0;
}

.comment-item__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.comment-item__name {
  font-size: 13px;
  font-weight: 600;
}

.comment-item__date {
  font-size: 12px;
  color: var(--blog-text-secondary);
}

.comment-item__content {
  margin: 0;
  font-size: 13px;
  color: var(--blog-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.comment-list__empty {
  font-size: 13px;
  color: var(--blog-text-secondary);
}
</style>
