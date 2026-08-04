<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import SectionTitle from '@/components/common/SectionTitle.vue'
import { fetchCommentReplies, fetchComments, postComment } from '@/api/comment'
import { useUserStore } from '@/store/user'
import { formatDateTime } from '@/utils/format'

const props = defineProps({
  articleId: {
    type: String,
    required: true,
  },
})

const emit = defineEmits(['comment-added'])

const router = useRouter()
const userStore = useUserStore()

const comments = ref([])
const loading = ref(false)
const submitting = ref(false)
const sort = ref('TIME')
const pageNum = ref(1)
const total = ref(0)
const pageSize = 10

const content = ref('')
const replyTarget = ref(null)
const expandedReplies = ref({})

const isLoggedIn = computed(() => userStore.isLoggedIn)
const currentUser = computed(() => userStore.userInfo)
const placeholder = computed(() => {
  if (replyTarget.value) {
    return `回复 @${replyTarget.value.userNickName}：`
  }
  return '发一条友善的评论'
})

function getVisibleReplies(comment) {
  return expandedReplies.value[comment.id] || comment.childComments || []
}

function shouldShowReplyMore(comment) {
  return comment.hasMoreChildren && !expandedReplies.value[comment.id]
}

function hasReplyThread(comment) {
  return (comment.childCount ?? 0) > 0 || getVisibleReplies(comment).length > 0
}

async function loadComments() {
  if (!props.articleId) return
  loading.value = true
  try {
    const page = await fetchComments({
      articleId: props.articleId,
      sort: sort.value,
      pageNum: pageNum.value,
      pageSize,
    })
    comments.value = page.content || []
    total.value = page.totalElements || 0
  } catch {
    comments.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  const text = content.value.trim()
  if (!text) {
    ElMessage.warning('请输入评论内容')
    return
  }
  if (!isLoggedIn.value) {
    router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } })
    return
  }

  submitting.value = true
  try {
    await postComment({
      articleId: props.articleId,
      content: text,
      parentId: replyTarget.value?.id || undefined,
    })
    ElMessage.success(replyTarget.value ? '回复成功' : '评论成功')
    content.value = ''
    replyTarget.value = null
    pageNum.value = 1
    expandedReplies.value = {}
    await loadComments()
    emit('comment-added')
  } finally {
    submitting.value = false
  }
}

function handleReply(comment) {
  if (!isLoggedIn.value) {
    router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } })
    return
  }
  replyTarget.value = comment
  content.value = ''
}

function cancelReply() {
  replyTarget.value = null
}

async function expandReplies(comment) {
  if (expandedReplies.value[comment.id]) return
  try {
    const page = await fetchCommentReplies(comment.id, { pageNum: 1, pageSize: 50 })
    expandedReplies.value = {
      ...expandedReplies.value,
      [comment.id]: page.content || [],
    }
  } catch {
    ElMessage.error('加载回复失败')
  }
}

function handleSortChange(value) {
  sort.value = value
  pageNum.value = 1
  expandedReplies.value = {}
  loadComments()
}

function handlePageChange(page) {
  pageNum.value = page
  expandedReplies.value = {}
  loadComments()
}

onMounted(loadComments)

watch(() => props.articleId, () => {
  pageNum.value = 1
  replyTarget.value = null
  expandedReplies.value = {}
  loadComments()
})
</script>

