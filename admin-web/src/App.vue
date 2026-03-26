<template>
  <div v-if="!token" class="login-wrap">
    <el-card class="login-card">
      <template #header>
        <div>
          <div style="font-size: 24px; font-weight: 700">宿舍报修管理端</div>
          <div style="color: #6b7280; margin-top: 6px">宿管、维修人员、管理员使用</div>
        </div>
      </template>
      <el-form :model="loginForm" @submit.prevent>
        <el-form-item label="账号"><el-input v-model="loginForm.username" placeholder="admin / dorm01 / repair01" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="loginForm.password" type="password" show-password placeholder="123456" /></el-form-item>
        <el-button type="primary" style="width: 100%" @click="login">登录</el-button>
      </el-form>
    </el-card>
  </div>

  <div v-else class="page-shell">
    <div class="toolbar">
      <div>
        <div style="font-size: 28px; font-weight: 700">高校宿舍报修管理系统</div>
        <div style="color: #6b7280; margin-top: 4px">当前用户：{{ userInfo.realName }} / {{ userInfo.role }}</div>
      </div>
      <div>
        <el-button @click="refreshAll">刷新</el-button>
        <el-button type="danger" plain @click="logout">退出</el-button>
      </div>
    </div>

    <div v-if="userInfo.role === 'admin'">
      <div class="stat-grid">
        <div class="stat-card"><div>总工单</div><h2>{{ overview.totalRepairCount || 0 }}</h2></div>
        <div class="stat-card"><div>待审核</div><h2>{{ overview.pendingReviewCount || 0 }}</h2></div>
        <div class="stat-card"><div>处理中</div><h2>{{ overview.processingCount || 0 }}</h2></div>
        <div class="stat-card"><div>平均评分</div><h2>{{ Number(overview.avgScore || 0).toFixed(1) }}</h2></div>
      </div>
      <div class="grid-two" style="margin-bottom: 16px">
        <el-card><template #header>报修类型统计</template><div ref="typeChartRef" class="chart-box"></div></el-card>
        <el-card><template #header>楼栋报修热度</template><div ref="buildingChartRef" class="chart-box"></div></el-card>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane v-if="isDorm" label="工单管理" name="orders">
        <div class="toolbar" style="margin-bottom: 12px">
          <div style="display:flex; gap:10px; align-items:center; flex-wrap:wrap">
            <div style="font-weight: 700">工单列表</div>
            <el-select v-model="orderFilter.status" placeholder="状态筛选" clearable style="width: 160px">
              <el-option label="待审核" value="pending_review" />
              <el-option label="已驳回" value="rejected" />
              <el-option label="待接单" value="pending_accept" />
              <el-option label="处理中" value="processing" />
              <el-option label="待评价" value="pending_rating" />
              <el-option label="已完成" value="completed" />
            </el-select>
            <el-input v-model="orderFilter.keyword" placeholder="学生/标题/宿舍" style="width: 220px" clearable />
          </div>
          <el-button @click="exportCsv('工单列表', filteredOrders, orderExportColumns)">导出 CSV</el-button>
        </div>
        <el-table :data="filteredOrders" stripe>
          <el-table-column prop="orderNo" label="工单号" min-width="160" />
          <el-table-column prop="studentName" label="学生" width="120" />
          <el-table-column prop="repairTypeName" label="类型" width="100" />
          <el-table-column prop="buildingName" label="楼栋" width="100" />
          <el-table-column prop="roomNo" label="宿舍" width="90" />
          <el-table-column prop="status" label="状态" width="130" />
          <el-table-column prop="title" label="标题" min-width="180" />
          <el-table-column label="操作" width="280">
            <template #default="scope">
              <el-button size="small" @click="openOrder(scope.row)">详情</el-button>
              <el-button v-if="scope.row.status === 'pending_review'" size="small" type="primary" @click="assign(scope.row)">分配</el-button>
              <el-button v-if="scope.row.status === 'pending_review'" size="small" type="danger" plain @click="reject(scope.row)">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="isRepairer" label="我的工单" name="repairerOrders">
        <div class="toolbar" style="margin-bottom: 12px">
          <div style="display:flex; gap:10px; align-items:center; flex-wrap:wrap">
            <div style="font-weight: 700">维修任务</div>
            <el-select v-model="repairerFilter.status" placeholder="状态筛选" clearable style="width: 160px">
              <el-option label="待接单" value="pending_accept" />
              <el-option label="处理中" value="processing" />
              <el-option label="待评价" value="pending_rating" />
              <el-option label="已完成" value="completed" />
            </el-select>
          </div>
          <el-button @click="exportCsv('维修任务', filteredRepairerOrders, repairerExportColumns)">导出 CSV</el-button>
        </div>
        <el-table :data="filteredRepairerOrders" stripe>
          <el-table-column prop="orderNo" label="工单号" min-width="160" />
          <el-table-column prop="studentName" label="学生" width="120" />
          <el-table-column prop="repairTypeName" label="类型" width="100" />
          <el-table-column prop="status" label="状态" width="130" />
          <el-table-column prop="title" label="标题" min-width="180" />
          <el-table-column label="操作" width="260">
            <template #default="scope">
              <el-button size="small" @click="openRepairerOrder(scope.row)">详情</el-button>
              <el-button v-if="scope.row.status === 'pending_accept'" size="small" type="primary" @click="acceptOrder(scope.row)">接单</el-button>
              <el-button v-if="scope.row.status === 'processing'" size="small" type="success" @click="openFeedback(scope.row)">提交结果</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="isDorm" label="宿舍管理" name="dorms">
        <div class="grid-two">
          <el-card>
            <template #header>楼栋</template>
            <el-form :model="buildingForm" inline>
              <el-form-item><el-input v-model="buildingForm.buildingName" placeholder="楼栋名称" /></el-form-item>
              <el-form-item><el-input v-model="buildingForm.buildingCode" placeholder="楼栋编码" /></el-form-item>
              <el-form-item><el-input-number v-model="buildingForm.floorCount" :min="1" /></el-form-item>
              <el-form-item><el-button type="primary" @click="createBuilding">新增楼栋</el-button></el-form-item>
            </el-form>
            <el-table :data="buildings" size="small">
              <el-table-column prop="buildingName" label="名称" />
              <el-table-column prop="buildingCode" label="编码" />
              <el-table-column prop="floorCount" label="楼层" />
            </el-table>
          </el-card>
          <el-card>
            <template #header>宿舍</template>
            <el-form :model="roomForm" inline>
              <el-form-item><el-select v-model="roomForm.buildingId" placeholder="楼栋" style="width: 120px"><el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" /></el-select></el-form-item>
              <el-form-item><el-input v-model="roomForm.roomNo" placeholder="宿舍号" /></el-form-item>
              <el-form-item><el-input-number v-model="roomForm.capacity" :min="1" /></el-form-item>
              <el-form-item><el-button type="primary" @click="createRoom">新增宿舍</el-button></el-form-item>
            </el-form>
            <el-table :data="rooms" size="small">
              <el-table-column prop="buildingName" label="楼栋" />
              <el-table-column prop="roomNo" label="宿舍" />
              <el-table-column prop="capacity" label="容量" />
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane v-if="isDorm" label="公告管理" name="announcements">
        <el-form :model="announcementForm" inline>
          <el-form-item><el-input v-model="announcementForm.title" placeholder="公告标题" style="width: 240px" /></el-form-item>
          <el-form-item><el-input v-model="announcementForm.content" placeholder="公告内容" style="width: 380px" /></el-form-item>
          <el-form-item><el-button type="primary" @click="createAnnouncement">发布公告</el-button></el-form-item>
        </el-form>
        <el-table :data="announcements" stripe>
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column prop="content" label="内容" min-width="260" />
          <el-table-column prop="publisherName" label="发布人" width="120" />
          <el-table-column prop="publishedAt" label="发布时间" width="180" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="isAdmin" label="用户与日志" name="admin">
        <div class="grid-two">
          <el-card>
            <template #header><div class="toolbar" style="margin-bottom:0"><span>用户管理</span><el-button size="small" @click="exportCsv('用户列表', users, userExportColumns)">导出 CSV</el-button></div></template>
            <el-form :model="userForm" inline>
              <el-form-item><el-input v-model="userForm.username" placeholder="账号" /></el-form-item>
              <el-form-item><el-input v-model="userForm.realName" placeholder="姓名" /></el-form-item>
              <el-form-item><el-select v-model="userForm.role" placeholder="角色" style="width: 140px"><el-option label="宿管" value="dorm_admin" /><el-option label="维修" value="repairer" /><el-option label="管理员" value="admin" /></el-select></el-form-item>
              <el-form-item><el-button type="primary" @click="createUser">新增用户</el-button></el-form-item>
            </el-form>
            <el-table :data="users" size="small">
              <el-table-column prop="username" label="账号" />
              <el-table-column prop="realName" label="姓名" />
              <el-table-column prop="role" label="角色" />
              <el-table-column prop="status" label="状态" />
            </el-table>
          </el-card>
          <el-card>
            <template #header><div class="toolbar" style="margin-bottom:0"><span>系统日志</span><el-button size="small" @click="exportCsv('系统日志', logs, logExportColumns)">导出 CSV</el-button></div></template>
            <el-table :data="logs" size="small">
              <el-table-column prop="moduleName" label="模块" />
              <el-table-column prop="operationType" label="操作" />
              <el-table-column prop="userName" label="用户" />
              <el-table-column prop="operationDesc" label="描述" min-width="180" />
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="orderDialogVisible" title="工单详情" width="860px">
      <div v-if="currentOrder" class="detail-layout">
        <div class="detail-grid">
          <div><strong>工单号：</strong>{{ currentOrder.orderNo }}</div>
          <div><strong>状态：</strong>{{ currentOrder.status }}</div>
          <div><strong>学生：</strong>{{ currentOrder.studentName }}</div>
          <div><strong>维修人员：</strong>{{ currentOrder.repairerName || '待分配' }}</div>
          <div><strong>类型：</strong>{{ currentOrder.repairTypeName }}</div>
          <div><strong>宿舍：</strong>{{ currentOrder.buildingName }} {{ currentOrder.roomNo }}</div>
          <div style="grid-column: 1 / -1"><strong>标题：</strong>{{ currentOrder.title }}</div>
          <div style="grid-column: 1 / -1"><strong>描述：</strong>{{ currentOrder.description }}</div>
          <div v-if="currentOrder.rejectReason" style="grid-column: 1 / -1"><strong>驳回原因：</strong>{{ currentOrder.rejectReason }}</div>
          <div v-if="currentOrder.resultDesc" style="grid-column: 1 / -1"><strong>维修结果：</strong>{{ currentOrder.resultDesc }}</div>
          <div v-if="currentOrder.materialsUsed" style="grid-column: 1 / -1"><strong>使用材料：</strong>{{ currentOrder.materialsUsed }}</div>
        </div>
        <div style="margin-top: 20px">
          <div class="section-title">现场与维修图片</div>
          <el-empty v-if="!currentOrder.images?.length" description="暂无图片" />
          <div v-else class="image-grid">
            <div v-for="img in currentOrder.images" :key="img.id" class="image-card">
              <div class="image-tag">{{ img.image_type === 'fault' ? '故障图' : '结果图' }}</div>
              <img :src="fileUrl(img.file_path)" alt="工单图片" @click="previewImage(fileUrl(img.file_path))" />
            </div>
          </div>
        </div>
        <div style="margin-top: 20px">
          <div class="section-title">流转记录</div>
          <el-timeline>
            <el-timeline-item v-for="flow in currentOrder.flows || []" :key="flow.id" :timestamp="flow.created_at">{{ flow.operator_name || '系统' }}：{{ flow.remark }}</el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="feedbackDialogVisible" title="提交维修结果" width="560px">
      <el-form :model="feedbackForm" label-width="90px">
        <el-form-item label="维修结果"><el-input v-model="feedbackForm.resultDesc" type="textarea" rows="4" /></el-form-item>
        <el-form-item label="材料"><el-input v-model="feedbackForm.materialsUsed" /></el-form-item>
        <el-form-item label="完成时间"><el-input v-model="feedbackForm.finishTime" placeholder="2026-03-25 18:00:00" /></el-form-item>
        <el-form-item label="维修图片">
          <el-upload :http-request="uploadRepairImage" list-type="picture-card" :limit="3">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feedbackDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFeedback">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="图片预览" width="720px">
      <img v-if="previewSrc" :src="previewSrc" alt="预览图" style="width: 100%; border-radius: 12px" />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from './api'

