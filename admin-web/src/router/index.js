import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import PortalLayoutView from '../views/PortalLayoutView.vue'
import AdminLayoutView from '../views/AdminLayoutView.vue'
import { useAuth } from '../utils/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { public: true }
  },
  {
    path: '/',
    component: PortalLayoutView,
    meta: { layout: 'portal' },
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', name: 'home', component: () => import('../views/student/HomeView.vue'), meta: { public: true, layout: 'portal' } },
      { path: 'announcements', name: 'announcements', component: () => import('../views/student/AnnouncementListView.vue'), meta: { public: true, layout: 'portal' } },
      { path: 'forum', name: 'forum', component: () => import('../views/student/ForumView.vue'), meta: { public: true, layout: 'portal' } },
      { path: 'repairers', name: 'repairers', component: () => import('../views/student/RepairerListView.vue'), meta: { public: true, layout: 'portal' } },
      { path: 'repairers/:id', name: 'repairer-detail', component: () => import('../views/student/RepairerDetailView.vue'), meta: { public: true, layout: 'portal' } },
      { path: 'student/repair/create', name: 'student-repair-create', component: () => import('../views/student/RepairCreateView.vue'), meta: { roles: ['student'], layout: 'portal' } },
      { path: 'student/orders', name: 'student-orders', component: () => import('../views/student/OrderListView.vue'), meta: { roles: ['student'], layout: 'portal' } },
      { path: 'student/orders/:id', name: 'student-order-detail', component: () => import('../views/student/OrderDetailView.vue'), meta: { roles: ['student'], layout: 'portal' } },
      { path: 'student/messages', name: 'student-messages', component: () => import('../views/student/ServiceMessageView.vue'), meta: { roles: ['student'], layout: 'portal' } },
      { path: 'student/profile', name: 'student-profile', component: () => import('../views/student/ProfileView.vue'), meta: { roles: ['student'], layout: 'portal' } }
    ]
  },
  {
    path: '/admin',
    component: AdminLayoutView,
    meta: { roles: ['admin', 'dorm_admin', 'repairer'], layout: 'admin' },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'dashboard', component: () => import('../views/admin/AdminDashboardView.vue'), meta: { roles: ['admin'], layout: 'admin' } },
      { path: 'orders', name: 'orders', component: () => import('../views/dorm/OrderManageView.vue'), meta: { roles: ['admin', 'dorm_admin'], layout: 'admin' } },
      { path: 'repair-work', name: 'repair-work', component: () => import('../views/repairer/RepairWorkView.vue'), meta: { roles: ['repairer'], layout: 'admin' } },
      { path: 'repairer/stats', name: 'repairer-stats', component: () => import('../views/repairer/RepairerStatsView.vue'), meta: { roles: ['repairer'], layout: 'admin' } },
      { path: 'repairer/list', name: 'repairer-list', component: () => import('../views/repairer/RepairerInfoView.vue'), meta: { roles: ['repairer'], layout: 'admin' } },
      { path: 'repairer/profile', name: 'repairer-profile', component: () => import('../views/repairer/RepairerProfileView.vue'), meta: { roles: ['repairer'], layout: 'admin' } },
      { path: 'dorm-rooms', name: 'dorm-rooms', component: () => import('../views/dorm/DormRoomManageView.vue'), meta: { roles: ['admin', 'dorm_admin'], layout: 'admin' } },
      { path: 'facilities', name: 'facilities', component: () => import('../views/dorm/FacilityManageView.vue'), meta: { roles: ['admin', 'dorm_admin'], layout: 'admin' } },
      { path: 'students', name: 'students', component: () => import('../views/dorm/StudentStayManageView.vue'), meta: { roles: ['admin', 'dorm_admin'], layout: 'admin' } },
      { path: 'announcements', name: 'admin-announcements', component: () => import('../views/dorm/AnnouncementManageView.vue'), meta: { roles: ['admin', 'dorm_admin'], layout: 'admin' } },
      { path: 'users', name: 'users', component: () => import('../views/admin/UserManageView.vue'), meta: { roles: ['admin'], layout: 'admin' } },
      { path: 'resources', name: 'resources', component: () => import('../views/admin/ResourceManageView.vue'), meta: { roles: ['admin'], layout: 'admin' } },
      { path: 'forum-posts', name: 'forum-posts', component: () => import('../views/admin/ForumManageView.vue'), meta: { roles: ['admin', 'dorm_admin'], layout: 'admin' } },
      { path: 'service-messages', name: 'service-messages', component: () => import('../views/admin/ServiceMessageManageView.vue'), meta: { roles: ['admin', 'dorm_admin'], layout: 'admin' } },
      { path: 'ratings', name: 'ratings', component: () => import('../views/admin/RatingManageView.vue'), meta: { roles: ['admin', 'dorm_admin'], layout: 'admin' } },
      { path: 'materials', name: 'materials', component: () => import('../views/admin/MaterialManageView.vue'), meta: { roles: ['admin'], layout: 'admin' } },
      { path: 'config', name: 'config', component: () => import('../views/admin/ConfigManageView.vue'), meta: { roles: ['admin'], layout: 'admin' } },
      { path: 'logs', name: 'logs', component: () => import('../views/admin/SystemLogView.vue'), meta: { roles: ['admin'], layout: 'admin' } }
    ]
  },
  { path: '/refreshing', name: 'refreshing', component: { template: '<div />' }, meta: { public: true } },
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const auth = useAuth()
  if (to.meta.public || to.path === '/refreshing') {
    if (to.path === '/login' && auth.hasToken()) {
      return auth.getRoleHomePath()
    }
    return true
  }
  if (!auth.hasToken()) {
    return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  }
  if (!auth.state.role) {
    try {
      await auth.loadMe()
    } catch {
      auth.logout()
      return `/login?redirect=${encodeURIComponent(to.fullPath)}`
    }
  }
  const roles = to.meta.roles
  if (Array.isArray(roles) && !auth.hasAnyRole(roles)) {
    return auth.getRoleHomePath()
  }
  return true
})

export default router
