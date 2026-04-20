<template>
  <div class="admin-shell">
    <aside class="admin-side">
      <div class="brand-box">
        <div class="brand-title">宿舍报修管理</div>
        <div class="brand-subtitle">{{ auth.state.userInfo.realName }} / {{ roleText }}</div>
      </div>
      <nav class="side-nav">
        <router-link v-for="item in menus" :key="item.path" :to="item.path" class="nav-item" active-class="is-active">
          <span>{{ item.label }}</span>
          <small>{{ item.desc }}</small>
        </router-link>
      </nav>
    </aside>

    <div class="admin-main">
      <header class="admin-header">
        <div>
          <div class="page-title">{{ currentTitle }}</div>
          <div class="page-subtitle">围绕宿舍报修全流程、宿舍设施、统计分析和服务反馈进行管理</div>
        </div>
        <div class="header-actions">
          <el-button @click="refreshCurrent">刷新当前页</el-button>
          <el-button type="danger" plain @click="handleLogout">退出</el-button>
        </div>
      </header>
      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminAuth } from '../utils/auth'

const router = useRouter()
const route = useRoute()
const auth = useAdminAuth()

const allMenus = [
  { path: '/dashboard', label: '统计中心', desc: '概览与图表', roles: ['admin'] },
  { path: '/orders', label: '工单管理', desc: '审核与分配', roles: ['admin', 'dorm_admin'] },
  { path: '/repair-work', label: '维修处理', desc: '工单执行', roles: ['repairer'] },
  { path: '/repairer/stats', label: '个人统计', desc: '维修员数据', roles: ['repairer'] },
  { path: '/repairer/list', label: '维修员信息', desc: '同事与评价', roles: ['repairer'] },
  { path: '/repairer/profile', label: '个人信息', desc: '头像与工种', roles: ['repairer'] },
  { path: '/dorm-rooms', label: '楼栋宿舍', desc: '楼栋房间管理', roles: ['admin', 'dorm_admin'] },
  { path: '/facilities', label: '设施台账', desc: '宿舍资产管理', roles: ['admin', 'dorm_admin'] },
  { path: '/students', label: '学生住宿', desc: '入住信息维护', roles: ['admin', 'dorm_admin'] },
  { path: '/announcements', label: '公告管理', desc: '发布系统公告', roles: ['admin', 'dorm_admin'] },
  { path: '/users', label: '用户管理', desc: '账号与角色', roles: ['admin'] },
  { path: '/resources', label: '报修知识', desc: '知识库维护', roles: ['admin'] },
  { path: '/forum-posts', label: '论坛管理', desc: '公开内容审核', roles: ['admin'] },
  { path: '/service-messages', label: '服务留言', desc: '用户反馈处理', roles: ['admin'] },
  { path: '/ratings', label: '评价管理', desc: '服务质量评价', roles: ['admin'] },
  { path: '/materials', label: '耗材管理', desc: '库存与扣减', roles: ['admin'] },
  { path: '/config', label: '基础配置', desc: '类型与字典', roles: ['admin'] },
  { path: '/logs', label: '系统日志', desc: '操作留痕', roles: ['admin'] }
]

const menus = computed(() => allMenus.filter((item) => item.roles.includes(auth.state.userInfo.role)))
const currentTitle = computed(() => menus.value.find((item) => item.path === route.path)?.label || '管理平台')
const roleText = computed(() => ({ admin: '系统管理员', dorm_admin: '宿舍管理员', repairer: '维修人员' }[auth.state.userInfo.role] || auth.state.userInfo.role || ''))

function handleLogout() {
  auth.logout()
  router.replace('/login')
}

function refreshCurrent() {
  router.replace({ path: '/refreshing' }).finally(() => router.replace(route.fullPath))
}
</script>
