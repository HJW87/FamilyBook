import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// ===== 请求拦截器：自动附加 JWT Token =====
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ===== 响应拦截器 =====
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        // Token 过期或无效 → 清除登录状态 → 跳转登录页
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('familyId')
        localStorage.removeItem('userId')
        localStorage.removeItem('role')
        localStorage.removeItem('displayId')
        localStorage.removeItem('memberId')
        ElMessage.error('登录已过期，请重新登录')
        // 跳转登录页（避免重复跳转）
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
      } else if (status === 400) {
        ElMessage.error(error.response.data?.message || '请求参数错误')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status === 500) {
        ElMessage.error('服务器内部错误')
      } else {
        ElMessage.error('网络异常，请稍后重试')
      }
    } else {
      ElMessage.error('网络连接失败，请检查后端是否启动')
    }
    return Promise.reject(error)
  }
)

export default request
