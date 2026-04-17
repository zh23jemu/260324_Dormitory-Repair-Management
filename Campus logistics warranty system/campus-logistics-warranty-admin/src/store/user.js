import { defineStore } from 'pinia'
import { getUserByToken } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token'),
    userInfo: null,
    userRole: localStorage.getItem('userRole') || null // 添加用户角色
  }),
  
  getters: {
    // 判断是否是管理员
    isAdmin: (state) => state.userRole === 'admin',
    // 判断是否是维修工
    isWorker: (state) => state.userRole === 'worker'
  },
  
  actions: {
    // 设置token
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },
    
    // 清除token
    clearToken() {
      this.token = null
      localStorage.removeItem('token')
    },
    
    // 设置用户角色
    setUserRole(role) {
      this.userRole = role
      localStorage.setItem('userRole', role)
    },
    
    // 清除用户角色
    clearUserRole() {
      this.userRole = null
      localStorage.removeItem('userRole')
    },
    
    // 获取用户信息
    async getUserInfo() {
      if (!this.token) return null
      
      try {
        const res = await getUserByToken()
        this.userInfo = res.data
        // 同时设置用户角色
        if (res.data && res.data.role) {
          this.setUserRole(res.data.role)
        }
        return this.userInfo
      } catch (error) {
        console.error('获取用户信息失败', error)
        return null
      }
    },
    
    // 设置用户信息
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      // 同时设置用户角色
      if (userInfo && userInfo.role) {
        this.setUserRole(userInfo.role)
      }
    },
    
    // 清除用户信息
    clearUserInfo() {
      this.userInfo = null
    },
    
    // 登出
    logout() {
      this.clearToken()
      this.clearUserInfo()
      this.clearUserRole()
    }
  }
}) 