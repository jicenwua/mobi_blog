import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/blog/auth/login',
    method: 'post',
    data,
  })
}

export function register(data) {
  return request({
    url: '/blog/auth/register',
    method: 'post',
    data,
  })
}

export function sendVerificationCode(data) {
  return request({
    url: '/blog/auth/send-code',
    method: 'post',
    data,
  })
}

export function sendResetCode(data) {
  return request({
    url: '/blog/auth/send-reset-code',
    method: 'post',
    data,
  })
}

export function resetPassword(data) {
  return request({
    url: '/blog/auth/reset-password',
    method: 'post',
    data,
  })
}
