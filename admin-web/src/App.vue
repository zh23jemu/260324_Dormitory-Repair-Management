<template>
  <div v-if="!token" class="login-wrap">
    <el-card class="login-card">
      <template #header>
        <div>
          <div style="font-size:24px;font-weight:700">宿舍报修管理端</div>
          <div style="color:#6b7280;margin-top:6px">宿管、维修人员、管理员使用</div>
        </div>
      </template>
      <el-form :model="loginForm" @submit.prevent>
        <el-form-item label="账号"><el-input v-model="loginForm.username" placeholder="admin / dorm01 / repair01" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="loginForm.password" type="password" show-password placeholder="123456" /></el-form-item>
        <el-button type="primary" style="width:100%" @click="login">登录</el-button>
      </el-form>
    </el-card>
  </div>

  <div v-else class="page-shell">
    <div class="toolbar">
      <div>
        <div style="font-size:28px;font-weight:700">高校宿舍报修管理系统</div>
        <div style="color:#6b7280;margin-top:4px">当前用户：{{ userInfo.realName }} / {{ userInfo.role }}</div>
      </div>
      <div>
        <el-button @click="refreshAll">刷新</el-button>
        <el-button type="danger" plain @click="logout">退出</el-button>
      </div>
    </div>

    <div v-if="isAdmin">
      <div class="stat-grid stats-six">
        <div class="stat-card"><div>总工单</div><h2>{{ overview.totalRepairCount || 0 }}</h2></div>
        <div class="stat-card"><div>待审核</div><h2>{{ overview.pendingReviewCount || 0 }}</h2></div>
        <div class="stat-card"><div>处理中</div><h2>{{ overview.processingCount || 0 }}</h2></div>
        <div class="stat-card"><div>待评价</div><h2>{{ overview.pendingRatingCount || 0 }}</h2></div>
        <div class="stat-card"><div>完成率</div><h2>{{ Number(overview.completionRate || 0).toFixed(1) }}%</h2></div>
        <div class="stat-card"><div>满意度</div><h2>{{ Number(overview.satisfactionRate || 0).toFixed(1) }}%</h2></div>
      </div>
      <div class="grid-two" style="margin-bottom:16px">
        <el-card><template #header>报修类型统计</template><div ref="typeChartRef" class="chart-box"></div></el-card>
        <el-card><template #header>楼栋报修热度</template><div ref="buildingChartRef" class="chart-box"></div></el-card>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane v-if="isDorm" label="工单管理" name="orders">
        <div class="toolbar" style="margin-bottom:12px">
          <div style="display:flex;gap:10px;align-items:center;flex-wrap:wrap">
            <div style="font-weight:700">工单列表</div>
            <el-select v-model="orderFilter.status" placeholder="状态筛选" clearable style="width:160px">
              <el-option label="待审核" value="pending_review" />
              <el-option label="已驳回" value="rejected" />
              <el-option label="待接单" value="pending_accept" />
              <el-option label="处理中" value="processing" />
              <el-option label="待评价" value="pending_rating" />
              <el-option label="已完成" value="completed" />
            </el-select>
            <el-input v-model="orderFilter.keyword" placeholder="学生/标题/宿舍" style="width:220px" clearable />
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
        <div class="toolbar" style="margin-bottom:12px">
          <div style="display:flex;gap:10px;align-items:center;flex-wrap:wrap">
            <div style="font-weight:700">维修任务</div>
            <el-select v-model="repairerFilter.status" placeholder="状态筛选" clearable style="width:160px">
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

      <el-tab-pane v-if="isRepairer" label="个人统计" name="repairerStats">
        <div class="toolbar" style="margin-bottom:12px">
          <el-date-picker v-model="repairerStatsRange" type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
          <el-button type="primary" @click="loadRepairerStats">查询统计</el-button>
        </div>
        <div class="stat-grid">
          <div class="stat-card"><div>累计工单</div><h2>{{ repairerStats.totalCount || 0 }}</h2></div>
          <div class="stat-card"><div>已完成</div><h2>{{ repairerStats.completedCount || 0 }}</h2></div>
          <div class="stat-card"><div>待接单</div><h2>{{ repairerStats.pendingCount || 0 }}</h2></div>
          <div class="stat-card"><div>处理中</div><h2>{{ repairerStats.processingCount || 0 }}</h2></div>
        </div>
        <div class="stat-grid">
          <div class="stat-card"><div>完成率</div><h2>{{ Number(repairerStats.completionRate || 0).toFixed(1) }}%</h2></div>
          <div class="stat-card"><div>平均处理时长</div><h2>{{ Number(repairerStats.avgHandleHours || 0).toFixed(2) }}h</h2></div>
        </div>
      </el-tab-pane>

      <el-tab-pane v-if="isDorm" label="宿舍管理" name="dorms">
        <div class="grid-two">
          <el-card>
            <template #header>楼栋与宿舍</template>
            <el-form :model="buildingForm" inline>
              <el-form-item><el-input v-model="buildingForm.buildingName" placeholder="楼栋名称" /></el-form-item>
              <el-form-item><el-input v-model="buildingForm.buildingCode" placeholder="楼栋编码" /></el-form-item>
              <el-form-item><el-input-number v-model="buildingForm.floorCount" :min="1" /></el-form-item>
              <el-form-item><el-button type="primary" @click="createBuilding">新增楼栋</el-button></el-form-item>
            </el-form>
            <el-divider />
            <el-form :model="roomForm" inline>
              <el-form-item><el-select v-model="roomForm.buildingId" placeholder="楼栋" style="width:120px"><el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" /></el-select></el-form-item>
              <el-form-item><el-input v-model="roomForm.roomNo" placeholder="宿舍号" /></el-form-item>
              <el-form-item><el-input-number v-model="roomForm.capacity" :min="1" /></el-form-item>
              <el-form-item><el-input v-model="roomForm.facilityDesc" placeholder="设施配置" style="width:220px" /></el-form-item>
              <el-form-item><el-button type="primary" @click="createRoom">新增宿舍</el-button></el-form-item>
            </el-form>
            <el-table :data="rooms" size="small" style="margin-top:12px">
              <el-table-column prop="buildingName" label="楼栋" width="90" />
              <el-table-column prop="roomNo" label="宿舍" width="90" />
              <el-table-column prop="capacity" label="容量" width="70" />
              <el-table-column prop="facilityDesc" label="设施配置" min-width="180" />
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button size="small" @click="editRoom(scope.row)">编辑</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>

          <el-card>
            <template #header>入住学生维护</template>
            <el-table :data="students" size="small">
              <el-table-column prop="studentNo" label="学号" width="100" />
              <el-table-column prop="realName" label="姓名" width="90" />
              <el-table-column prop="buildingName" label="楼栋" width="90" />
              <el-table-column prop="roomNo" label="宿舍" width="90" />
              <el-table-column prop="bedNo" label="床位" width="80" />
              <el-table-column prop="college" label="学院" min-width="120" />
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button size="small" @click="editStudentRoom(scope.row)">调整</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane v-if="isDorm" label="公告管理" name="announcements">
        <el-form :model="announcementForm" inline>
          <el-form-item><el-input v-model="announcementForm.title" placeholder="公告标题" style="width:240px" /></el-form-item>
          <el-form-item><el-input v-model="announcementForm.content" placeholder="公告内容" style="width:380px" /></el-form-item>
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
              <el-form-item><el-select v-model="userForm.role" placeholder="角色" style="width:140px"><el-option label="宿管" value="dorm_admin" /><el-option label="维修" value="repairer" /><el-option label="管理员" value="admin" /></el-select></el-form-item>
              <el-form-item v-if="userForm.role === 'repairer'"><el-select v-model="userForm.workTypeCode" placeholder="维修工种" style="width:160px"><el-option v-for="item in workTypeOptions" :key="item.dictCode" :label="item.dictName" :value="item.dictCode" /></el-select></el-form-item>
              <el-form-item><el-button type="primary" @click="createUser">新增用户</el-button></el-form-item>
            </el-form>
            <el-table :data="users" size="small">
              <el-table-column prop="username" label="账号" />
              <el-table-column prop="realName" label="姓名" />
              <el-table-column prop="role" label="角色" />
              <el-table-column prop="workTypeName" label="工种" />
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

      <el-tab-pane v-if="isAdmin" label="基础配置" name="config">
        <div class="grid-two">
          <el-card>
            <template #header><div class="toolbar" style="margin-bottom:0"><span>报修类型</span><el-button size="small" type="primary" @click="editRepairType()">新增</el-button></div></template>
            <el-table :data="repairTypes" size="small">
              <el-table-column prop="typeName" label="名称" />
              <el-table-column prop="sortNo" label="排序" width="80" />
              <el-table-column prop="status" label="状态" width="90" />
              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button size="small" @click="editRepairType(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" plain @click="removeRepairType(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
          <el-card>
            <template #header>
              <div class="toolbar" style="margin-bottom:0">
                <span>字典配置</span>
                <div style="display:flex;gap:10px;align-items:center">
                  <el-select v-model="dictFilterType" style="width:160px">
                    <el-option label="全部字典" value="" />
                    <el-option label="维修工种" value="repair_work_type" />
                    <el-option label="评价指标" value="rating_indicator" />
                    <el-option label="报修优先级" value="repair_priority" />
                    <el-option label="公告类型" value="announcement_type" />
                  </el-select>
                  <el-button size="small" type="primary" @click="editDict()">新增</el-button>
                </div>
              </div>
            </template>
            <el-table :data="filteredDicts" size="small">
              <el-table-column prop="dictType" label="类型" width="160" />
              <el-table-column prop="dictCode" label="编码" width="130" />
              <el-table-column prop="dictName" label="名称" />
              <el-table-column prop="sortNo" label="排序" width="80" />
              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button size="small" @click="editDict(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" plain @click="removeDict(scope.row)">删除</el-button>
                </template>
              </el-table-column>
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
          <div><strong>期望时间：</strong>{{ currentOrder.expectTime || '未填写' }}</div>
          <div style="grid-column:1 / -1"><strong>标题：</strong>{{ currentOrder.title }}</div>
          <div style="grid-column:1 / -1"><strong>描述：</strong>{{ currentOrder.description }}</div>
          <div v-if="currentOrder.rejectReason" style="grid-column:1 / -1"><strong>驳回原因：</strong>{{ currentOrder.rejectReason }}</div>
          <div v-if="currentOrder.resultDesc" style="grid-column:1 / -1"><strong>维修结果：</strong>{{ currentOrder.resultDesc }}</div>
          <div v-if="currentOrder.materialsUsed" style="grid-column:1 / -1"><strong>使用材料：</strong>{{ currentOrder.materialsUsed }}</div>
          <div v-if="currentOrder.rating" style="grid-column:1 / -1"><strong>学生评分：</strong>{{ currentOrder.rating.score }} 星 / {{ currentOrder.rating.content || '未填写评价内容' }}</div>
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
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from './api'

