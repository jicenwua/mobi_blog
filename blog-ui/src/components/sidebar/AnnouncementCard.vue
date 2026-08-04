<script setup>
import SectionTitle from '@/components/common/SectionTitle.vue'

defineProps({
  content: {
    type: String,
    default: '欢迎来到我的博客！这里记录技术探索与生活感悟，感谢你的访问。',
  },
})
</script>

<template>
  <el-card class="sidebar-card" shadow="never">
    <SectionTitle title="公告信息" />
    <p class="announcement">{{ content }}</p>
    <p class="announcement-disclaimer">
      <span class="announcement-disclaimer__label">免责声明：</span>
      本站
      <strong class="announcement-disclaimer__highlight">仅作为学习及作品展示使用</strong>
      ，内容不代表任何商业用途或专业建议。
    </p>
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

.announcement {
  margin: 0 0 10px;
  font-size: 13px;
  color: var(--blog-text-secondary);
  line-height: 1.8;
}

.announcement-disclaimer {
  margin: 0;
  padding: 10px 12px;
  border-radius: 6px;
  background: var(--blog-primary-light);
  font-size: 13px;
  color: var(--blog-text-secondary);
  line-height: 1.8;
}

.announcement-disclaimer__label {
  font-weight: 600;
  color: var(--blog-primary);
}

.announcement-disclaimer__highlight {
  color: var(--blog-primary);
  font-weight: 700;
}
</style>
