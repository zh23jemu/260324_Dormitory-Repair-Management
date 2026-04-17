import axios from 'axios'
import { useStore } from '@/store/index'

// 创建 Axios 实例，并命名为 service
const service = axios.create({
  baseURL: "/api", // 基础 URL
  // baseURL: import.meta.env.VITE_API_URL, // 基础 URL
  timeout: 10000, // 超时时间（毫秒）
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token') // 假设 token 存储在 localStorage 中
    if (token) {
      config.headers.Authorization = `Bearer ${token}` // 设置 Authorization 请求头
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    // 后端返回的状态码为 200 时，表示成功
    if (response.status === 200) {
      return response.data // 返回数据部分
    }
    return Promise.reject(response)
  },
  (error) => {
    const code = error.response.status // 获取错误状态码
    if(code == 401){
      const store = useStore()
      store.removeToken() // 清除 token
      ElMessage({ message: "身份信息过期，请重新登录。", type: 'error'})
      return 
    }
    let { message } = error;
    if (message == "Network Error") {
      message = "后端接口连接异常";
    } else if (message.includes("timeout")) {
      message = "系统接口请求超时";
    } else if (message.includes("Request failed with status code")) {
      message = "系统接口" + message.substr(message.length - 3) + "异常";
    }
    ElMessage({ message: message, type: 'error', duration: 5 * 1000 })
    return Promise.reject(error)
  }
)

export default service
