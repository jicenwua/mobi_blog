<script setup>
import { ref } from 'vue'
import SectionTitle from '@/components/common/SectionTitle.vue'

const skills = ref(['Java', 'Spring Boot', 'Vue 3', 'MongoDB', 'Redis', 'Docker'])

const projects = ref([
  {
    label: '微信小程序',
    name: '尚品发艺会员助手',
    url: '',
  },
  {
    label: '小程序源码',
    name: 'mobi-app',
    url: 'https://github.com/jicenwua/mobi-app',
  },
  {
    label: '博客源码',
    name: 'mobi_blog',
    url: 'https://github.com/jicenwua/mobi_blog',
  },
  {
    label: '公共模块',
    name: 'commons',
    url: 'https://github.com/jicenwua/commons',
  },
])
</script>

<template>
  <section class="about-view">
    <SectionTitle title="关于我" />

    <el-card shadow="never" class="about-card">
      <div class="about-card__content">
        <el-avatar :size="80" class="about-card__avatar">岑</el-avatar>
        <h2>岑兹博客</h2>
        <p>
          你好，欢迎来到我的个人博客。我是一名全栈开发者，专注于 Java 后端、Vue 前端与微信小程序开发。
        </p>
        <p>
          目前主要维护「尚品发艺会员助手」微信小程序，以及基于 Spring Cloud 的微服务会员系统；
          本博客则用于记录我在技术探索中的心得体会，涵盖 Spring 生态、微服务架构、数据库优化与 AI 辅助开发等话题。
        </p>
        <p>
          下方整理了我在 GitHub 上的开源项目，欢迎 Star 与交流。若对我的文章有任何想法，也欢迎在评论区留言。
        </p>

        <div class="about-card__projects">
          <h3 class="about-card__section-title">项目与开源</h3>
          <ul class="about-card__project-list">
            <li v-for="item in projects" :key="item.label" class="about-card__project-item">
              <span class="about-card__project-label">{{ item.label }}</span>
              <a
                v-if="item.url"
                :href="item.url"
                target="_blank"
                rel="noopener noreferrer"
                class="about-card__project-link"
              >
                <svg class="about-card__github-icon" viewBox="0 0 19 19" aria-hidden="true">
                  <path
                    fill="currentColor"
                    fill-rule="evenodd"
                    d="M9.356 1.85C5.05 1.85 1.57 5.356 1.57 9.694a7.84 7.84 0 0 0 5.324 7.44c.387.079.528-.168.528-.376 0-.182-.013-.805-.013-1.454-2.165.467-2.616-.935-2.616-.935-.349-.91-.864-1.143-.864-1.143-.71-.48.051-.48.051-.48.787.051 1.2.805 1.2.805.695 1.194 1.817.857 2.268.649.064-.507.27-.857.49-1.052-1.728-.182-3.545-.857-3.545-3.87 0-.857.31-1.558.8-2.104-.078-.195-.349-1 .077-2.078 0 0 .657-.208 2.14.805a7.5 7.5 0 0 1 1.946-.26c.657 0 1.328.092 1.946.26 1.483-1.013 2.14-.805 2.14-.805.426 1.078.155 1.883.078 2.078.502.546.799 1.247.799 2.104 0 3.013-1.818 3.675-3.558 3.87.284.247.528.714.528 1.454 0 1.052-.012 1.896-.012 2.156 0 .208.142.455.528.377a7.84 7.84 0 0 0 5.324-7.441c.013-4.338-3.48-7.844-7.773-7.844"
                    clip-rule="evenodd"
                  />
                </svg>
                {{ item.name }}
              </a>
              <span v-else class="about-card__project-name">{{ item.name }}</span>
            </li>
          </ul>
        </div>

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

.about-card__section-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
  text-align: left;
  color: var(--blog-text);
}

.about-card__projects {
  margin-top: 24px;
  padding: 16px;
  border-radius: 12px;
  background: var(--blog-primary-light);
  text-align: left;
}

.about-card__project-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.about-card__project-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px dashed var(--blog-border);
}

.about-card__project-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.about-card__project-item:first-child {
  padding-top: 0;
}

.about-card__project-label {
  flex-shrink: 0;
  min-width: 88px;
  font-size: 13px;
  color: var(--blog-text-secondary);
}

.about-card__project-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--blog-text);
}

.about-card__project-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: var(--blog-primary);
  text-decoration: none;
  transition: opacity 0.2s ease;
}

.about-card__project-link:hover {
  opacity: 0.8;
}

.about-card__github-icon {
  width: 16px;
  height: 16px;
}

.about-card__skills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  margin-top: 24px;
}
</style>
