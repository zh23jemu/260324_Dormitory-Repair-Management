import { createRouter, createWebHistory } from 'vue-router'
import AdminLayoutView from '../views/AdminLayoutView.vue'
import LoginView from '../views/LoginView.vue'
import { useAdminAuth } from '../utils/auth'

const routes = [
  { path: '/login', name: 'login', component: LoginView },
  {
    path: '/',
    component: AdminLayoutView,
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'dashboard', component: () => import('../views/admin/AdminDashboardView.vue'), meta: { roles: ['admin'] } },
      { path: 'orders', name: 'orders', component: () => import('../views/dorm/OrderManageView.vue'), meta: { roles: ['admin', 'dorm_admin'] } },
      { path: 'repair-work', name: 'repair-work', component: () => import('../views/repairer/RepairWorkView.vue'), meta: { roles: ['repairer'] } },
      { path: 'repairer/stats', name: 'repairer-stats', component: () => import('../views/repairer/RepairerStatsView.vue'), meta: { roles: ['repairer'] } },
      { path: 'dorm-rooms', name: 'dorm-rooms', component: () => import('../views/dorm/DormRoomManageView.vue'), meta: { roles: ['admin', 'dorm_admin'] } },
      { path: 'facilities', name: 'facilities', component: () => import('../views/dorm/FacilityManageView.vue'), meta: { roles: ['admin', 'dorm_admin'] } },
      { path: 'students', name: 'students', component: () => import('../views/dorm/StudentStayManageView.vue'), meta: { roles: ['admin', 'dorm_admin'] } },
      { path: 'announcements', name: 'announcements', component: () => import('../views/dorm/AnnouncementManageView.vue'), meta: { roles: ['admin', 'dorm_admin'] } },
      { path: 'users', name: 'users', component: () => import('../views/admin/UserManageView.vue'), meta: { roles: ['admin'] } },
      { path: 'resources', name: 'resources', component: () => import('../views/admin/ResourceManageView.vue'), meta: { roles: ['admin'] } },
      { path: 'service-messages', name: 'service-messages', component: () => import('../views/admin/ServiceMessageManageView.vue'), meta: { roles: ['admin'] } },
      { path: 'config', name: 'config', component: () => import('../views/admin/ConfigManageView.vue'), meta: { roles: ['admin'] } },
      { path: 'logs', name: 'logs', component: () => import('../views/admin/SystemLogView.vue'), meta: { roles: ['admin'] } }
    ]
  },
  { path: '/refreshing', name: 'refreshing', component: { template: '<div />' } },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const auth = useAdminAuth()
  if (to.path === '/login' || to.path === '/refreshing') {
    if (to.path === '/login' && auth.hasToken()) return '/dashboard'
    return true
  }
  if (!auth.hasToken()) {
    return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  }
  if (!auth.state.userInfo.role) {
    try {
      await auth.loadMe()
    } catch {
      auth.logout()
      return `/login?redirect=${encodeURIComponent(to.fullPath)}`
    }
  }
  const roles = to.meta.roles
  if (Array.isArray(roles) && !roles.includes(auth.state.userInfo.role)) {
    const fallback = auth.state.userInfo.role === 'repairer' ? '/repair-work' : auth.state.userInfo.role === 'admin' ? '/dashboard' : '/orders'
    return fallback
  }
  return true
})

export default router