const token = ref(localStorage.getItem('admin_token') || '')
const activeTab = ref('orders')
const loginForm = reactive({ username: 'admin', password: '123456' })
const userInfo = reactive({})
const overview = reactive({})
const orders = ref([])
const repairerOrders = ref([])
const buildings = ref([])
const rooms = ref([])
const students = ref([])
const announcements = ref([])
const users = ref([])
const logs = ref([])
const dicts = ref([])
const repairTypes = ref([])
const repairTypeStats = ref([])
const buildingHeatStats = ref([])
const repairerStats = reactive({})
const repairerStatsRange = ref([])
const currentOrder = ref(null)
const currentRepairOrder = ref(null)
const orderDialogVisible = ref(false)
const feedbackDialogVisible = ref(false)
const typeChartRef = ref(null)
const buildingChartRef = ref(null)
let typeChart = null
let buildingChart = null

const orderFilter = reactive({ status: '', keyword: '' })
const repairerFilter = reactive({ status: '' })
const dictFilterType = ref('')
const buildingForm = reactive({ buildingName: '', buildingCode: '', floorCount: 6 })
const roomForm = reactive({ buildingId: null, roomNo: '', capacity: 4, facilityDesc: '' })
const announcementForm = reactive({ title: '', content: '' })
const userForm = reactive({ username: '', realName: '', role: 'repairer', workTypeCode: '' })
const feedbackForm = reactive({ resultDesc: '', materialsUsed: '', finishTime: '', imagePaths: [] })

