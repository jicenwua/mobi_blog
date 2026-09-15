<script setup>
import { Link } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

defineProps({
  profile: {
    type: Object,
    default: () => ({
      nickname: '岑兹',
      bio: '热爱技术，分享生活。专注于后端架构与前端工程化。',
      github: 'https://github.com/jicenwua',
      avatar: '',
      categoryCount: 4,
      articleCount: 0,
      tagCount: 0,
    }),
  },
})

async function copySiteUrl() {
  const url = window.location.origin
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success('网站地址已复制到剪贴板')
  } catch {
    const input = document.createElement('input')
    input.value = url
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    ElMessage.success('网站地址已复制到剪贴板')
  }
}
</script>

<template>
  <el-card class="profile-card" shadow="never">
    <div class="profile-card__banner" />
    <div class="profile-card__body">
      <el-avatar :size="72" class="profile-card__avatar">
        {{ profile.nickname?.charAt(0) }}
      </el-avatar>
      <h3 class="profile-card__name">{{ profile.nickname }}</h3>
      <p class="profile-card__bio">{{ profile.bio }}</p>

      <div class="profile-card__stats">
        <div class="profile-card__stat">
          <span class="profile-card__stat-num">{{ profile.categoryCount }}</span>
          <span class="profile-card__stat-label">分类</span>
        </div>
        <div class="profile-card__stat">
          <span class="profile-card__stat-num">{{ profile.articleCount }}</span>
          <span class="profile-card__stat-label">文章</span>
        </div>
        <div class="profile-card__stat">
          <span class="profile-card__stat-num">{{ profile.tagCount }}</span>
          <span class="profile-card__stat-label">标签</span>
        </div>
      </div>

      <div class="profile-card__action">
        <button type="button" class="profile-card__icon-btn" title="复制网站地址" @click="copySiteUrl">
          <el-icon :size="20"><Link /></el-icon>
        </button>
        <a
          v-if="profile.github"
          :href="profile.github"
          target="_blank"
          rel="noopener noreferrer"
          class="profile-card__icon-btn"
          title="GitHub"
        >
          <svg class="profile-card__github-icon" viewBox="0 0 19 19" aria-hidden="true">
            <path
              fill="currentColor"
              fill-rule="evenodd"
              d="M9.356 1.85C5.05 1.85 1.57 5.356 1.57 9.694a7.84 7.84 0 0 0 5.324 7.44c.387.079.528-.168.528-.376 0-.182-.013-.805-.013-1.454-2.165.467-2.616-.935-2.616-.935-.349-.91-.864-1.143-.864-1.143-.71-.48.051-.48.051-.48.787.051 1.2.805 1.2.805.695 1.194 1.817.857 2.268.649.064-.507.27-.857.49-1.052-1.728-.182-3.545-.857-3.545-3.87 0-.857.31-1.558.8-2.104-.078-.195-.349-1 .077-2.078 0 0 .657-.208 2.14.805a7.5 7.5 0 0 1 1.946-.26c.657 0 1.328.092 1.946.26 1.483-1.013 2.14-.805 2.14-.805.426 1.078.155 1.883.078 2.078.502.546.799 1.247.799 2.104 0 3.013-1.818 3.675-3.558 3.87.284.247.528.714.528 1.454 0 1.052-.012 1.896-.012 2.156 0 .208.142.455.528.377a7.84 7.84 0 0 0 5.324-7.441c.013-4.338-3.48-7.844-7.773-7.844"
              clip-rule="evenodd"
            />
          </svg>
        </a>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.profile-card {
  border: none;
  border-radius: var(--blog-radius);
  overflow: hidden;
  margin-bottom: 16px;
}

.profile-card :deep(.el-card__body) {
  padding: 0;
}

.profile-card__banner {
  height: 80px;
  background: var(--blog-primary-gradient);
}

.profile-card__body {
  padding: 0 20px 20px;
  text-align: center;
  margin-top: -36px;
}

.profile-card__avatar {
  border: 3px solid #fff;
  background: var(--blog-primary);
  color: #fff;
  font-size: 28px;
  font-weight: 600;
}

.profile-card__name {
  margin: 12px 0 6px;
  font-size: 18px;
}

.profile-card__bio {
  margin: 0 0 16px;
  font-size: 13px;
  color: var(--blog-text-secondary);
  line-height: 1.6;
}

.profile-card__stats {
  display: flex;
  justify-content: space-around;
  padding: 12px 0;
  border-top: 1px solid var(--blog-border);
  border-bottom: 1px solid var(--blog-border);
}

.profile-card__stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.profile-card__stat-num {
  font-size: 18px;
  font-weight: 600;
  color: var(--blog-primary);
}

.profile-card__stat-label {
  font-size: 12px;
  color: var(--blog-text-secondary);
}

.profile-card__action {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 16px;
}

.profile-card__icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--blog-text-secondary);
  cursor: pointer;
  text-decoration: none;
  transition: color 0.2s ease, background 0.2s ease;
}

.profile-card__icon-btn:hover {
  color: var(--blog-primary);
  background: var(--blog-primary-light);
}

.profile-card__github-icon {
  width: 20px;
  height: 20px;
}
</style>
