import { onBeforeUnmount, onMounted, ref } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { ElMessage } from 'element-plus'
import { deleteArticleDraft, fetchMyDraft, saveArticleDraft } from '@/api/article'
import { getToken } from '@/utils/auth'

const AUTO_SAVE_INTERVAL = 30_000
const DRAFT_API_URL = `${import.meta.env.VITE_APP_BASE_API}/blog/blog/article/draft`

export function useArticleDraft(form, parseTags) {
  const draftId = ref(null)
  const saving = ref(false)
  const lastSavedAt = ref(null)
  const draftLoaded = ref(false)
  const skipDraftSave = ref(false)

  let lastSnapshot = ''
  let autoSaveTimer = null

  function buildPayload() {
    return {
      title: form.title.trim(),
      summary: form.summary.trim(),
      content: form.content.trim(),
      category: form.category,
      tags: parseTags(form.tagsInput),
    }
  }

  function hasDraftContent(payload = buildPayload()) {
    return !!(
      payload.title ||
      payload.summary ||
      payload.content ||
      payload.category ||
      payload.tags?.length
    )
  }

  function serializePayload(payload) {
    return JSON.stringify(payload)
  }

  function isDirty(payload = buildPayload()) {
    return serializePayload(payload) !== lastSnapshot
  }

  async function loadDraft() {
    try {
      const draft = await fetchMyDraft()
      if (!draft) return

      draftId.value = draft.id
      form.title = draft.title || ''
      form.summary = draft.summary || ''
      form.content = draft.content || ''
      form.category = draft.category || ''
      form.tagsInput = (draft.tags || []).join(',')
      lastSnapshot = serializePayload(buildPayload())
      lastSavedAt.value = draft.updateTime || draft.createTime
      ElMessage.info('已恢复上次保存的草稿')
    } finally {
      draftLoaded.value = true
    }
  }

  async function saveDraft(options = { silent: true }) {
    if (skipDraftSave.value) return true

    const payload = buildPayload()
    if (!hasDraftContent(payload)) return true
    if (!isDirty(payload) && draftId.value) return true

    saving.value = true
    try {
      const draft = await saveArticleDraft(payload)
      draftId.value = draft.id
      lastSnapshot = serializePayload(payload)
      lastSavedAt.value = draft.updateTime || draft.createTime
      if (!options.silent) {
        ElMessage.success('草稿已保存')
      }
      return true
    } catch {
      if (!options.silent) {
        ElMessage.error('草稿保存失败')
      }
      return false
    } finally {
      saving.value = false
    }
  }

  function saveDraftKeepalive() {
    if (skipDraftSave.value) return

    const payload = buildPayload()
    if (!hasDraftContent(payload)) return
    if (!isDirty(payload) && draftId.value) return

    const token = getToken()
    if (!token) return

    fetch(DRAFT_API_URL, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(payload),
      keepalive: true,
    }).catch(() => {})
  }

  function markPublished() {
    skipDraftSave.value = true
    draftId.value = null
    lastSnapshot = ''
    lastSavedAt.value = null
  }

  async function clearDraft() {
    skipDraftSave.value = false
    await deleteArticleDraft().catch(() => {})
    draftId.value = null
    lastSnapshot = ''
    lastSavedAt.value = null
  }

  function handleVisibilityChange() {
    if (document.visibilityState === 'hidden') {
      saveDraftKeepalive()
    }
  }

  function handleBeforeUnload() {
    saveDraftKeepalive()
  }

  function startAutoSave() {
    autoSaveTimer = window.setInterval(() => {
      saveDraft({ silent: true })
    }, AUTO_SAVE_INTERVAL)
  }

  function stopAutoSave() {
    if (autoSaveTimer) {
      clearInterval(autoSaveTimer)
      autoSaveTimer = null
    }
  }

  onMounted(async () => {
    await loadDraft()
    startAutoSave()
    document.addEventListener('visibilitychange', handleVisibilityChange)
    window.addEventListener('beforeunload', handleBeforeUnload)
  })

  onBeforeUnmount(() => {
    stopAutoSave()
    document.removeEventListener('visibilitychange', handleVisibilityChange)
    window.removeEventListener('beforeunload', handleBeforeUnload)
  })

  onBeforeRouteLeave(async (_to, _from, next) => {
    if (skipDraftSave.value) {
      next()
      return
    }
    await saveDraft({ silent: true })
    next()
  })

  return {
    draftId,
    saving,
    lastSavedAt,
    draftLoaded,
    saveDraft,
    clearDraft,
    markPublished,
  }
}
