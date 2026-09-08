import request from '@/utils/request'

export function fetchBlacklist(params) {
  return request({
    url: '/blog/black/list',
    method: 'get',
    params,
  })
}

export function createBlacklist(data) {
  return request({
    url: '/blog/black',
    method: 'post',
    data,
  })
}

export function deleteBlacklist(id) {
  return request({
    url: `/blog/black/${id}`,
    method: 'delete',
  })
}
