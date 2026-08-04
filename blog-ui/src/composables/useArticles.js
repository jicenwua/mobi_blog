import { ref } from 'vue'
import { fetchArticles } from '@/api/article'

export function useArticles(defaultParams = {}) {
  const articles = ref([])
  const loading = ref(false)
  const total = ref(0)
  const pageNum = ref(1)
  const pageSize = ref(10)

  async function loadArticles(extraParams = {}) {
    loading.value = true
    try {
      const data = await fetchArticles({
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        ...defaultParams,
        ...extraParams,
      })
      articles.value = data?.content || []
      total.value = data?.totalElements || 0
    } catch {
      articles.value = getFallbackArticles()
      total.value = articles.value.length
    } finally {
      loading.value = false
    }
  }

  return {
    articles,
    loading,
    total,
    pageNum,
    pageSize,
    loadArticles,
  }
}

function getFallbackArticles() {
  return [
    {
      id: '1',
      title: 'Vue 3 组合式 API 最佳实践',
      summary: '深入探讨 Vue 3 Composition API 在企业级项目中的架构设计与代码组织方式。',
      category: 'spring',
      tags: ['Vue', '前端'],
      viewCount: 1280,
      commentCount: 36,
      favoriteCount: 89,
      createTime: '2024-05-17 10:30:00',
    },
    {
      id: '2',
      title: 'Spring Boot 微服务架构实战',
      summary: '从单体到微服务的演进之路，涵盖服务拆分、网关设计与分布式事务处理。',
      category: 'spring',
      tags: ['Spring', '微服务'],
      viewCount: 956,
      commentCount: 24,
      favoriteCount: 67,
      createTime: '2024-05-15 14:20:00',
    },
    {
      id: '3',
      title: 'AI 辅助编程：提升开发效率的新范式',
      summary: '探索 AI 工具如何改变软件开发流程，以及如何在团队中有效落地。',
      category: 'ai',
      tags: ['AI', '效率'],
      viewCount: 2100,
      commentCount: 58,
      favoriteCount: 142,
      createTime: '2024-05-12 09:00:00',
    },
  ]
}
