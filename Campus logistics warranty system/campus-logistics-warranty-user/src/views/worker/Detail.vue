<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserById } from '@/api/user'
import { getRepairRecordByWorkerId } from '@/api/repairRecord'
import { getRepairRecordRatingList } from '@/api/repairRecordRating'

const route = useRoute()
const router = useRouter()

// 维修工ID
const workerId = computed(() => route.params.id)

// 维修工信息
const workerInfo = ref({})

// 维修记录数据
const repairRecords = ref([])

// 评价数据
const ratings = ref([])

// 加载状态
const loading = ref(false)
const ratingLoading = ref(false)

// 模式切换 (维修记录/评价)
const activeTab = ref('records')

// 获取维修工信息
const fetchWorkerInfo = async () => {
  loading.value = true
  try {
    const res = await getUserById(workerId.value)
    if (res.code === 200) {
      workerInfo.value = res.data || {}
    } else {
      ElMessage.error(res.msg || '获取维修工信息失败')
    }
  } catch (error) {
    console.error('获取维修工信息失败', error)
    ElMessage.error('获取维修工信息失败')
  } finally {
    loading.value = false
  }
}

// 获取维修记录
const fetchRepairRecords = async () => {
  loading.value = true
  try {
    const res = await getRepairRecordByWorkerId(workerId.value)
    if (res.code === 200) {
      repairRecords.value = res.data || []
    } else {
      ElMessage.error(res.msg || '获取维修记录失败')
    }
  } catch (error) {
    console.error('获取维修记录失败', error)
    ElMessage.error('获取维修记录失败')
  } finally {
    loading.value = false
  }
}

// 获取评价数据
const fetchRatings = async () => {
  ratingLoading.value = true
  try {
    // 获取所有维修记录的评分
    const ratingPromises = repairRecords.value.map(record => 
      getRepairRecordRatingList({
        repairRecordId: record.id
      })
    )
    
    const ratingResults = await Promise.all(ratingPromises)
    // 合并所有评分数据
    ratings.value = ratingResults
      .filter(res => res.code === 200 && res.data.records)
      .flatMap(res => res.data.records)
  } catch (error) {
    console.error('获取评价数据失败', error)
    ElMessage.error('获取评价数据失败')
  } finally {
    ratingLoading.value = false
  }
}

// 返回列表页
const goBack = () => {
  router.push({ name: 'workerList' })
}

