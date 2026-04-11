import { createRouter, createWebHistory } from 'vue-router'
import AdminConsoleView from '../views/AdminConsoleView.vue'

const routes = [
  { path: '/', redirect: '/orders' },
  { path: '/orders', name: 'orders', component: AdminConsoleView },
  { path: '/repairer/orders', name: 'repairerOrders', component: AdminConsoleView },
  { path: '/repairer/stats', name: 'repairerStats', component: AdminConsoleView },
  { path: '/dorms', name: 'dorms', component: AdminConsoleView },
  { path: '/announcements', name: 'announcements', component: AdminConsoleView },
  { path: '/admin', name: 'admin', component: AdminConsoleView },
  { path: '/config', name: 'config', component: AdminConsoleView },
  { path: '/:pathMatch(.*)*', redirect: '/orders' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
