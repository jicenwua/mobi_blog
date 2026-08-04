import request from '@/utils/request'

export function fetchAdminStats() {
  return request({ url: '/blog/admin/stats', method: 'get' })
}

export function fetchAdminArticles(params) {
  return request({ url: '/blog/admin/articles', method: 'get', params })
}

export function fetchAdminComments(params) {
  return request({ url: '/blog/admin/comments', method: 'get', params })
}

export function fetchAdminUsers(params) {
  return request({ url: '/blog/admin/users', method: 'get', params })
}

export function updateUserRole(userId, role) {
  return request({
    url: `/blog/admin/users/${userId}/role`,
    method: 'put',
    data: { role },
  })
}

export function updateUserStatus(userId, status) {
  return request({
    url: `/blog/admin/users/${userId}/status`,
    method: 'put',
    data: { status },
  })
}

export function fetchAdminLogs(params) {
  return request({ url: '/blog/admin/logs', method: 'get', params })
}

export function deleteArticle(articleId) {
  return request({ url: `/blog/article/${articleId}`, method: 'delete' })
}

export function deleteComment(commentId) {
  return request({ url: `/blog/comment/${commentId}`, method: 'delete' })
}

export function createArticle(data) {
  return request({ url: '/blog/article', method: 'post', data })
}
