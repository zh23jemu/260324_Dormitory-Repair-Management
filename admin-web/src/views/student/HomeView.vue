<template>
  <div class="portal-home">
    <section class="portal-banner">
      <div class="portal-banner__copy">
        <div class="portal-banner__eyebrow">高校后勤一站式服务平台</div>
        <h1>报修服务平台</h1>
        <p>统一浏览全部报修记录、服务统计、维修排行与公告信息。学生、宿管、维修员、管理员均从同一主页进入系统。</p>
        <div class="portal-banner__cta">
          <el-button type="primary" size="large" @click="goProtected('/student/repair/create')">我要报修</el-button>
          <el-button size="large" @click="$router.push('/login')">角色登录</el-button>
        </div>
      </div>
      <div class="portal-banner__visual">
        <div class="portal-illustration-card">
          <div class="portal-illustration-card__title">统一门户演示入口</div>
          <div class="portal-illustration-card__list">
            <span>全部报修记录公开浏览</span>
            <span>服务统计与维修排行</span>
            <span>公告与报修信息统一展示</span>
            <span>按角色进入不同工作台</span>
          </div>
          <div class="portal-illustration-card__image" aria-hidden="true"></div>
        </div>
      </div>
    </section>

    <section class="portal-main">
      <div class="portal-feed">
        <div class="portal-panel portal-feed__panel">
          <div class="portal-feed__toolbar">
            <div class="portal-filter-row">
              <el-select v-model="selectedBuilding" placeholder="全部报修区域" clearable class="portal-filter-select">
                <el-option label="全部报修区域" value="all" />
                <el-option v-for="item in buildingOptions" :key="item" :label="item" :value="item" />
              </el-select>
              <el-select v-model="selectedType" placeholder="全部报修项目" clearable class="portal-filter-select">
                <el-option label="全部报修项目" value="all" />
                <el-option v-for="item in repairTypeOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </div>
            <el-input v-model="keyword" placeholder="单号/报修人/内容" clearable />
          </div>

          <div v-if="filteredOrders.length" class="portal-order-list">
            <article v-for="item in filteredOrders" :key="item.id" class="portal-order-card portal-order-card--clickable" @click="openOrderDetail(item)">
              <div class="portal-order-card__head">
                <div class="portal-order-card__title-row">
                  <el-tag type="warning" effect="plain">{{ repairOrderStatusText(item.status) }}</el-tag>
                  <strong>{{ orderTitle(item) }}</strong>
                </div>
                <span class="portal-order-card__no">单号：{{ item.orderNo }}</span>
              </div>
              <div class="portal-order-card__meta">
                <span>报修人：{{ item.studentUsername || item.studentName || '未知用户' }}</span>
                <span>时间：{{ item.submittedAt }}</span>
                <span>位置：{{ orderLocation(item) }}</span>
              </div>
              <p class="portal-order-card__desc">{{ item.description || '暂无报修描述' }}</p>
              <div class="portal-order-card__foot">
                <span>类型：{{ item.repairTypeName || '未分类' }}</span>
                <span v-if="item.repairerName">维修员：{{ item.repairerName }}</span>
              </div>
              <div class="portal-order-card__view-hint">点击查看详情</div>
            </article>
          </div>
          <el-empty v-else description="没有匹配的报修记录" />
        </div>
      </div>

      <aside class="portal-sidebar">
        <div class="portal-panel portal-entry-card" @click="goProtected('/student/repair/create')">
          <div class="portal-entry-card__icon">🔧</div>
          <div>
            <strong>我要报修</strong>
            <span v-if="!auth.hasToken()">登录后提交宿舍报修申请</span>
            <span v-else-if="auth.hasRole('student')">进入个人报修中心，快速提交并跟踪宿舍工单</span>
            <span v-else>当前账号可浏览门户信息，学生账号可在此提交报修申请</span>
          </div>
        </div>

        <div class="portal-panel portal-stats-card">
          <div class="portal-side-title">维修服务统计</div>
          <div class="portal-stats-card__block">
            <div class="portal-stats-card__caption">
              <span class="portal-stats-card__caption-icon">📊</span>
              <span>工单处理情况</span>
            </div>
            <div class="portal-kpi-grid">
              <div class="portal-kpi-item">
                <div class="portal-kpi-item__icon">单</div>
                <span>工单总数</span>
                <strong>{{ statistics.totalCount || 0 }}</strong>
              </div>
              <div class="portal-kpi-item">
                <div class="portal-kpi-item__icon is-success">成</div>
                <span>已维修</span>
                <strong class="is-success">{{ statistics.completedCount || 0 }}</strong>
              </div>
              <div class="portal-kpi-item">
                <div class="portal-kpi-item__icon is-warn">修</div>
                <span>维修中</span>
                <strong class="is-warn">{{ statistics.processingCount || 0 }}</strong>
              </div>
            </div>
          </div>
          <div class="portal-stats-card__block">
            <div class="portal-stats-card__caption">
              <span class="portal-stats-card__caption-icon">🏆</span>
              <span>维修单位排行榜</span>
            </div>
            <div class="portal-mini-rank">
              <div v-for="(item, index) in repairerRanking.slice(0, 5)" :key="item.id" class="portal-mini-rank__item">
                <div class="portal-mini-rank__main">
                  <em class="portal-mini-rank__badge" :class="`is-rank-${index + 1}`">TOP {{ index + 1 }}</em>
                  <span>{{ item.realName }}</span>
                </div>
                <strong>{{ item.completedCount || 0 }} 单</strong>
              </div>
            </div>
          </div>
        </div>

        <div class="portal-panel portal-stats-card">
          <div class="portal-side-title">系统功能</div>
          <div class="portal-feature-list">
            <div class="portal-feature-list__item">
              <strong>公告浏览</strong>
              <span>首页直接查看发布时间与发布者</span>
            </div>
            <div class="portal-feature-list__item">
              <strong>服务透明</strong>
              <span>展示历史报修记录、评分和维修排行</span>
            </div>
          </div>
        </div>
      </aside>
    </section>

    <section class="portal-bottom">
      <div class="portal-panel">
        <div class="portal-section-title">公告信息</div>
        <div v-if="announcements.length" class="portal-announcement-list">
          <article
            v-for="item in announcements.slice(0, 4)"
            :key="item.id"
            :class="['portal-announcement-card', { 'portal-announcement-card--with-image': !!item.imagePath }]"
          >
            <img v-if="item.imagePath" :src="fileUrl(item.imagePath)" alt="公告配图" />
            <div class="portal-announcement-card__content">
              <strong>{{ item.title }}</strong>
              <p>{{ item.content }}</p>
              <span>发布者：{{ item.publisherUsername || item.publisherName || '系统' }} · 发布时间：{{ item.publishedAt || item.createdAt }}</span>
            </div>
          </article>
        </div>
        <el-empty v-else description="暂无公告" />
      </div>
    </section>

    <el-dialog v-model="orderDialogVisible" title="工单详情" width="780px" class="portal-order-dialog">
      <div v-if="currentOrder" class="portal-order-detail">
        <div class="portal-order-detail__eyebrow">基础信息</div>
        <div class="portal-order-detail__grid">
          <div><span>工单号</span><strong>{{ currentOrder.orderNo }}</strong></div>
          <div><span>报修类型</span><strong>{{ currentOrder.repairTypeName || '未分类' }}</strong></div>
          <div><span>报修位置</span><strong>{{ orderLocation(currentOrder) }}</strong></div>
          <div><span>关联设施</span><strong>{{ facilityText(currentOrder) }}</strong></div>
          <div><span>期望时间</span><strong>{{ currentOrder.expectTime || '未填写' }}</strong></div>
          <div><span>维修员</span><strong>{{ currentOrder.repairerName || '待分配' }}</strong></div>
          <div class="portal-order-detail__grid--full"><span>维修结果</span><strong>{{ currentOrder.resultDesc || '暂无维修结果' }}</strong></div>
        </div>

        <div class="portal-order-detail__eyebrow">报修内容</div>
        <div class="portal-order-detail__block">
          <div class="portal-order-detail__block-title">描述</div>
          <p>{{ currentOrder.description || '暂无描述' }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../../api'
import { useAuth } from '../../utils/auth'
import { fileUrl } from '../../utils/file'
import { repairOrderStatusText } from '../../utils/status'

const router = useRouter()
const auth = useAuth()
const announcements = ref([])
const orders = ref([])
const repairerRanking = ref([])
const statistics = ref({})
const orderDialogVisible = ref(false)
const currentOrder = ref(null)
const keyword = ref('')
const selectedBuilding = ref('all')
const selectedType = ref('all')

const buildingOptions = computed(() => Array.from(new Set((orders.value || []).map((item) => String(item.buildingName || '').trim()).filter(Boolean))))
const repairTypeOptions = computed(() => Array.from(new Set((orders.value || []).map((item) => String(item.repairTypeName || '').trim()).filter(Boolean))))

const filteredOrders = computed(() => {
  return (orders.value || []).filter((item) => {
    const buildingMatch = selectedBuilding.value === 'all' || !selectedBuilding.value || item.buildingName === selectedBuilding.value
    const typeMatch = selectedType.value === 'all' || !selectedType.value || item.repairTypeName === selectedType.value
    const searchText = `${item.orderNo || ''} ${item.studentUsername || ''} ${item.studentName || ''} ${item.title || ''} ${item.description || ''}`.toLowerCase()
    const keywordMatch = !keyword.value.trim() || searchText.includes(keyword.value.trim().toLowerCase())
    return buildingMatch && typeMatch && keywordMatch
  })
})

async function loadPortal() {
  const { data } = await api.get('/portal/home')
  announcements.value = data.data.announcements || []
  orders.value = data.data.orders || []
  repairerRanking.value = data.data.repairerRanking || []
  statistics.value = data.data.statistics || {}
}

function goProtected(path) {
  if (!auth.hasToken()) {
    router.push(`/login?redirect=${encodeURIComponent(path)}&role=student`)
    return
  }
  if (!auth.hasRole('student')) {
    ElMessage.warning('当前账号请进入对应工作台操作')
    router.push(auth.getRoleHomePath())
    return
  }
  router.push(path)
}

function orderTitle(item) {
  const prefix = item.buildingName ? `【${item.buildingName}】` : ''
  const typeName = item.repairTypeName || '综合报修'
  return `${prefix}${typeName}`
}

function orderLocation(item) {
  return [item.buildingName, item.roomNo].filter(Boolean).join(' / ') || '未填写位置'
}

function facilityText(item) {
  const name = item.facilityName || ''
  const type = item.facilityType || ''
  if (!name && !type) return '未关联设施'
  if (name && type) return `${name}（${type}）`
  return name || type || '未关联设施'
}

async function openOrderDetail(item) {
  orderDialogVisible.value = true
  currentOrder.value = { ...item }

  // 先直接展示首页已有的工单摘要，避免详情接口在后端未重启或短暂异常时直接弹错。
  // 如果公开详情接口可用，再静默补充处理结果、评价等更完整字段。
  try {
    const response = await fetch(`${api.defaults.baseURL}/portal/repair-orders/${item.id}`)
    const payload = await response.json()
    if (response.ok && payload?.code === 200 && payload?.data) {
      currentOrder.value = { ...currentOrder.value, ...payload.data }
    }
  } catch (error) {
    // 只保留基础信息，不再打断用户查看首页工单。
  }
}

onMounted(loadPortal)
</script>

<style scoped>
.portal-order-card--clickable {
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.portal-order-card--clickable:hover {
  transform: translateY(-3px);
  border-color: rgba(37, 99, 235, 0.28);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.12);
}

.portal-order-card__view-hint {
  margin-top: 12px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
}

.portal-order-detail {
  display: grid;
  gap: 18px;
}

.portal-order-detail__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(14, 116, 144, 0.1), rgba(37, 99, 235, 0.08));
}

.portal-order-detail__header h3 {
  margin: 6px 0 0;
  color: #0f172a;
  font-size: 22px;
}

.portal-order-detail__eyebrow {
  color: #2563eb;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.portal-order-detail__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.portal-order-detail__grid > div,
.portal-order-detail__block {
  padding: 14px 16px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 16px;
  background: rgba(248, 250, 252, 0.9);
}

.portal-order-detail__grid span {
  display: block;
  margin-bottom: 6px;
  color: #64748b;
  font-size: 13px;
}

.portal-order-detail__grid strong {
  color: #0f172a;
}

.portal-order-detail__block-title {
  margin-bottom: 8px;
  color: #0f172a;
  font-weight: 800;
}

.portal-order-detail__block p {
  margin: 0;
  color: #334155;
  line-height: 1.8;
}

@media (max-width: 720px) {
  .portal-order-detail__header,
  .portal-order-detail__grid {
    grid-template-columns: 1fr;
  }

  .portal-order-detail__header {
    display: grid;
  }
}
</style>
