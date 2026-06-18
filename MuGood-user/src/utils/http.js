import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userStore'
import router from '@/router'
// 创建axios实例
const http = axios.create({
  baseURL: 'http://118.178.237.0:8080',
  timeout: 8000
})

// axios请求拦截器
http.interceptors.request.use(config => {
  // 1. 从pinia获取token数据
  const userStore = useUserStore()
  // 2. 按照后端的要求，给服务器携带token
  const token = userStore.userInfo.token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, e => Promise.reject(e))

// axios响应式拦截器
http.interceptors.response.use(res => res.data, e => {
  const userStore = useUserStore()
  // 统一错误提示
  ElMessage({
    type: 'error',
    message: e.response?.data?.message || e.message || '请求失败',
    
  })
  // 401 token失效处理
  // 1. 清除本地用户数据
  // 2. 跳转到登录页
  if (e.response?.status === 401) {
    userStore.clearUserInfo()
    // 跳转到登录页
    router.push({
      path: '/login',
      query: {
        redirect: router.currentRoute.value.fullPath
      }
    })
  }
  return Promise.reject(e)
})


export default http