const baseUrl = 'http://localhost:8080'
const token = ref(localStorage.getItem('admin_token') || '')
const activeTab = ref('orders')
const loginForm = reactive({ username: 'admin', password: '123456' })
const userInfo = reactive({})
const overview = reactive({})
const orders = ref([])
const repairerOrders = ref([])
const buildings = ref([])
const rooms = ref([])
const announcements = ref([])
const users = ref([])
const logs = ref([])
const repairTypeStats = ref([])
const buildingHeatStats = ref([])
const currentOrder = ref(null)
const currentRepairOrder = ref(null)
const orderDialogVisible = ref(false)
const feedbackDialogVisible = ref(false)
const previewVisible = ref(false)
const previewSrc = ref('')
const typeChartRef = ref(null)
const buildingChartRef = ref(null)
let typeChart = null
let buildingChart = null
const orderFilter = reactive({ status: '', keyword: '' })
const repairerFilter = reactive({ status: '' })
const buildingForm = reactive({ buildingName: '', buildingCode: '', floorCount: 6 })
const roomForm = reactive({ buildingId: null, roomNo: '', capacity: 4 })
const announcementForm = reactive({ title: '', content: '' })
const userForm = reactive({ username: '', realName: '', role: 'repairer' })
const feedbackForm = reactive({ resultDesc: '', materialsUsed: '', finishTime: '', imagePaths: [] })

