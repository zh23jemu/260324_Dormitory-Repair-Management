import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserByToken } from '@/api/user'

export const useStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref({})
  const loading = ref(false)

  // 设置token
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  // 移除token
  function removeToken() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
  }

  // 获取用户信息
  async function getUserInfo() {
    if (!token.value) return null
    
    loading.value = true
    try {
      const res = await getUserByToken()
      if (res.code === 200) {
        userInfo.value = res.data
      }
    } catch (error) {
      console.error('获取用户信息失败', error)
    } finally {
      loading.value = false
    }
    
    return userInfo.value
  }

  return {
    token,
    userInfo,
    loading,
    setToken,
    removeToken,
    getUserInfo
  }
})
