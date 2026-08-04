import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { isAdminRole, isMasterRole } from '@/constants/roles'
import { clearAuth, getStoredUser, getToken, setStoredUser, setToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(getStoredUser())

  const isLoggedIn = computed(() => Boolean(token.value))
  const roles = computed(() => userInfo.value?.roles || [])
  const isAdmin = computed(() => isAdminRole(roles.value))
  const isMaster = computed(() => isMasterRole(roles.value))

  function setAuth(payload) {
    token.value = payload.token
    userInfo.value = {
      userId: payload.userId,
      nickname: payload.nickname,
      roles: normalizeRoles(payload.role),
    }
    setToken(payload.token)
    setStoredUser(userInfo.value)
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    clearAuth()
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    roles,
    isAdmin,
    isMaster,
    setAuth,
    logout,
  }
})

function normalizeRoles(role) {
  if (Array.isArray(role)) return role
  if (role && typeof role === 'object') return Object.keys(role)
  return []
}