const orderExportColumns = [['orderNo', '工单号'], ['studentName', '学生'], ['repairTypeName', '类型'], ['buildingName', '楼栋'], ['roomNo', '宿舍'], ['status', '状态'], ['title', '标题']]
const repairerExportColumns = [['orderNo', '工单号'], ['studentName', '学生'], ['repairTypeName', '类型'], ['status', '状态'], ['title', '标题']]
const userExportColumns = [['username', '账号'], ['realName', '姓名'], ['role', '角色'], ['status', '状态']]
const logExportColumns = [['moduleName', '模块'], ['operationType', '操作'], ['userName', '用户'], ['operationDesc', '描述']]

const isAdmin = computed(() => userInfo.role === 'admin')
const isDorm = computed(() => ['admin', 'dorm_admin'].includes(userInfo.role))
const isRepairer = computed(() => userInfo.role === 'repairer')
const filteredOrders = computed(() => orders.value.filter((item) => {
  const statusOk = !orderFilter.status || item.status === orderFilter.status
  const keyword = orderFilter.keyword.trim()
  const keywordOk = !keyword || [item.studentName, item.title, item.roomNo, item.orderNo].some((v) => String(v || '').includes(keyword))
  return statusOk && keywordOk
}))
const filteredRepairerOrders = computed(() => repairerOrders.value.filter((item) => !repairerFilter.status || item.status === repairerFilter.status))

