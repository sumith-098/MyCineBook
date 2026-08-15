import axios from 'axios'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://api-gateway.lemondesert-03dc2ff4.eastasia.azurecontainerapps.io'

// Tokens live in memory + localStorage (localStorage so a refresh doesn't log the user out;
// in-memory copy avoids re-reading storage on every single request).
let accessToken = localStorage.getItem('cinebook_access_token') || null
let refreshToken = localStorage.getItem('cinebook_refresh_token') || null
let role = localStorage.getItem('cinebook_role') || null

export function setTokens({ accessToken: at, refreshToken: rt, role: r }) {
  accessToken = at
  refreshToken = rt ?? refreshToken
  role = r ?? role
  if (at) localStorage.setItem('cinebook_access_token', at)
  if (rt) localStorage.setItem('cinebook_refresh_token', rt)
  if (r) localStorage.setItem('cinebook_role', r)
}

export function clearTokens() {
  accessToken = null
  refreshToken = null
  role = null
  localStorage.removeItem('cinebook_access_token')
  localStorage.removeItem('cinebook_refresh_token')
  localStorage.removeItem('cinebook_role')
}

export function getAccessToken() {
  return accessToken
}

export function getRole() {
  return role
}

const api = axios.create({ baseURL: BASE_URL })

api.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

// Auto-refresh once on a 401, then retry the original request. If the refresh itself fails,
// clear tokens and let the caller's own error handling (redirect to /login) take over — this
// interceptor never redirects itself, since it has no idea which page should own that decision.
let refreshInFlight = null

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config
    if (error.response?.status === 401 && refreshToken && !original._retried) {
      original._retried = true
      try {
        refreshInFlight = refreshInFlight ?? axios.post(`${BASE_URL}/api/auth/refresh`, { refreshToken })
        const { data } = await refreshInFlight
        refreshInFlight = null
        setTokens({ accessToken: data.data.accessToken })
        original.headers.Authorization = `Bearer ${data.data.accessToken}`
        return api(original)
      } catch (refreshError) {
        refreshInFlight = null
        clearTokens()
        return Promise.reject(error)
      }
    }
    return Promise.reject(error)
  },
)

/** Unwraps the {success,message,data} envelope every backend service uses, and normalizes
 *  errors to a plain message string so components don't need to know the envelope shape. */
export async function call(promise) {
  try {
    const { data } = await promise
    return data.data
  } catch (error) {
    const message = error.response?.data?.message || error.message || 'Something went wrong.'
    throw new Error(message)
  }
}

export default api
