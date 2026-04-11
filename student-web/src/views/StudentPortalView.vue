<template>
  <div v-if="!token" style="padding: 16px">
    <van-tabs v-model:active="authTab">
      <van-tab title="登录">
        <div class="section">
          <van-field v-model="loginForm.username" label="账号" placeholder="student01" />
          <van-field v-model="loginForm.password" label="密码" type="password" placeholder="123456" />
          <van-button type="primary" block @click="login">登录</van-button>
        </div>
      </van-tab>
      <van-tab title="注册">
        <div class="section">
          <van-field v-model="registerForm.username" label="账号" />
          <van-field v-model="registerForm.password" label="密码" type="password" />
          <van-field v-model="registerForm.realName" label="姓名" />
          <van-field v-model="registerForm.phone" label="手机号" />
          <van-field v-model="registerForm.studentNo" label="学号" />
          <van-field v-model="registerForm.college" label="学院" />
          <van-field v-model="registerForm.major" label="专业" />
          <van-field v-model="registerForm.className" label="班级" />
          <van-button type="success" block @click="register">注册</van-button>
        </div>
      </van-tab>
      <van-tab title="找回密码">
        <div class="section">
          <van-field v-model="forgotForm.username" label="账号" />
          <van-field v-model="forgotForm.studentNo" label="学号" />
          <van-field v-model="forgotForm.phone" label="手机号" />
          <van-field v-model="forgotForm.newPassword" label="新密码" type="password" />
          <van-button type="warning" block @click="forgotPassword">重置密码</van-button>
        </div>
      </van-tab>
    </van-tabs>
  </div>

  <div v-else class="mobile-shell">
    <div class="hero">
      <div style="font-size: 24px; font-weight: 700">学生报修端</div>
      <div style="margin-top: 6px">{{ profile.realName }}，{{ profile.studentNo || '未绑定学号' }}</div>
      <div style="margin-top: 8px; font-size: 13px; opacity: 0.9">手机浏览器即可访问和报修</div>
    </div>

    <div v-if="activeSection === 'home'" class="section">
      <div style="font-weight: 700; margin-bottom: 8px">公告通知</div>
      <van-notice-bar v-if="announcements.length" :text="announcements[0].title + '：' + announcements[0].content" left-icon="volume-o" />
      <div v-else style="color: #64748b">暂无公告</div>
    </div>

    <div v-if="activeSection === 'repair'" class="section">
      <div style="font-weight: 700; margin-bottom: 8px">提交报修</div>
      <van-field v-model="repairForm.title" label="标题" placeholder="例如：水龙头漏水" />
      <van-field v-model="repairForm.description" label="描述" type="textarea" rows="3" placeholder="请填写具体故障情况" />
      <van-field label="期望时间">
        <template #input>
          <input v-model="repairForm.expectTimeInput" class="datetime-input" type="datetime-local" />
        </template>
      </van-field>
      <van-field label="报修类型">
        <template #input>
          <van-radio-group v-model="repairForm.repairTypeId" direction="horizontal" class="type-group">
            <van-radio v-for="item in repairTypes" :key="item.id" :name="item.id">{{ item.typeName }}</van-radio>
          </van-radio-group>
        </template>
      </van-field>
      <div style="margin: 10px 0 14px">
        <van-uploader v-model="uploaderFiles" :after-read="afterRead" multiple :max-count="3" />
      </div>
      <van-button type="primary" block @click="submitRepair">提交报修</van-button>
    </div>

    <div v-if="activeSection === 'orders'" class="section">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px">
        <div style="font-weight: 700">我的报修</div>
        <van-button size="small" plain type="primary" @click="loadOrders">刷新</van-button>
      </div>
      <van-cell-group inset>
        <van-cell v-for="item in orders" :key="item.id" :title="item.title" :label="`${item.repairTypeName || ''} / ${item.buildingName || ''}${item.roomNo || ''}`" :value="item.status" is-link @click="showOrder(item)" />
      </van-cell-group>
    </div>

    <van-tabbar v-model="activeSection" route fixed placeholder active-color="#2563eb">
      <van-tabbar-item replace to="/home" name="home" icon="volume-o">公告</van-tabbar-item>
      <van-tabbar-item replace to="/repair" name="repair" icon="edit">报修</van-tabbar-item>
      <van-tabbar-item replace to="/orders" name="orders" icon="orders-o">工单</van-tabbar-item>
    </van-tabbar>

    <van-popup v-model:show="orderVisible" round position="bottom" :style="{ height: '82%' }">
      <div style="padding: 16px">
        <div style="font-size: 20px; font-weight: 700; margin-bottom: 10px">工单详情</div>
        <div v-if="currentOrder">
          <p>工单号：{{ currentOrder.orderNo }}</p>
          <p>状态：{{ currentOrder.status }}</p>
          <p>标题：{{ currentOrder.title }}</p>
          <p>描述：{{ currentOrder.description }}</p>
          <p>期望时间：{{ currentOrder.expectTime || '未填写' }}</p>
          <p>维修人员：{{ currentOrder.repairerName || '待分配' }}</p>
          <p>维修结果：{{ currentOrder.resultDesc || '暂无' }}</p>
          <p v-if="currentOrder.rejectReason">驳回原因：{{ currentOrder.rejectReason }}</p>
          <p v-if="currentOrder.rating">我的评分：{{ currentOrder.rating.score }} 星</p>
          <p v-if="currentOrder.rating?.content">评价内容：{{ currentOrder.rating.content }}</p>
          <div style="margin-top: 14px; font-weight: 700">图片记录</div>
          <div v-if="currentOrder.images?.length" class="mobile-image-grid">
            <img v-for="img in currentOrder.images" :key="img.id" :src="fileUrl(img.file_path)" @click="previewImage(fileUrl(img.file_path))" />
          </div>
          <div v-else style="color:#64748b; margin-top: 8px">暂无图片</div>
          <van-button v-if="currentOrder.status === 'pending_rating'" type="success" block style="margin-top: 16px" @click="openRating">提交评价</van-button>
        </div>
      </div>
    </van-popup>

    <van-popup v-model:show="ratingVisible" round position="bottom" :style="{ minHeight: '52%' }">
      <div style="padding: 16px">
        <div style="font-size: 20px; font-weight: 700; margin-bottom: 12px">服务评价</div>
        <div v-if="ratingIndicators.length" class="indicator-box">
          <div style="font-weight: 700; margin-bottom: 8px">评价参考</div>
          <div class="indicator-list">
            <span v-for="item in ratingIndicators" :key="item.id" class="indicator-chip">{{ item.dictName }}</span>
          </div>
        </div>
        <div style="margin: 16px 0 12px">
          <div style="font-weight: 700; margin-bottom: 8px">评分</div>
          <van-rate v-model="ratingForm.score" :count="5" />
        </div>
        <van-field v-model="ratingForm.content" label="评价内容" type="textarea" rows="4" placeholder="请填写本次维修服务评价" />
        <div style="margin-top: 16px; display: flex; gap: 12px">
          <van-button plain block @click="ratingVisible = false">取消</van-button>
          <van-button type="success" block @click="rateOrder">提交</van-button>
        </div>
      </div>
    </van-popup>

    <van-image-preview v-model:show="previewVisible" :images="previewImages" :start-position="previewIndex" />

    <div style="padding: 0 14px 12px">
      <van-button plain danger block @click="logout">退出登录</van-button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showNotify, showToast } from 'vant'