<template>
  <section class="comment-section">
    <SectionTitle title="评论区" />

    <div class="comment-section__card">
      <div class="comment-editor">
        <el-avatar :size="48" class="comment-editor__avatar">
          {{ isLoggedIn ? (currentUser?.nickname?.charAt(0) || 'U') : '?' }}
        </el-avatar>
        <div class="comment-editor__body">
          <div v-if="replyTarget" class="comment-editor__reply-hint">
            正在回复 <strong>@{{ replyTarget.userNickName }}</strong>
            <button type="button" class="comment-editor__cancel" @click="cancelReply">取消</button>
          </div>
          <el-input
            v-model="content"
            type="textarea"
            :rows="3"
            :placeholder="placeholder"
            maxlength="1000"
            show-word-limit
            resize="none"
            class="comment-editor__input"
          />
          <div class="comment-editor__actions">
            <el-radio-group v-model="sort" size="small" @change="handleSortChange">
              <el-radio-button value="TIME">最新</el-radio-button>
              <el-radio-button value="HOT">最热</el-radio-button>
            </el-radio-group>
            <el-button
              type="primary"
              round
              :loading="submitting"
              @click="handleSubmit"
            >
              {{ isLoggedIn ? '发布' : '登录后评论' }}
            </el-button>
          </div>
        </div>
      </div>

      <div v-loading="loading" class="comment-list">
        <article v-for="comment in comments" :key="comment.id" class="comment-item">
          <el-avatar :size="48" class="comment-item__avatar">
            {{ comment.userNickName?.charAt(0) || 'U' }}
          </el-avatar>

          <div class="comment-item__main">
            <div class="comment-item__name">{{ comment.userNickName }}</div>
            <p class="comment-item__content">{{ comment.content }}</p>

            <div class="comment-item__footer">
              <span class="comment-item__time">{{ formatDateTime(comment.createTime) }}</span>
              <button type="button" class="comment-footer-btn">
                <svg class="comment-like-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path
                    d="M7 10v12M15 5.88 14 10h5.83a2 2 0 0 1 1.92 2.56l-2.33 8A2 2 0 0 1 17.5 22H4a2 2 0 0 1-2-2v-8a2 2 0 0 1 2-2h2.76a2 2 0 0 0 1.79-1.11L12 2a3.13 3.13 0 0 1 3 3.88Z"
                    stroke="currentColor"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
                <span>{{ comment.likeCount ?? 0 }}</span>
              </button>
              <button type="button" class="comment-footer-btn" @click="handleReply(comment)">
                回复
              </button>
            </div>

            <div v-if="hasReplyThread(comment)" class="reply-thread">
              <div
                v-for="child in getVisibleReplies(comment)"
                :key="child.id"
                class="reply-item"
              >
                <el-avatar :size="24" class="reply-item__avatar">
                  {{ child.userNickName?.charAt(0) || 'U' }}
                </el-avatar>
                <div class="reply-item__main">
                  <div class="reply-item__line">
                    <span class="reply-item__name">{{ child.userNickName }}</span><span class="reply-item__colon">：</span><span class="reply-item__text">{{ child.content }}</span>
                  </div>
                  <div class="reply-item__footer">
                    <span class="reply-item__time">{{ formatDateTime(child.createTime) }}</span>
                    <button type="button" class="comment-footer-btn comment-footer-btn--sm">
                      <svg class="comment-like-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <path
                          d="M7 10v12M15 5.88 14 10h5.83a2 2 0 0 1 1.92 2.56l-2.33 8A2 2 0 0 1 17.5 22H4a2 2 0 0 1-2-2v-8a2 2 0 0 1 2-2h2.76a2 2 0 0 0 1.79-1.11L12 2a3.13 3.13 0 0 1 3 3.88Z"
                          stroke="currentColor"
                          stroke-width="1.8"
                          stroke-linecap="round"
                          stroke-linejoin="round"
                        />
                      </svg>
                      <span>{{ child.likeCount ?? 0 }}</span>
                    </button>
                  </div>
                </div>
              </div>

              <button
                v-if="shouldShowReplyMore(comment)"
                type="button"
                class="reply-thread__more"
                @click="expandReplies(comment)"
              >
                共{{ comment.childCount }}条回复，点击查看
              </button>
            </div>
          </div>
        </article>

        <el-empty v-if="!loading && !comments.length" description="暂无评论，来抢沙发吧" />

        <div v-if="total > pageSize" class="comment-list__pagination">
          <el-pagination
            v-model:current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            background
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.comment-section {
  margin-top: 24px;
}

.comment-section__card {
  background: var(--blog-card-bg);
  border-radius: 12px;
  box-shadow: var(--blog-shadow);
  padding: 24px;
}

.comment-editor {
  display: flex;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--blog-border);
  margin-bottom: 8px;
}

