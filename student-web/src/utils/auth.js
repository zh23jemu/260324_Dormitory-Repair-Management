import { reactive } from 'vue'
import api from '../api'

const state = reactive({
  token: localStorage.getItem('student_token') || '',
  profile: {}
})

function hasToken() {
  return Boolean(state.token)
}

async function login(payload) {
  const { data } = await api.post('/auth/login', payload)
  state.token = data.data.token
  localStorage.setItem('student_token', state.token)
  await loadProfile()
  return data.data
}

async function register(payload) {
  await api.post('/auth/register', payload)
}

async function forgotPassword(payload) {
  await api.post('/auth/forgot-password', payload)
}

async function loadProfile() {
  const { data } = await api.get('/auth/me')
  Object.keys(state.profile).forEach((key) => delete state.profile[key])
  Object.assign(state.profile, data.data)
  return state.profile
}

function logout() {
  localStorage.removeItem('student_token')
  state.token = ''
  Object.keys(state.profile).forEach((key) => delete state.profile[key])
}

export function useStudentAuth() {
  return {
    state,
    hasToken,
    login,
    register,
    forgotPassword,
    loadProfile,
    logout
  }
}