import api from '../api'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
const baseUrl = apiBaseUrl.replace(/\/api\/?$/, '')
const token = ref(localStorage.getItem('student_token') || '')
const route = useRoute()
const router = useRouter()
const authTab = ref(0)
const profile = reactive({})
const announcements = ref([])
const repairTypes = ref([])
const ratingIndicators = ref([])
const orders = ref([])
const orderVisible = ref(false)
const ratingVisible = ref(false)
const previewVisible = ref(false)
const previewImages = ref([])
const previewIndex = ref(0)
const currentOrder = ref(null)
const uploaderFiles = ref([])
const loginForm = reactive({ username: 'student01', password: '123456' })
const registerForm = reactive({ username: '', password: '', realName: '', phone: '', studentNo: '', college: '', major: '', className: '' })
const forgotForm = reactive({ username: '', studentNo: '', phone: '', newPassword: '' })
const repairForm = reactive({ repairTypeId: null, title: '', description: '', expectTimeInput: '', imagePaths: [] })
const ratingForm = reactive({ score: 5, content: '' })
const sectionPathMap = {
  home: '/home',
  repair: '/repair',
  orders: '/orders'
}
const activeSection = computed({
  get() {
    return typeof route.name === 'string' ? route.name : 'home'
  },
  set(value) {
    const target = sectionPathMap[value] || '/home'
    if (route.path !== target) router.push(target)
  }
})

