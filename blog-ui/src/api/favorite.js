import request from '@/utils/request'

export function fetchFavoriteStatus(articleId) {
  return request({
    url: `/blog/favorite/article/${articleId}/status`,
    method: 'get',
  })
}

export function toggleFavorite(articleId) {
  return request({
    url: `/blog/favorite/article/${articleId}/toggle`,
    method: 'post',
  })
}

export function fetchFavoriteFolders(params = {}) {
  return request({
    url: '/blog/favorite',
    method: 'get',
    params: {
      pageNum: 1,
      pageSize: 100,
      ...params,
    },
  })
}

export function createFavoriteFolder(favoriteName) {
  return request({
    url: '/blog/favorite',
    method: 'post',
    data: { favoriteName },
  })
}

export function addArticleToFolder(favoriteId, articleId) {
  return request({
    url: `/blog/favorite/${favoriteId}/article/${articleId}`,
    method: 'post',
  })
}

export function removeArticleFromFolder(favoriteId, articleId) {
  return request({
    url: `/blog/favorite/${favoriteId}/article/${articleId}`,
    method: 'delete',
  })
}

export function fetchFolderArticles(favoriteId, params = {}) {
  return request({
    url: `/blog/favorite/${favoriteId}/article`,
    method: 'get',
    params: {
      pageNum: 1,
      pageSize: 10,
      ...params,
    },
  })
}

export function deleteFavoriteFolder(favoriteId) {
  return request({
    url: `/blog/favorite/${favoriteId}`,
    method: 'delete',
  })
}
