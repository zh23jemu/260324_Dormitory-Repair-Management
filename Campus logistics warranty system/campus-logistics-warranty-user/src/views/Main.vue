<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from '@/store'

const userStore = useStore()
const router = useRouter()
const route = useRoute()

// 菜单项
const menuItems = [
  { path: '/home', title: '主页', icon: 'House', requiresAuth: false },
  { path: '/announcement', title: '公告', icon: 'Bell', requiresAuth: false },
  { path: '/resource/list', title: '资源大厅', icon: 'Collection', requiresAuth: false },
  { path: '/message', title: '留言', icon: 'ChatDotSquare', requiresAuth: true },
  { path: '/forum', title: '论坛', icon: 'Comment', requiresAuth: true },
  { path: '/repair/list', title: '报修单', icon: 'Tools', requiresAuth: true },
  { path: '/worker/list', title: '维修工', icon: 'User', requiresAuth: true },
  { path: '/profile', title: '个人中心', icon: 'User', requiresAuth: true }
]

// 用户信息
const userInfo = ref({})
const isLoggedIn = computed(() => !!userStore.token)

// 获取用户信息
onMounted(async () => {
  if (userStore.token) {
    userInfo.value = await userStore.getUserInfo()
  }
})

// 退出登录
const logout = () => {
  userStore.removeToken()
  router.push('/login')
}

// 去登录页
const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <div class="app-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="logo">校园后勤报修系统</div>
      
      <div class="nav-menu">
        <router-link 
          v-for="item in menuItems" 
          :key="item.path" 
          :to="item.path"
          class="nav-item"
          v-show="!item.requiresAuth || isLoggedIn"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </router-link>
      </div>
      
      <div class="user-section">
        <!-- 已登录状态 -->
        <el-dropdown v-if="isLoggedIn" trigger="click">
          <span class="user-dropdown">
            <el-avatar :size="32" :src="userInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
            <span>{{ userInfo.name || userInfo.username || '用户' }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item @click="router.push('/repair/list')">我的报修</el-dropdown-item>
              <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <!-- 未登录状态 -->
        <div v-else class="login-btns">
          <el-button type="primary" size="small" @click="goToLogin">
            <el-icon><User /></el-icon> 登录
          </el-button>
        </div>
      </div>
    </header>
    
    <!-- 内容区域 -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}

.nav-menu {
  display: flex;
  gap: 25px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #606266;
  text-decoration: none;
}

.nav-item:hover {
  color: #409EFF;
}

.nav-item.router-link-active {
  color: #409EFF;
  font-weight: bold;
}

.user-section {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.login-btns {
  display: flex;
  gap: 10px;
}

.main-content {
  flex: 1;
  padding: 20px;
  background-color: #f5f7f9;
}
</style> 