// 查看维修记录详情
const viewRepairDetail = (recordId) => {
  router.push({
    name: 'repairDetail',
    params: { id: recordId }
  })
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取维修记录状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case '待分配':
      return 'info'
    case '处理中':
      return 'warning'
    case '已完成':
      return 'success'
    case '已取消':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取维修记录状态文本
const getStatusText = (status) => {
  return status || '未知状态'
}

// 页面初始化时加载数据
onMounted(async () => {
  await fetchWorkerInfo()
  await fetchRepairRecords()
  await fetchRatings()
})
</script>

<template>
  <div class="worker-detail-container">
    <!-- 顶部信息卡片 -->
    <el-card v-loading="loading" class="worker-info-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button @click="goBack" type="primary" plain size="small">
              <el-icon><Back /></el-icon>
              返回列表
            </el-button>
            <span class="title">维修工详情</span>
          </div>
        </div>
      </template>

      <el-row :gutter="20" class="worker-profile">
        <el-col :span="6" class="avatar-col">
          <el-avatar :size="100" :src="workerInfo.avatar" class="worker-avatar">
            {{ workerInfo.name ? workerInfo.name.charAt(0) : 'W' }}
          </el-avatar>
        </el-col>
        <el-col :span="18">
          <div class="worker-header">
            <h1 class="worker-name">{{ workerInfo.name || '未知' }}
              <el-tag :type="workerInfo.status === 1 ? 'success' : 'info'" effect="dark" class="status-tag">
                {{ workerInfo.status === 1 ? '在线' : '离线' }}
              </el-tag>
            </h1>
          </div>
          
          <div class="worker-info-grid">
            <div class="info-item">
              <el-icon><Phone /></el-icon>
              <span class="info-label">联系电话:</span>
              <span class="info-value">{{ workerInfo.phone || '-' }}</span>
            </div>
            
            <div class="info-item">
              <el-icon><Message /></el-icon>
              <span class="info-label">邮箱:</span>
              <span class="info-value">{{ workerInfo.email || '-' }}</span>
            </div>
            
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span class="info-label">用户名:</span>
              <span class="info-value">{{ workerInfo.username || '-' }}</span>
            </div>
            
            <div class="info-item">
              <el-icon><Male v-if="workerInfo.sex === '男'" /><Female v-else /></el-icon>
              <span class="info-label">性别:</span>
              <span class="info-value">{{ workerInfo.sex || '-' }}</span>
            </div>
            
            <div class="info-item">
              <el-icon><Calendar /></el-icon>
              <span class="info-label">注册时间:</span>
              <span class="info-value">{{ formatDateTime(workerInfo.createTime) }}</span>
            </div>
          </div>
          
          <div v-if="workerInfo.tags" class="worker-tags-section">
            <div class="tags-header">
              <el-icon><PriceTag /></el-icon>
              <span>擅长领域</span>
            </div>
            <div class="tags-list">
              <el-tag 
                v-for="(tag, index) in workerInfo.tags.split(',')" 
                :key="index"
                type="success"
                effect="light"
                size="large"
              >
                <el-icon><Tools /></el-icon>
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 分隔线 -->
    <div class="spacer"></div>

    <!-- 记录和评价切换卡 -->
    <el-card class="records-card">
      <template #header>
        <div class="tab-header">
          <el-radio-group v-model="activeTab" size="large">
            <el-radio-button label="records">维修记录</el-radio-button>
            <el-radio-button label="ratings">用户评价</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 维修记录 -->
      <div v-if="activeTab === 'records'" v-loading="loading">
        <el-empty v-if="repairRecords.length === 0" description="暂无维修记录" />
        
        <div v-else class="records-list">
          <el-timeline>
            <el-timeline-item
              v-for="record in repairRecords"
              :key="record.id"
              :type="getStatusTagType(record.status)"
              :hollow="record.status !== '已完成'"
              size="large"
            >
              <template #dot>
                <el-icon v-if="record.status === '待分配'"><MoreFilled /></el-icon>
                <el-icon v-else-if="record.status === '处理中'"><Tools /></el-icon>
                <el-icon v-else-if="record.status === '已完成'"><Select /></el-icon>
                <el-icon v-else><Close /></el-icon>
              </template>
              
              <el-card class="record-card" shadow="hover" >
                <div class="record-header">
                  <div class="record-title">
                    <span class="repair-item">{{ record.name || '未指定维修项目' }}</span>
                    <el-tag size="small" :type="getStatusTagType(record.status)">
                      {{ getStatusText(record.status) }}
                    </el-tag>
                  </div>
                  <div class="record-time">{{ formatDateTime(record.createTime) }}</div>
                </div>
                
                <div class="record-content">
                  <div class="record-description">
                    <p>{{ record.description || '无维修描述' }}</p>
                  </div>
                  
                  <div v-if="record.location" class="record-location">
                    <el-icon><Location /></el-icon>
                    <span>{{ record.location }}</span>
                  </div>
                </div>
                
                <div v-if="record.remark" class="record-remark">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>维修备注: {{ record.remark }}</span>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>

      <!-- 用户评价 -->
      <div v-else v-loading="ratingLoading">
        <el-empty v-if="ratings.length === 0" description="暂无用户评价" />
        
        <div v-else class="ratings-list">
          <el-card 
            v-for="rating in ratings" 
            :key="rating.id" 
            class="rating-card"
            shadow="hover"
          >
            <div class="rating-header">
              <div class="rating-info">
                <div class="rating-score">
                  <el-rate
                    v-model="rating.rating"
                    disabled
                    show-score
                    text-color="#ff9900"
                    score-template="{value}"
                  />
                </div>
                <div class="repair-record-name">
                  <el-tag size="small" type="info">维修项目：{{ rating.repairRecordName || '未知项目' }}</el-tag>
                </div>
              </div>
              <div class="rating-time">{{ formatDateTime(rating.createTime) }}</div>
            </div>
            
            <div class="rating-content">
              <p>{{ rating.comment || '用户未留下评价内容' }}</p>
            </div>
            
            <div class="user-info">
              <el-avatar :size="24">{{ rating.userName ? rating.userName.charAt(0) : 'U' }}</el-avatar>
              <span>{{ rating.userName || '匿名用户' }}</span>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.worker-detail-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.worker-profile {
  display: flex;
  margin-top: 10px;
}

.avatar-col {
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.worker-avatar {
  border: 3px solid #f0f2f5;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.worker-header {
  margin-bottom: 20px;
}

.worker-name {
  font-size: 24px;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-tag {
  margin-left: 10px;
}

.worker-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-label {
  color: #909399;
  font-weight: 500;
}

.info-value {
  color: #303133;
  font-weight: 500;
}

.spacer {
  height: 20px;
}

.tab-header {
  display: flex;
  justify-content: center;
}

.records-list, .ratings-list {
  margin-top: 20px;
}

.record-card {
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.record-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.record-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.repair-item {
  font-weight: bold;
  font-size: 16px;
}

.record-time {
  color: #909399;
  font-size: 14px;
}

.record-content {
  margin-bottom: 10px;
}

.record-location {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 10px;
  color: #909399;
}

.record-remark {
  padding-top: 10px;
  border-top: 1px dashed #EBEEF5;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 5px;
}

.rating-card {
  margin-bottom: 15px;
}

.rating-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.rating-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.repair-record-name {
  margin-top: 4px;
}

.rating-time {
  color: #909399;
  font-size: 14px;
}

.rating-content {
  padding: 10px 0;
  border-bottom: 1px dashed #EBEEF5;
  color: #303133;
  margin-bottom: 10px;
}

.rating-content p {
  margin: 0;
  line-height: 1.6;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #606266;
}

.worker-tags-section {
  margin-top: 25px;
  padding-top: 20px;
  border-top: 2px solid #f0f2f5;
}

.tags-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tags-list .el-tag {
  padding: 8px 16px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}
</style> 