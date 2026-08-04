import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuth, getToken } from '@/utils/auth'

const service = axios.create({
  baseURL: `${import.meta.env.VITE_APP_BASE_API}/blog`,
  timeout: 15000,
})

function resolveErrorMessage(error) {
  const data = error.response?.data

  if (data != null && data !== '') {
    if (typeof data === 'string') {
      try {
        const parsed = JSON.parse(data)
        return parsed.msg || parsed.message || data
      } catch {
        return data
      }
    }

    if (typeof data === 'object') {
      if (data.msg) return data.msg
      if (data.message) return data.message
    }
  }

  const axiosMessage = error.message || ''
  if (axiosMessage && !/^Request failed with status code \d+$/.test(axiosMessage)) {
    return axiosMessage
  }

  const status = error.response?.status
  if (status) {
    const statusMessages = {
      400: '请求参数错误',
      403: '没有访问权限',
      404: '请求地址不存在',
      500: '服务器内部错误',
      502: '网关错误',
      503: '服务不可用',
      504: '网关超时',
    }
    return statusMessages[status] || `请求失败 (${status})`
  }

  return '网络异常'
}

service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== undefined && res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res.data !== undefined ? res.data : res
  },
  (error) => {
    const status = error.response?.status
    const message = resolveErrorMessage(error)

    if (status === 401) {
      clearAuth()
      ElMessage.warning(message || '登录已过期，请重新登录')
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

export default service