function fileUrl(path) {
  return `${baseUrl}${path}`
}

function toApiDateTime(value) {
  return value ? `${value.replace('T', ' ')}:00` : ''
}

async function login() {
  const { data } = await api.post('/auth/login', loginForm)
  localStorage.setItem('student_token', data.data.token)
  token.value = data.data.token
  await bootstrap()
  router.push('/repair')
}

async function register() {
  await api.post('/auth/register', registerForm)
  showToast('注册成功，请登录')
  authTab.value = 0
}

async function forgotPassword() {
  await api.post('/auth/forgot-password', forgotForm)
  showToast('密码已重置，请使用新密码登录')
  forgotForm.username = ''
  forgotForm.studentNo = ''
  forgotForm.phone = ''
  forgotForm.newPassword = ''
  authTab.value = 0
}

async function bootstrap() {
  Object.assign(profile, (await api.get('/auth/me')).data.data)
  announcements.value = (await api.get('/student/announcements')).data.data
  repairTypes.value = (await api.get('/student/repair-types')).data.data
  ratingIndicators.value = (await api.get('/student/rating-indicators')).data.data
  if (!repairForm.repairTypeId && repairTypes.value.length) {
    repairForm.repairTypeId = repairTypes.value[0].id
  }
  await loadOrders()
  if (!['home', 'repair', 'orders'].includes(String(route.name || ''))) {
    router.replace('/repair')
  }
}

async function afterRead(file) {
  const files = Array.isArray(file) ? file : [file]
  for (const item of files) {
    const formData = new FormData()
    formData.append('file', item.file)
    const { data } = await api.post('/common/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    repairForm.imagePaths.push(data.data.filePath)
  }
  showToast('图片上传完成')
}

async function submitRepair() {
  await api.post('/student/repair-orders', {
    repairTypeId: repairForm.repairTypeId,
    title: repairForm.title,
    description: repairForm.description,
    expectTime: toApiDateTime(repairForm.expectTimeInput),
    imagePaths: repairForm.imagePaths
  })
  showNotify({ type: 'success', message: '报修提交成功' })
  repairForm.title = ''
  repairForm.description = ''
  repairForm.expectTimeInput = ''
  repairForm.imagePaths = []
  uploaderFiles.value = []
  if (repairTypes.value.length) {
    repairForm.repairTypeId = repairTypes.value[0].id
  }
  await loadOrders()
}

async function loadOrders() {
  orders.value = (await api.get('/student/repair-orders')).data.data
}

async function showOrder(item) {
  currentOrder.value = (await api.get(`/student/repair-orders/${item.id}`)).data.data
  orderVisible.value = true
}

function previewImage(src) {
  previewImages.value = (currentOrder.value?.images || []).map((img) => fileUrl(img.file_path))
  previewIndex.value = previewImages.value.indexOf(src)
  previewVisible.value = true
}

function openRating() {
  ratingForm.score = 5
  ratingForm.content = ''
  ratingVisible.value = true
}

async function rateOrder() {
  await api.post(`/student/repair-orders/${currentOrder.value.id}/rating`, { score: ratingForm.score, content: ratingForm.content })
  showToast('评价成功')
  ratingVisible.value = false
  orderVisible.value = false
  await loadOrders()
}

function logout() {
  localStorage.removeItem('student_token')
  token.value = ''
  router.push('/home')
}

onMounted(async () => {
  if (token.value) {
    try {
      await bootstrap()
    } catch {
      logout()
    }
  }
})
</script>
