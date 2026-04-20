import { reactive } from 'vue'
import api from '../api'

const state = reactive({
  token: localStorage.getItem('admin_token') || '',
  userInfo: {}
})

function hasToken() {
  return Boolean(state.token)
}

function isAdmin() {
  return state.userInfo.role === 'admin'
}

function isDormAdmin() {
  return ['admin', 'dorm_admin'].includes(state.userInfo.role)
}

function isRepairer() {
  return state.userInfo.role === 'repairer'
}

async function login(payload) {
  const { data } = await api.post('/auth/login', payload)
  state.token = data.data.token
  localStorage.setItem('admin_token', state.token)
  await loadMe()
  return data.data
}

async function loadMe() {
  const { data } = await api.get('/auth/me')
  Object.keys(state.userInfo).forEach((key) => delete state.userInfo[key])
  Object.assign(state.userInfo, data.data)
  return state.userInfo
}

function logout() {
  localStorage.removeItem('admin_token')
  state.token = ''
  Object.keys(state.userInfo).forEach((key) => delete state.userInfo[key])
}

export function useAdminAuth() {
  return {
    state,
    hasToken,
    isAdmin,
    isDormAdmin,
    isRepairer,
    login,
    loadMe,
    logout
  }
}