const orderExportColumns = [['orderNo', '工单号'], ['studentName', '学生'], ['repairTypeName', '类型'], ['buildingName', '楼栋'], ['roomNo', '宿舍'], ['status', '状态'], ['title', '标题']]
const repairerExportColumns = [['orderNo', '工单号'], ['studentName', '学生'], ['repairTypeName', '类型'], ['status', '状态'], ['title', '标题']]
const userExportColumns = [['username', '账号'], ['realName', '姓名'], ['role', '角色'], ['workTypeName', '工种'], ['status', '状态']]
const logExportColumns = [['moduleName', '模块'], ['operationType', '操作'], ['userName', '用户'], ['operationDesc', '描述']]

const isAdmin = computed(() => userInfo.role === 'admin')
const isDorm = computed(() => ['admin', 'dorm_admin'].includes(userInfo.role))
const isRepairer = computed(() => userInfo.role === 'repairer')
const filteredOrders = computed(() => orders.value.filter((item) => (!orderFilter.status || item.status === orderFilter.status) && (!orderFilter.keyword.trim() || [item.studentName, item.title, item.roomNo, item.orderNo].some((v) => String(v || '').includes(orderFilter.keyword.trim())))))
const filteredRepairerOrders = computed(() => repairerOrders.value.filter((item) => !repairerFilter.status || item.status === repairerFilter.status))
const filteredDicts = computed(() => dicts.value.filter((item) => !dictFilterType.value || item.dictType === dictFilterType.value))
const workTypeOptions = computed(() => dicts.value.filter((item) => item.dictType === 'repair_work_type'))

