import { createRouter, createWebHistory } from 'vue-router'
import AuthView from '../views/AuthView.vue'
import StudentLayoutView from '../views/StudentLayoutView.vue'
import { useStudentAuth } from '../utils/auth'

const routes = [
  { path: '/login', name: 'login', component: AuthView },
  {
    path: '/',
    component: StudentLayoutView,
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', name: 'home', component: () => import('../views/student/HomeView.vue') },
      { path: 'repair/create', name: 'repair-create', component: () => import('../views/student/RepairCreateView.vue') },
      { path: 'orders', name: 'orders', component: () => import('../views/student/OrderListView.vue') },
      { path: 'orders/:id', name: 'order-detail', component: () => import('../views/student/OrderDetailView.vue') },
      { path: 'resources', name: 'resources', component: () => import('../views/student/ResourceListView.vue') },
      { path: 'resources/:id', name: 'resource-detail', component: () => import('../views/student/ResourceDetailView.vue') },
      { path: 'repairers', name: 'repairers', component: () => import('../views/student/RepairerListView.vue') },
      { path: 'repairers/:id', name: 'repairer-detail', component: () => import('../views/student/RepairerDetailView.vue') },
      { path: 'messages', name: 'messages', component: () => import('../views/student/ServiceMessageView.vue') },
      { path: 'profile', name: 'profile', component: () => import('../views/student/ProfileView.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const auth = useStudentAuth()
  if (to.path === '/login') {
    if (auth.hasToken()) return '/home'
    return true
  }
  if (!auth.hasToken()) {
    return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  }
  if (!auth.state.profile.id) {
    try {
      await auth.loadProfile()
    } catch {
      auth.logout()
      return `/login?redirect=${encodeURIComponent(to.fullPath)}`
    }
  }
  return true
})

export default router