async function login() {
  const { data } = await api.post('/auth/login', loginForm)
  localStorage.setItem('admin_token', data.data.token)
  token.value = data.data.token
  await loadMe()
  await refreshAll()
}

async function loadMe() {
  const { data } = await api.get('/auth/me')
  Object.assign(userInfo, data.data)
  activeTab.value = isRepairer.value ? 'repairerOrders' : 'orders'
}

function logout() {
  localStorage.removeItem('admin_token')
  token.value = ''
  Object.keys(userInfo).forEach((key) => delete userInfo[key])
}

async function refreshAll() {
  if (!token.value) return
  if (isDorm.value) {
    orders.value = (await api.get('/dorm-admin/repair-orders')).data.data
    buildings.value = (await api.get('/dorm-admin/buildings')).data.data
    rooms.value = (await api.get('/dorm-admin/rooms')).data.data
    announcements.value = (await api.get('/dorm-admin/announcements')).data.data
  }
  if (isRepairer.value) {
    repairerOrders.value = (await api.get('/repairer/repair-orders')).data.data
  }
  if (isAdmin.value) {
    Object.assign(overview, (await api.get('/admin/statistics/overview')).data.data)
    users.value = (await api.get('/admin/users')).data.data
    logs.value = (await api.get('/admin/logs')).data.data
    repairTypeStats.value = (await api.get('/admin/statistics/repair-type')).data.data
    buildingHeatStats.value = (await api.get('/admin/statistics/building-heat')).data.data
    await nextTick()
    renderCharts()
  }
}

