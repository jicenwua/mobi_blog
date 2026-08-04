<script setup>
import { Link } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

defineProps({
  profile: {
    type: Object,
    default: () => ({
      nickname: 'Mobi',
      bio: '热爱技术，分享生活。专注于后端架构与前端工程化。',
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
        <button type="button" class="profile-card__copy" title="复制网站地址" @click="copySiteUrl">
          <el-icon :size="20"><Link /></el-icon>
        </button>
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
  margin-top: 16px;
}

.profile-card__copy {
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
  transition: color 0.2s ease, background 0.2s ease;
}

.profile-card__copy:hover {
  color: var(--blog-primary);
  background: var(--blog-primary-light);
}
</style>
