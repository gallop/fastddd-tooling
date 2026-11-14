import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import * as authApi from '../api/auth'
import type { UserInfo } from '../api/auth'
import { removeToken, setToken } from '../utils/token'

export const useUserStore = defineStore('user', () => {
  const router = useRouter()

  // ${msg.vue.userStore.stateComment}
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const permissions = ref<string[]>([])

  /**
   * ${msg.vue.userStore.loginDesc}
   */
  const login = async (username: string, password: string) => {
    try {
      const data = await authApi.login({ username, password })
      token.value = data.accessToken
      permissions.value = data.permissions
      userInfo.value = data.user
      setToken(data.accessToken)

      // ${msg.vue.userStore.storeRefreshTokenComment}
      if (data.refreshToken) {
        localStorage.setItem('refreshToken', data.refreshToken)
      }

      return data
    } catch (error) {
      console.error('${msg.vue.userStore.loginFailed}', error)
      throw error
    }
  }

  /**
   * ${msg.vue.userStore.logoutDesc}
   */
  const logout = async () => {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('${msg.vue.userStore.logoutApiFailed}', error)
    } finally {
      // ${msg.vue.userStore.alwaysCleanupComment}
      token.value = ''
      userInfo.value = null
      permissions.value = []
      removeToken()
      localStorage.removeItem('refreshToken')
      router.push('/login')
    }
  }

  /**
   * ${msg.vue.userStore.getUserInfoDesc}
   */
  const getUserInfo = async () => {
    try {
      const data = await authApi.getUserInfo()
      userInfo.value = data.user
      permissions.value = data.permissions
      return data
    } catch (error) {
      console.error('${msg.vue.userStore.getUserInfoFailed}', error)
      throw error
    }
  }

  /**
   * ${msg.vue.userStore.refreshTokenDesc}
   */
  const refreshTokenAction = async () => {
    const refreshToken = localStorage.getItem('refreshToken')
    if (!refreshToken) {
      throw new Error('${msg.vue.userStore.noRefreshToken}')
    }

    try {
      const data = await authApi.refreshToken(refreshToken)
      token.value = data.accessToken
      setToken(data.accessToken)
      return data
    } catch (error) {
      console.error('${msg.vue.userStore.refreshTokenFailed}', error)
      // ${msg.vue.userStore.refreshFailedCleanupComment}
      await logout()
      throw error
    }
  }

  /**
   * ${msg.vue.userStore.hasPermissionDesc}
   */
  const hasPermission = (permission: string): boolean => {
    return permissions.value.includes(permission) ||
           permissions.value.includes('/**') ||
           permissions.value.includes('*:*')
  }

  /**
   * ${msg.vue.userStore.isLoggedInDesc}
   */
  const isLoggedIn = (): boolean => {
    return !!token.value
  }

  return {
    token,
    userInfo,
    permissions,
    login,
    logout,
    getUserInfo,
    refreshTokenAction,
    hasPermission,
    isLoggedIn
  }
})