async function login() {
  const { data } = await api.post('/auth/login', loginForm)
  localStorage.setItem('admin_token', data.data.token)
  token.value = data.data.token
  await loadMe()
  await refreshAll()
}

async function loadMe() {
  Object.assign(userInfo, (await api.get('/auth/me')).data.data)
  activeTab.value = isRepairer.value ? 'repairerOrders' : 'orders'
}

function logout() {
  localStorage.removeItem('admin_token')
  token.value = ''
  Object.keys(userInfo).forEach((key) => delete userInfo[key])
}

async function refreshAll() {
  if (isDorm.value) {
    orders.value = (await api.get('/dorm-admin/repair-orders')).data.data
    buildings.value = (await api.get('/dorm-admin/buildings')).data.data
    rooms.value = (await api.get('/dorm-admin/rooms')).data.data
    students.value = (await api.get('/dorm-admin/students')).data.data
    announcements.value = (await api.get('/dorm-admin/announcements')).data.data
  }
  if (isRepairer.value) {
    repairerOrders.value = (await api.get('/repairer/repair-orders')).data.data
    await loadRepairerStats()
  }
  if (isAdmin.value) {
    Object.assign(overview, (await api.get('/admin/statistics/overview')).data.data)
    users.value = (await api.get('/admin/users')).data.data
    logs.value = (await api.get('/admin/logs')).data.data
    dicts.value = (await api.get('/admin/dicts')).data.data
    repairTypes.value = (await api.get('/admin/repair-types')).data.data
    repairTypeStats.value = (await api.get('/admin/statistics/repair-type')).data.data
    buildingHeatStats.value = (await api.get('/admin/statistics/building-heat')).data.data
    await nextTick()
    renderCharts()
  }
}

async function loadRepairerStats() {
  const params = repairerStatsRange.value?.length === 2 ? { dateFrom: repairerStatsRange.value[0], dateTo: repairerStatsRange.value[1] } : {}
  Object.assign(repairerStats, (await api.get('/repairer/statistics', { params })).data.data)
}

function renderCharts() {
  if (!isAdmin.value) return
  if (typeChartRef.value) {
    typeChart ||= echarts.init(typeChartRef.value)
    typeChart.setOption({ tooltip: { trigger: 'item' }, series: [{ type: 'pie', radius: ['40%', '70%'], data: repairTypeStats.value.map((i) => ({ name: i.name, value: i.value })) }] })
  }
  if (buildingChartRef.value) {
    buildingChart ||= echarts.init(buildingChartRef.value)
    buildingChart.setOption({ tooltip: {}, xAxis: { type: 'category', data: buildingHeatStats.value.map((i) => i.name) }, yAxis: { type: 'value' }, series: [{ type: 'bar', data: buildingHeatStats.value.map((i) => i.value), itemStyle: { color: '#2563eb' } }] })
  }
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
  const repairer = (await api.get('/dorm-admin/repairers')).data.data[0]
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
  buildingForm.buildingName = ''
  buildingForm.buildingCode = ''
  ElMessage.success('楼栋已新增')
  refreshAll()
}

async function createRoom() {
  await api.post('/dorm-admin/rooms', roomForm)
  roomForm.roomNo = ''
  roomForm.facilityDesc = ''
  ElMessage.success('宿舍已新增')
  refreshAll()
}

async function editRoom(row) {
  const facilityDesc = window.prompt('请输入设施配置', row.facilityDesc || '')
  if (facilityDesc === null) return
  await api.put(`/dorm-admin/rooms/${row.id}`, { buildingId: row.buildingId, roomNo: row.roomNo, capacity: row.capacity, facilityDesc, status: row.status || 'enabled', remark: row.remark || '' })
  ElMessage.success('宿舍已更新')
  refreshAll()
}

