<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import SectionTitle from '@/components/common/SectionTitle.vue'
import { fetchHotTags } from '@/api/article'

const router = useRouter()
const tags = ref([])
const loading = ref(false)

async function loadHotTags() {
  loading.value = true
  try {
    tags.value = await fetchHotTags(10) || []
  } catch {
    tags.value = []
  } finally {
    loading.value = false
  }
}

function goTag(tagName) {
  router.push({ name: 'TagArticles', params: { tag: tagName } })
}

onMounted(loadHotTags)
</script>

<template>
  <el-card class="sidebar-card" shadow="never">
    <SectionTitle title="标签云" />
    <div v-loading="loading" class="tag-cloud">
      <el-tag
        v-for="tag in tags"
        :key="tag.name"
        class="tag-cloud__item"
        effect="plain"
        round
        @click="goTag(tag.name)"
      >
        {{ tag.name }}
        <span class="tag-cloud__count">{{ tag.viewCount ?? tag.articleCount ?? 0 }}</span>
      </el-tag>
      <span v-if="!loading && !tags.length" class="tag-cloud__empty">暂无标签</span>
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

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 32px;
}

.tag-cloud__item {
  cursor: pointer;
}

.tag-cloud__count {
  margin-left: 4px;
  opacity: 0.6;
  font-size: 12px;
}

.tag-cloud__empty {
  font-size: 13px;
  color: var(--blog-text-secondary);
}
</style>
