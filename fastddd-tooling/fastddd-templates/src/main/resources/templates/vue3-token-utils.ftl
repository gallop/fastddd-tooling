const TOKEN_KEY = 'token'

/**
 * ${msg.vue.token.getDesc}
 */
export const getToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * ${msg.vue.token.setDesc}
 */
export const setToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * ${msg.vue.token.removeDesc}
 */
export const removeToken = (): void => {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * ${msg.vue.token.hasDesc}
 */
export const hasToken = (): boolean => {
  return !!getToken()
}
