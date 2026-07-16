import axios, { type AxiosRequestConfig } from 'axios'

const http = axios.create({
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

// 请求拦截器: 自动注入 JWT Token
http.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  // 店铺模式
  const shopToken = localStorage.getItem('shopToken')
  if (shopToken) {
    config.headers.shop = `Bearer ${shopToken}`
  }

  // 客服模式
  const agentToken = localStorage.getItem('agentToken')
  if (agentToken) {
    config.headers.agent = `Bearer ${agentToken}`
  }

  return config
})

// 响应拦截器: 统一错误处理
http.interceptors.response.use(
  response => {
    const data = response.data
    if (data && data.code !== undefined) {
      if (data.code !== 200) {
        // 认证失败
        if (data.code === 401 || data.code === 403) {
          localStorage.removeItem('token')
          localStorage.removeItem('userId')
          localStorage.removeItem('nickname')
          localStorage.removeItem('avatarUrl')
        }
        return Promise.reject(new Error(data.msg || '请求失败'))
      }
      return data.data
    }
    return data
  },
  error => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('nickname')
      localStorage.removeItem('avatarUrl')
      window.location.reload()
    }
    return Promise.reject(error)
  },
)

// 请求辅助函数

export async function apiGet<T>(url: string, params?: any, config?: AxiosRequestConfig): Promise<T> {
  const res = await http.get(url, { params, ...config })
  return res as T
}

/** GET 请求带 body (兼容后端 @RequestBody on GET) */
export async function apiGetBody<T>(url: string, data?: any): Promise<T> {
  const res = await http.get(url, { data })
  return res as T
}

export async function apiPost<T = void>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  const res = await http.post(url, data, config)
  return res as T
}

export async function apiPut<T = void>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  const res = await http.put(url, data, config)
  return res as T
}

export async function apiDelete<T = void>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  const res = await http.delete(url, { data, ...config })
  return res as T
}

export default http
