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
                  <el-select v-model="registerForm.college" placeholder="请选择学院" filterable @change="handleCollegeChange">
                    <el-option v-for="item in collegeOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
                <el-form-item label="专业">
                  <el-select v-model="registerForm.major" placeholder="请选择专业" filterable :disabled="!registerForm.college" @change="handleMajorChange">
                    <el-option v-for="item in majorOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
                <el-form-item label="班级">
                  <el-select v-model="registerForm.className" placeholder="请选择班级" filterable :disabled="!registerForm.major">
                    <el-option v-for="item in classOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
                <el-form-item label="找回密码问题">
                  <el-input v-model="registerForm.passwordQuestion" placeholder="例如：你的学号后四位是什么？" />
                </el-form-item>
                <el-form-item label="找回密码答案">
                  <el-input v-model="registerForm.passwordAnswer" placeholder="请牢记该答案" />
                </el-form-item>
                <el-button type="success" class="full-width" :disabled="selectedRole !== 'student'" @click="submitRegister">注册学生账号</el-button>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="找回密码" name="forgot" :disabled="selectedRole === 'admin'">
              <el-alert
                v-if="selectedRole === 'admin'"
                type="info"
                :closable="false"
                title="管理员账号请联系系统管理员重置密码"
                style="margin-bottom: 14px"
              />
              <el-form :model="forgotForm" label-position="top" @submit.prevent>
                <el-form-item label="账号">
                  <el-input v-model="forgotForm.username" />
                </el-form-item>
                <el-button class="full-width" :disabled="selectedRole === 'admin'" @click="loadForgotQuestion">获取找回问题</el-button>
                <el-form-item v-if="forgotQuestion" label="找回密码问题" style="margin-top: 14px">
                  <el-input :model-value="forgotQuestion" disabled />
                </el-form-item>
                <el-form-item label="答案">
                  <el-input v-model="forgotForm.answer" :disabled="!forgotQuestion" placeholder="请输入预先设置的答案" />
                </el-form-item>
                <el-form-item label="新密码">
                  <el-input v-model="forgotForm.newPassword" type="password" show-password :disabled="!forgotQuestion" />
                </el-form-item>
                <el-button type="warning" class="full-width" :disabled="selectedRole === 'admin' || !forgotQuestion" @click="submitForgot">重置密码</el-button>
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
const registerForm = reactive({ username: '', password: '', realName: '', phone: '', studentNo: '', college: '', major: '', className: '', passwordQuestion: '', passwordAnswer: '' })
const forgotForm = reactive({ username: '', answer: '', newPassword: '' })
const forgotQuestion = ref('')

const roleOptions = [
  { label: '学生', value: 'student', desc: '报修、查单、评价、留言、论坛', demo: 'student01' },
  { label: '宿管', value: 'dorm_admin', desc: '审核工单、宿舍管理、公告与评价管理', demo: 'dorm01' },
  { label: '维修员', value: 'repairer', desc: '接单处理、个人统计、工种与耗材上报', demo: 'repair01' },
  { label: '管理员', value: 'admin', desc: '统计、配置、用户、日志、全局管理', demo: 'admin' }
]

// 注册信息中的学院、专业、班级采用固定选项，避免学生随意录入造成统计口径混乱。
const collegeOptions = [
  '计算机学院',
  '软件学院',
  '机电工程学院',
  '土木工程学院',
  '管理学院',
  '艺术学院',
  '外国语学院'
]

const majorOptionsMap = {
  '计算机学院': ['计算机科学与技术', '软件工程', '网络工程', '人工智能'],
  '软件学院': ['软件工程', '数据科学与大数据技术', '信息安全', '物联网工程'],
  '机电工程学院': ['机械设计制造及其自动化', '电气工程及其自动化', '自动化', '机器人工程'],
  '土木工程学院': ['土木工程', '工程管理', '建筑环境与能源应用工程', '给排水科学与工程'],
  '管理学院': ['工商管理', '会计学', '市场营销', '人力资源管理'],
  '艺术学院': ['视觉传达设计', '环境设计', '产品设计', '数字媒体艺术'],
  '外国语学院': ['英语', '商务英语', '日语', '翻译']
}

const classOptionsMap = {
  '计算机科学与技术': ['计科1班', '计科2班', '计科3班'],
  '软件工程': ['软件1班', '软件2班', '软件3班'],
  '网络工程': ['网工1班', '网工2班'],
  '人工智能': ['人工智能1班', '人工智能2班'],
  '数据科学与大数据技术': ['大数据1班', '大数据2班'],
  '信息安全': ['信安1班', '信安2班'],
  '物联网工程': ['物联网1班', '物联网2班'],
  '机械设计制造及其自动化': ['机制1班', '机制2班'],
  '电气工程及其自动化': ['电气1班', '电气2班'],
  '自动化': ['自动化1班', '自动化2班'],
  '机器人工程': ['机器人1班', '机器人2班'],
  '土木工程': ['土木1班', '土木2班'],
  '工程管理': ['工管1班', '工管2班'],
  '建筑环境与能源应用工程': ['建环1班', '建环2班'],
  '给排水科学与工程': ['给排水1班', '给排水2班'],
  '工商管理': ['工商1班', '工商2班'],
  '会计学': ['会计1班', '会计2班'],
  '市场营销': ['营销1班', '营销2班'],
  '人力资源管理': ['人资1班', '人资2班'],
  '视觉传达设计': ['视传1班', '视传2班'],
  '环境设计': ['环设1班', '环设2班'],
  '产品设计': ['产品1班', '产品2班'],
  '数字媒体艺术': ['数媒1班', '数媒2班'],
  '英语': ['英语1班', '英语2班'],
  '商务英语': ['商英1班', '商英2班'],
  '日语': ['日语1班', '日语2班'],
  翻译: ['翻译1班', '翻译2班']
}

const rolePlaceholder = computed(() => roleOptions.find((item) => item.value === selectedRole.value)?.demo || '请输入账号')
const majorOptions = computed(() => majorOptionsMap[registerForm.college] || [])
const classOptions = computed(() => classOptionsMap[registerForm.major] || [])

function handleRoleChange(role) {
  selectedRole.value = role
  loginForm.username = roleOptions.find((item) => item.value === role)?.demo || ''
  forgotQuestion.value = ''
  forgotForm.username = ''
  forgotForm.answer = ''
  forgotForm.newPassword = ''
  if (role === 'admin' && activeTab.value === 'forgot') {
    activeTab.value = 'login'
  }
}

function handleCollegeChange() {
  registerForm.major = ''
  registerForm.className = ''
}

function handleMajorChange() {
  registerForm.className = ''
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
  if (selectedRole.value === 'admin') {
    ElMessage.warning('管理员账号请联系系统管理员重置密码')
    return
  }
  await auth.forgotPassword(forgotForm)
  ElMessage.success('密码已重置，请使用新密码登录')
  activeTab.value = 'login'
  loginForm.username = forgotForm.username
  forgotQuestion.value = ''
  forgotForm.answer = ''
  forgotForm.newPassword = ''
}

async function loadForgotQuestion() {
  if (selectedRole.value === 'admin') {
    ElMessage.warning('管理员账号请联系系统管理员重置密码')
    return
  }
  if (!forgotForm.username.trim()) {
    ElMessage.warning('请输入账号')
    return
  }
  const data = await auth.forgotPasswordQuestion({ username: forgotForm.username.trim() })
  forgotQuestion.value = data.question || ''
  forgotForm.answer = ''
  forgotForm.newPassword = ''
}
</script>
