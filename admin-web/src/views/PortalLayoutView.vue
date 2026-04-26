<template>
  <div class="site-shell">
    <header class="site-header">
      <div class="site-header__inner">
        <div class="site-brand" @click="router.push('/home')">
          <span class="site-brand__mark">
            <img src="../assets/portal/portal-logo.png" alt="高校宿舍报修管理系统标识" />
          </span>
          <div>
            <strong>高校宿舍报修管理系统</strong>
            <span>统一门户 / 公告 / 报修 / 论坛 / 维修服务</span>
          </div>
        </div>

        <nav class="site-nav site-nav--desktop">
          <button
            v-for="item in navItems"
            :key="item.to"
            type="button"
            class="site-nav__link"
            :class="{ active: isActive(item.to) }"
            @click="handleNavigate(item)"
          >
            {{ item.label }}
          </button>
        </nav>

        <div class="site-header__actions">
          <template v-if="auth.hasToken()">
            <span class="site-user-chip">{{ auth.state.userInfo.username || auth.state.userInfo.realName || '已登录用户' }}</span>
            <el-button v-if="auth.hasRole('student')" size="small" @click="router.push('/student/profile')">个人中心</el-button>
            <el-button v-else size="small" type="primary" @click="router.push(auth.getRoleHomePath())">进入工作台</el-button>
            <el-button size="small" plain @click="logout">退出</el-button>
          </template>
          <template v-else>
            <el-button size="small" type="primary" @click="router.push('/login')">登录</el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="site-main">
      <router-view />
    </main>

    <nav class="site-mobile-nav">
      <button
        v-for="item in navItems"
        :key="item.to"
        type="button"
        class="site-mobile-nav__item"
        :class="{ active: isActive(item.to) }"
        @click="handleNavigate(item)"
      >
        <span>{{ item.label }}</span>
      </button>
    </nav>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuth } from '../utils/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuth()

const navItems = [
  { label: '首页', to: '/home', requiresAuth: false },
  { label: '我要报修', to: '/student/repair/create', requiresAuth: true, roles: ['student'] },
  { label: '我的工单', to: '/student/orders', requiresAuth: true, roles: ['student'] },
  { label: '论坛交流', to: '/forum', requiresAuth: false },
  { label: '公告信息', to: '/announcements', requiresAuth: false },
  { label: '维修员信息', to: '/repairers', requiresAuth: false }
]

function isActive(path) {
  return route.path === path || route.path.startsWith(`${path}/`)
}

function handleNavigate(item) {
  if (item.requiresAuth && !auth.hasToken()) {
    ElMessage.warning('请先登录后再使用该功能')
    router.push(`/login?redirect=${encodeURIComponent(item.to)}&role=student`)
    return
  }
  if (item.roles && !auth.hasAnyRole(item.roles)) {
    ElMessage.warning('当前账号无权访问该功能')
    router.push(auth.getRoleHomePath())
    return
  }
  router.push(item.to)
}

function logout() {
  auth.logout()
  router.replace('/home')
}
</script>
