<script setup>
import { ref } from 'vue'
import SectionTitle from '@/components/common/SectionTitle.vue'

const skills = ref(['Java', 'Spring Boot', 'Vue 3', 'MongoDB', 'Redis', 'Docker'])
</script>

<template>
  <section class="about-view">
    <SectionTitle title="关于我" />

    <el-card shadow="never" class="about-card">
      <div class="about-card__content">
        <el-avatar :size="80" class="about-card__avatar">M</el-avatar>
        <h2>Mobi Blog</h2>
        <p>
          你好，欢迎来到我的个人博客。我是一名全栈开发者，专注于 Java 后端与 Vue 前端技术栈。
        </p>
        <p>
          这里记录我在技术探索中的心得体会，涵盖 Spring 生态、微服务架构、数据库优化以及 AI 辅助开发等话题。
        </p>
        <p>
          如果你对我的文章有任何想法，欢迎在评论区留言交流。
        </p>

        <div class="about-card__skills">
          <el-tag v-for="skill in skills" :key="skill" effect="plain" type="warning">
            {{ skill }}
          </el-tag>
        </div>
      </div>
    </el-card>
  </section>
</template>

<style scoped>
.about-card {
  border: none;
  border-radius: var(--blog-radius);
}

.about-card__content {
  text-align: center;
  padding: 20px;
}

.about-card__avatar {
  background: var(--blog-primary);
  color: #fff;
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 16px;
}

.about-card__content h2 {
  margin: 0 0 16px;
}

.about-card__content p {
  margin: 0 0 12px;
  color: var(--blog-text-secondary);
  line-height: 1.8;
  text-align: left;
}

.about-card__skills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  margin-top: 24px;
}
</style>