.comment-editor__avatar {
  flex-shrink: 0;
  background: var(--blog-primary-light);
  color: var(--blog-primary);
  font-weight: 600;
}

.comment-editor__body {
  flex: 1;
  min-width: 0;
}

.comment-editor__reply-hint {
  margin-bottom: 8px;
  font-size: 13px;
  color: #9499a0;
}

.comment-editor__reply-hint strong {
  color: #fb7299;
  font-weight: 500;
}

.comment-editor__cancel {
  margin-left: 8px;
  border: none;
  background: none;
  padding: 0;
  font-size: 13px;
  color: #00aeec;
  cursor: pointer;
}

.comment-editor__input :deep(.el-textarea__inner) {
  border-radius: 8px;
  box-shadow: none;
  background: #f4f4f4;
  border: 1px solid transparent;
  transition: border-color 0.2s, background 0.2s;
}

.comment-editor__input :deep(.el-textarea__inner:focus) {
  background: #fff;
  border-color: #00aeec;
}

.comment-editor__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  gap: 12px;
  flex-wrap: wrap;
}

.comment-list {
  min-height: 80px;
}

.comment-item {
  display: flex;
  gap: 16px;
  padding: 20px 0;
  border-bottom: 1px solid #f1f2f3;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-item__avatar {
  flex-shrink: 0;
  background: var(--blog-primary-light);
  color: var(--blog-primary);
  font-weight: 600;
}

.comment-item__main {
  flex: 1;
  min-width: 0;
}

.comment-item__name {
  font-size: 13px;
  font-weight: 500;
  color: #fb7299;
  line-height: 1.4;
  margin-bottom: 6px;
}

.comment-item__content {
  margin: 0 0 8px;
  font-size: 15px;
  line-height: 1.6;
  color: #18191c;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-item__footer,
.reply-item__footer {
  display: flex;
  align-items: center;
  gap: 16px;
}

.comment-item__time,
.reply-item__time {
  font-size: 12px;
  color: #9499a0;
}

.comment-footer-btn {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  border: none;
  background: none;
  padding: 0;
  font-size: 12px;
  color: #9499a0;
  cursor: pointer;
  transition: color 0.2s;
}

.comment-footer-btn--sm {
  font-size: 12px;
}

.comment-like-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

.comment-footer-btn:hover {
  color: #00aeec;
}

.reply-thread {
  margin-top: 12px;
  padding: 12px 14px;
  border-radius: 8px;
  background: #f4f4f4;
}

.reply-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
}

.reply-item:first-child {
  padding-top: 0;
}

.reply-item:last-child {
  padding-bottom: 0;
}

.reply-item + .reply-item {
  border-top: 1px solid #ebebeb;
}

.reply-item__avatar {
  flex-shrink: 0;
  margin-top: 2px;
  background: rgba(0, 174, 236, 0.12);
  color: #00aeec;
  font-size: 11px;
  font-weight: 600;
}

.reply-item__main {
  flex: 1;
  min-width: 0;
}

.reply-item__line {
  margin: 0 0 6px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}

.reply-item__name,
.reply-item__colon,
.reply-item__text {
  display: inline;
}

.reply-item__name {
  color: #fb7299;
  font-weight: 500;
}

.reply-item__colon {
  color: #18191c;
}

.reply-item__text {
  color: #18191c;
  white-space: pre-wrap;
}

.reply-thread__more {
  display: block;
  width: 100%;
  margin-top: 8px;
  padding: 6px 0 0;
  border: none;
  background: none;
  text-align: left;
  font-size: 13px;
  color: #9499a0;
  cursor: pointer;
  transition: color 0.2s;
}

.reply-thread__more:hover {
  color: #00aeec;
}

.comment-list__pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.comment-list__pagination :deep(.el-pagination.is-background .el-pager li.is-active) {
  background-color: var(--blog-primary);
}

@media (max-width: 768px) {
  .comment-section__card {
    padding: 16px;
  }

  .comment-editor {
    gap: 12px;
  }

  .comment-item {
    gap: 12px;
  }

  .comment-item__footer,
  .reply-item__footer {
    flex-wrap: wrap;
    gap: 12px;
  }
}
</style>
