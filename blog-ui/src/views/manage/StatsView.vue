<script setup>
import { onMounted, ref } from 'vue'
import SectionTitle from '@/components/common/SectionTitle.vue'
import { fetchAdminStats } from '@/api/admin'

const loading = ref(false)
const stats = ref({
  userCount: 0,
  articleCount: 0,
  commentCount: 0,
  adminCount: 0,
})

const cards = [
  { key: 'userCount', label: '用户总数', color: '#409eff' },
  { key: 'articleCount', label: '文章总数', color: '#67c23a' },
  { key: 'commentCount', label: '评论总数', color: '#e6a23c' },
  { key: 'adminCount', label: '管理员数', color: '#f56c6c' },
]

async function loadStats() {
  loading.value = true
  try {
    stats.value = await fetchAdminStats()
  } finally {
    loading.value = false
  }
}

onMounted(loadStats)
</script>

<template>
  <section class="manage-page" v-loading="loading">
    <SectionTitle title="数据统计" />
    <el-row :gutter="16">
      <el-col v-for="card in cards" :key="card.key" :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card__value" :style="{ color: card.color }">
            {{ stats[card.key] ?? 0 }}
          </div>
          <div class="stat-card__label">{{ card.label }}</div>
        </el-card>
      </el-col>
    </el-row>
  </section>
</template>

<style scoped>
.manage-page {
  background: var(--blog-card-bg);
  border-radius: var(--blog-radius);
  box-shadow: var(--blog-shadow);
  padding: 24px;
}

.stat-card {
  text-align: center;
  margin-bottom: 16px;
  border: none;
}

.stat-card__value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-card__label {
  margin-top: 8px;
  font-size: 14px;
  color: var(--blog-text-secondary);
}
</style>
