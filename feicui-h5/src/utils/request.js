import axios from 'axios'
import { showToast } from 'vant'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

// axios 实例: 统一 baseURL=/api(经 vite 代理到后端), 自动携带 JWT, 解包后端 Result.data
const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截: 注入 Authorization
service.interceptors.request.use(
  (config) => {
    const auth = useAuthStore()
    if (auth.token) {
      config.headers.Authorization = `Bearer ${auth.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截: 后端统一返回 {code,message,data}, 成功直接返回 data
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    // 401 未登录: 清登录态并跳登录页
    if (res.code === 401) {
      const auth = useAuthStore()
      auth.clear()
      showToast(res.message || '请先登录')
      router.push('/login')
      return Promise.reject(res)
    }
    showToast(res.message || '请求失败')
    return Promise.reject(res)
  },
  (error) => {
    showToast('网络异常，请稍后重试')
    return Promise.reject(error)
  }
)

export default service
