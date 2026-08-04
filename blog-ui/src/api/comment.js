import request from '@/utils/request'

export function fetchComments(params) {
  return request({
    url: '/blog/comment/list',
    method: 'get',
    params,
  })
}

export function fetchCommentReplies(parentId, params) {
  return request({
    url: `/blog/comment/${parentId}/replies`,
    method: 'get',
    params,
  })
}

export function postComment(data) {
  return request({
    url: '/blog/comment',
    method: 'post',
    data,
  })
}
