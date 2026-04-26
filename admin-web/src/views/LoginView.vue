<template>
  <div class="unified-login">
    <div class="unified-login__hero">
      <div class="unified-login__copy">
        <div class="unified-login__eyebrow">高校宿舍报修管理系统</div>
        <h1>统一登录入口</h1>
        <p>学生、宿管、维修员、管理员从同一个登录页进入系统。登录后会根据角色进入对应的门户或工作台。</p>
        <div class="unified-login__demo">
          <span>学生：student01 / 123456</span>
          <span>宿管：dorm01 / 123456</span>
          <span>维修员：repair01 / 123456</span>
          <span>管理员：admin / 123456</span>
        </div>
        <div class="unified-login__floating-card">
          <strong>统一门户与工作台</strong>
          <span>同一套视觉语言下完成公告浏览、报修提交、论坛交流与后台管理演示。</span>
        </div>
      </div>
      <div class="unified-login__panel">
        <el-card class="login-card">
          <template #header>
            <div>
              <div class="login-title">选择登录角色</div>
              <div class="login-subtitle">先选角色，再输入账号和密码</div>
            </div>
          </template>

          <div class="role-pick-grid">
            <button
              v-for="item in roleOptions"
              :key="item.value"
              type="button"
              class="role-pick-card"
              :class="{ active: selectedRole === item.value }"
              @click="handleRoleChange(item.value)"
            >
              <strong>{{ item.label }}</strong>
              <span>{{ item.desc }}</span>
            </button>
          </div>

          <el-tabs v-model="activeTab">
            <el-tab-pane label="登录" name="login">
              <el-form :model="loginForm" label-position="top" @submit.prevent>
                <el-form-item label="账号">
                  <el-input v-model="loginForm.username" :placeholder="rolePlaceholder" />
                </el-form-item>
                <el-form-item label="密码">
                  <el-input v-model="loginForm.password" type="password" show-password placeholder="123456" />
                </el-form-item>
                <el-button type="primary" class="full-width" @click="submitLogin">登录系统</el-button>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="注册" name="register" :disabled="selectedRole !== 'student'">
              <el-alert
                v-if="selectedRole !== 'student'"
                type="info"
                :closable="false"
                title="只有学生账号支持在线注册"
                style="margin-bottom: 14px"
              />
              <el-form :model="registerForm" label-position="top" @submit.prevent>
                <el-form-item label="账号">
                  <el-input v-model="registerForm.username" />
                </el-form-item>
                <el-form-item label="密码">
                  <el-input v-model="registerForm.password" type="password" show-password />
                </el-form-item>
                <el-form-item label="姓名">
                  <el-input v-model="registerForm.realName" />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="registerForm.phone" />
                </el-form-item>
                <el-form-item label="学号">
                  <el-input v-model="registerForm.studentNo" />
                </el-form-item>
                <el-form-item label="学院">
                  <el-input v-model="registerForm.college" />
                </el-form-item>
                <el-form-item label="专业">
                  <el-input v-model="registerForm.major" />
                </el-form-item>
                <el-form-item label="班级">
                  <el-input v-model="registerForm.className" />
                </el-form-item>
                <el-button type="success" class="full-width" :disabled="selectedRole !== 'student'" @click="submitRegister">注册学生账号</el-button>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="找回密码" name="forgot" :disabled="selectedRole !== 'student'">
              <el-alert
                v-if="selectedRole !== 'student'"
                type="info"
                :closable="false"
                title="当前仅学生账号支持自助找回密码"
                style="margin-bottom: 14px"
              />
              <el-form :model="forgotForm" label-position="top" @submit.prevent>
                <el-form-item label="账号">
                  <el-input v-model="forgotForm.username" />
                </el-form-item>
                <el-form-item label="学号">
                  <el-input v-model="forgotForm.studentNo" />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="forgotForm.phone" />
                </el-form-item>
                <el-form-item label="新密码">
                  <el-input v-model="forgotForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-button type="warning" class="full-width" :disabled="selectedRole !== 'student'" @click="submitForgot">重置密码</el-button>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuth } from '../utils/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuth()
const activeTab = ref('login')
const selectedRole = ref(String(route.query.role || 'student'))
const loginForm = reactive({ username: 'student01', password: '123456' })
const registerForm = reactive({ username: '', password: '', realName: '', phone: '', studentNo: '', college: '', major: '', className: '' })
const forgotForm = reactive({ username: '', studentNo: '', phone: '', newPassword: '' })

const roleOptions = [
  { label: '学生', value: 'student', desc: '报修、查单、评价、留言、论坛', demo: 'student01' },
  { label: '宿管', value: 'dorm_admin', desc: '审核工单、宿舍管理、公告与评价管理', demo: 'dorm01' },
  { label: '维修员', value: 'repairer', desc: '接单处理、个人统计、工种与耗材上报', demo: 'repair01' },
  { label: '管理员', value: 'admin', desc: '统计、配置、用户、日志、全局管理', demo: 'admin' }
]

const rolePlaceholder = computed(() => roleOptions.find((item) => item.value === selectedRole.value)?.demo || '请输入账号')

function handleRoleChange(role) {
  selectedRole.value = role
  loginForm.username = roleOptions.find((item) => item.value === role)?.demo || ''
}

async function submitLogin() {
  const result = await auth.login(loginForm)
  if (result.userInfo?.role !== selectedRole.value) {
    auth.logout()
    ElMessage.error('账号角色与所选登录角色不一致')
    return
  }
  router.replace(String(route.query.redirect || auth.getRoleHomePath(result.userInfo?.role)))
}

async function submitRegister() {
  if (selectedRole.value !== 'student') {
    ElMessage.warning('当前仅学生账号支持在线注册')
    return
  }
  await auth.register(registerForm)
  ElMessage.success('注册成功，请登录')
  activeTab.value = 'login'
  loginForm.username = registerForm.username
}

async function submitForgot() {
  if (selectedRole.value !== 'student') {
    ElMessage.warning('当前仅学生账号支持找回密码')
    return
  }
  await auth.forgotPassword(forgotForm)
  ElMessage.success('密码已重置，请使用新密码登录')
  activeTab.value = 'login'
}
</script>
