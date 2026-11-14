import request from './request'

/**
 * ${msg.vue.authApi.loginRequestDesc}
 */
export interface LoginRequest {
  username: string
  password: string
  rememberMe?: boolean
}

/**
 * ${msg.vue.authApi.userInfoDesc}
 */
export interface UserInfo {
  id: number
  username: string
  realName: string
  email?: string
  phone?: string
  avatar?: string
  status: boolean
}

/**
 * ${msg.vue.authApi.loginResponseDesc}
 */
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: UserInfo
  permissions: string[]
}

/**
 * ${msg.vue.authApi.tokenResponseDesc}
 */
export interface TokenResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
}

/**
 * ${msg.vue.authApi.userInfoResponseDesc}
 */
export interface UserInfoResponse {
  user: UserInfo
  permissions: string[]
  roles: string[]
}

/**
 * ${msg.vue.authApi.loginDesc}
 */
export const login = (data: LoginRequest): Promise<LoginResponse> => {
  return request.post('/auth/login', data)
}

/**
 * ${msg.vue.authApi.logoutDesc}
 */
export const logout = (): Promise<void> => {
  return request.post('/auth/logout')
}

/**
 * ${msg.vue.authApi.refreshTokenDesc}
 */
export const refreshToken = (refreshToken: string): Promise<TokenResponse> => {
  return request.post('/auth/refresh', { refreshToken })
}

/**
 * ${msg.vue.authApi.getUserInfoDesc}
 */
export const getUserInfo = (): Promise<UserInfoResponse> => {
  return request.get('/auth/userinfo')
}

export default {
  login,
  logout,
  refreshToken,
  getUserInfo
}