async function editStudentRoom(row) {
  const roomInput = window.prompt('请输入新的宿舍号或宿舍ID', row.roomNo || row.roomId || '')
  if (roomInput === null) return
  const bedNo = window.prompt('请输入床位号', row.bedNo || '')
  if (bedNo === null) return
  const normalizedInput = String(roomInput).trim()
  const matchedByRoomNo = rooms.value.filter((item) => String(item.roomNo) === normalizedInput)
  let targetRoom = null
  if (matchedByRoomNo.length === 1) {
    targetRoom = matchedByRoomNo[0]
  } else if (matchedByRoomNo.length > 1) {
    targetRoom = matchedByRoomNo.find((item) => String(item.buildingId) === String(row.buildingId)) || null
    if (!targetRoom) {
      return ElMessage.error('存在多个同名宿舍号，请改为输入宿舍ID')
    }
  } else {
    targetRoom = rooms.value.find((item) => String(item.id) === normalizedInput) || null
  }
  if (!targetRoom) return ElMessage.error('宿舍不存在')
  await api.put(`/dorm-admin/students/${row.id}/room`, { buildingId: targetRoom.buildingId, roomId: Number(targetRoom.id), bedNo })
  ElMessage.success('入住信息已更新')
  refreshAll()
}

async function createAnnouncement() {
  await api.post('/dorm-admin/announcements', announcementForm)
  announcementForm.title = ''
  announcementForm.content = ''
  ElMessage.success('公告已发布')
  refreshAll()
}

async function createUser() {
  await api.post('/admin/users', { ...userForm, password: '123456', phone: '13800000000' })
  userForm.username = ''
  userForm.realName = ''
  userForm.workTypeCode = ''
  ElMessage.success('用户已创建，默认密码 123456')
  refreshAll()
}

async function editDict(row) {
  const dictType = window.prompt('字典类型', row?.dictType || 'repair_work_type')
  if (!dictType) return
  const dictCode = window.prompt('字典编码', row?.dictCode || '')
  if (!dictCode) return
  const dictName = window.prompt('字典名称', row?.dictName || '')
  if (!dictName) return
  const sortNo = window.prompt('排序号', row?.sortNo || '1')
  if (!sortNo) return
  const status = window.prompt('状态(enabled/disabled)', row?.status || 'enabled')
  const payload = { dictType, dictCode, dictName, sortNo: Number(sortNo), status: status || 'enabled' }
  if (row?.id) await api.put(`/admin/dicts/${row.id}`, payload)
  else await api.post('/admin/dicts', payload)
  ElMessage.success('字典配置已保存')
  refreshAll()
}

async function removeDict(row) {
  if (!window.confirm(`确认删除字典项 ${row.dictName} 吗？`)) return
  await api.delete(`/admin/dicts/${row.id}`)
  ElMessage.success('字典项已删除')
  refreshAll()
}

async function editRepairType(row) {
  const typeName = window.prompt('报修类型名称', row?.typeName || '')
  if (!typeName) return
  const sortNo = window.prompt('排序号', row?.sortNo || '1')
  if (!sortNo) return
  const status = window.prompt('状态(enabled/disabled)', row?.status || 'enabled')
  const payload = { typeName, sortNo: Number(sortNo), status: status || 'enabled' }
  if (row?.id) await api.put(`/admin/repair-types/${row.id}`, payload)
  else await api.post('/admin/repair-types', payload)
  ElMessage.success('报修类型已保存')
  refreshAll()
}

async function removeRepairType(row) {
  if (!window.confirm(`确认删除报修类型 ${row.typeName} 吗？`)) return
  await api.delete(`/admin/repair-types/${row.id}`)
  ElMessage.success('报修类型已删除')
  refreshAll()
}

function exportCsv(fileName, rows, columns) {
  if (!rows?.length) return ElMessage.warning('当前没有可导出的数据')
  const header = columns.map(([, label]) => label).join(',')
  const body = rows.map((row) => columns.map(([key]) => `\"${String(row[key] ?? '').replaceAll('\"', '\"\"')}\"`).join(',')).join('\\n')
  const blob = new Blob([`\\ufeff${header}\\n${body}`], { type: 'text/csv;charset=utf-8;' })
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
