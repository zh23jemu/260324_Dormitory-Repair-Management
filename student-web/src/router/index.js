import { createRouter, createWebHistory } from 'vue-router'
import StudentPortalView from '../views/StudentPortalView.vue'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/home', name: 'home', component: StudentPortalView },
  { path: '/repair', name: 'repair', component: StudentPortalView },
  { path: '/orders', name: 'orders', component: StudentPortalView },
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
