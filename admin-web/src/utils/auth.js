import { reactive } from 'vue'
import api from '../api'

const ROLE_HOME_PATH = {
  student: '/home',
  dorm_admin: '/admin/orders',
  repairer: '/admin/repair-work',
  admin: '/admin/dashboard'
}

const state = reactive({
  token: localStorage.getItem('auth_token') || '',
  userInfo: {},
  role: ''
})

function hasToken() {
  return Boolean(state.token)
}

function hasRole(role) {
  return state.role === role
}

function hasAnyRole(roles) {
  return Array.isArray(roles) && roles.includes(state.role)
}

function getRoleHomePath(role = state.role) {
  return ROLE_HOME_PATH[role] || '/home'
}

function clearUserInfo() {
  Object.keys(state.userInfo).forEach((key) => delete state.userInfo[key])
}

function setAuth(token, userInfo = {}) {
  state.token = token || ''
  state.role = userInfo.role || ''
  clearUserInfo()
  Object.assign(state.userInfo, userInfo)
  if (state.token) {
    localStorage.setItem('auth_token', state.token)
  }
}

async function login(payload) {
  const { data } = await api.post('/auth/login', payload)
  setAuth(data.data.token, data.data.userInfo || {})
  if (!state.role) {
    await loadMe()
  }
  return {
    ...data.data,
    roleHomePath: getRoleHomePath(state.role)
  }
}

async function register(payload) {
  await api.post('/auth/register', payload)
}

async function forgotPassword(payload) {
  await api.post('/auth/forgot-password', payload)
}

async function loadMe() {
  const { data } = await api.get('/auth/me')
  state.role = data.data.role || ''
  clearUserInfo()
  Object.assign(state.userInfo, data.data)
  return state.userInfo
}

function logout() {
  localStorage.removeItem('auth_token')
  state.token = ''
  state.role = ''
  clearUserInfo()
}

export function useAuth() {
  return {
    state,
    hasToken,
    hasRole,
    hasAnyRole,
    getRoleHomePath,
    setAuth,
    login,
    register,
    forgotPassword,
    loadMe,
    logout
  }
}
