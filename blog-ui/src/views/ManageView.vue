<script setup>
import SectionTitle from '@/components/common/SectionTitle.vue'
import { Document, Edit, Setting } from '@element-plus/icons-vue'
</script>

<template>
  <section class="manage-view">
    <SectionTitle title="博客管理" />

    <el-row :gutter="16">
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="manage-card">
          <el-icon :size="32" class="manage-card__icon"><Document /></el-icon>
          <h3>文章管理</h3>
          <p>创建、编辑和发布博客文章</p>
          <el-button type="primary" plain>进入</el-button>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="manage-card">
          <el-icon :size="32" class="manage-card__icon"><Edit /></el-icon>
          <h3>评论管理</h3>
          <p>审核和管理用户评论</p>
          <el-button type="primary" plain>进入</el-button>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="manage-card">
          <el-icon :size="32" class="manage-card__icon"><Setting /></el-icon>
          <h3>站点设置</h3>
          <p>配置博客基本信息与公告</p>
          <el-button type="primary" plain>进入</el-button>
        </el-card>
      </el-col>
    </el-row>
  </section>
</template>

<style scoped>
.manage-card {
  text-align: center;
  border: none;
  border-radius: var(--blog-radius);
  margin-bottom: 16px;
  padding: 12px 0;
}

.manage-card__icon {
  color: var(--blog-primary);
}

.manage-card h3 {
  margin: 12px 0 8px;
}

.manage-card p {
  margin: 0 0 16px;
  font-size: 13px;
  color: var(--blog-text-secondary);
}

.manage-card :deep(.el-button) {
  --el-button-text-color: var(--blog-primary);
  --el-button-border-color: var(--blog-primary);
  --el-button-hover-text-color: #fff;
  --el-button-hover-bg-color: var(--blog-primary);
  --el-button-hover-border-color: var(--blog-primary);
}
</style>
