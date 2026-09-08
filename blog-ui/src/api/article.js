import request from '@/utils/request'

export function fetchArticles(params) {
  return request({
    url: '/blog/article/list',
    method: 'get',
    params,
  })
}

export function fetchHotTags(limit = 10) {
  return request({
    url: '/blog/article/tags/hot',
    method: 'get',
    params: { limit },
  })
}

export function fetchAllTags() {
  return request({
    url: '/blog/article/tags',
    method: 'get',
  })
}

export function fetchAllCategories() {
  return request({
    url: '/blog/article/categories',
    method: 'get',
  })
}

export function fetchArticleDetail(articleId) {
  return request({
    url: `/blog/article/detail/${articleId}`,
    method: 'get',
  })
}

export function fetchMyArticles(params) {
  return request({
    url: '/blog/article/mine',
    method: 'get',
    params,
  })
}

export function createArticle(data) {
  return request({
    url: '/blog/article',
    method: 'post',
    data,
  })
}

export function uploadArticleImages(files) {
  const formData = new FormData()
  files.forEach((file) => formData.append('images', file))
  return request({
    url: '/blog/article/image',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function deleteArticleImage(imagePath) {
  return request({
    url: '/blog/article/image',
    method: 'delete',
    params: { imagePath },
  })
}

export function deleteArticle(articleId) {
  return request({
    url: `/blog/article/${articleId}`,
    method: 'delete',
  })
}

export function fetchMyDraft() {
  return request({
    url: '/blog/article/draft',
    method: 'get',
  })
}

export function saveArticleDraft(data) {
  return request({
    url: '/blog/article/draft',
    method: 'put',
    data,
  })
}

export function deleteArticleDraft() {
  return request({
    url: '/blog/article/draft',
    method: 'delete',
  })
}