function renderCharts() {
  if (!isAdmin.value) return
  if (typeChartRef.value) {
    typeChart ||= echarts.init(typeChartRef.value)
    typeChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{ type: 'pie', radius: ['40%', '70%'], data: repairTypeStats.value.map((i) => ({ name: i.name, value: i.value })) }]
    })
  }
  if (buildingChartRef.value) {
    buildingChart ||= echarts.init(buildingChartRef.value)
    buildingChart.setOption({
      tooltip: {},
      xAxis: { type: 'category', data: buildingHeatStats.value.map((i) => i.name) },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: buildingHeatStats.value.map((i) => i.value), itemStyle: { color: '#2563eb' } }]
    })
  }
}

function fileUrl(path) {
  return `${baseUrl}${path}`
}

function previewImage(src) {
  previewSrc.value = src
  previewVisible.value = true
}

async function openOrder(row) {
  currentOrder.value = (await api.get(`/dorm-admin/repair-orders/${row.id}`)).data.data
  orderDialogVisible.value = true
}

async function openRepairerOrder(row) {
  currentOrder.value = (await api.get(`/repairer/repair-orders/${row.id}`)).data.data
  orderDialogVisible.value = true
}

async function assign(row) {
  const repairers = (await api.get('/dorm-admin/repairers')).data.data
  const repairer = repairers[0]
  if (!repairer) return ElMessage.error('暂无维修人员')
  await api.post(`/dorm-admin/repair-orders/${row.id}/assign`, { repairerId: repairer.id, remark: `自动分配给${repairer.realName}` })
  ElMessage.success('已分配')
  refreshAll()
}

async function reject(row) {
  await api.post(`/dorm-admin/repair-orders/${row.id}/reject`, { rejectReason: '信息不完整，请补充故障描述' })
  ElMessage.success('已驳回')
  refreshAll()
}

async function acceptOrder(row) {
  await api.post(`/repairer/repair-orders/${row.id}/accept`)
  ElMessage.success('接单成功')
  refreshAll()
}

function openFeedback(row) {
  currentRepairOrder.value = row
  feedbackForm.resultDesc = ''
  feedbackForm.materialsUsed = ''
  feedbackForm.finishTime = new Date().toISOString().slice(0, 19).replace('T', ' ')
  feedbackForm.imagePaths = []
  feedbackDialogVisible.value = true
}

async function uploadRepairImage(option) {
  const formData = new FormData()
  formData.append('file', option.file)
  const { data } = await api.post('/common/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
  feedbackForm.imagePaths.push(data.data.filePath)
  option.onSuccess(data)
}

async function submitFeedback() {
  await api.post(`/repairer/repair-orders/${currentRepairOrder.value.id}/feedback`, feedbackForm)
  ElMessage.success('维修结果已提交')
  feedbackDialogVisible.value = false
  refreshAll()
}

async function createBuilding() {
  await api.post('/dorm-admin/buildings', buildingForm)
  ElMessage.success('楼栋已新增')
  buildingForm.buildingName = ''
  buildingForm.buildingCode = ''
  refreshAll()
}

async function createRoom() {
  await api.post('/dorm-admin/rooms', roomForm)
  ElMessage.success('宿舍已新增')
  roomForm.roomNo = ''
  refreshAll()
}

async function createAnnouncement() {
  await api.post('/dorm-admin/announcements', announcementForm)
  ElMessage.success('公告已发布')
  announcementForm.title = ''
  announcementForm.content = ''
  refreshAll()
}

async function createUser() {
  await api.post('/admin/users', { ...userForm, password: '123456', phone: '13800000000' })
  ElMessage.success('用户已创建，默认密码 123456')
  userForm.username = ''
  userForm.realName = ''
  refreshAll()
}

function exportCsv(fileName, rows, columns) {
  if (!rows?.length) {
    ElMessage.warning('当前没有可导出的数据')
    return
  }
  const header = columns.map(([, label]) => label).join(',')
  const body = rows.map((row) => columns.map(([key]) => `"${String(row[key] ?? '').replaceAll('"', '""')}"`).join(',')).join('\n')
  const csv = `\ufeff${header}\n${body}`
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${fileName}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

function handleResize() {
  typeChart?.resize()
  buildingChart?.resize()
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)
  if (token.value) {
    try {
      await loadMe()
      await refreshAll()
    } catch {
      logout()
    }
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  typeChart?.dispose()
  buildingChart?.dispose()
})

watch(activeTab, async () => {
  await nextTick()
  renderCharts()
})
</script>
