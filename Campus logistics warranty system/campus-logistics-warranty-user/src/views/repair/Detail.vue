<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getRepairRecordById } from '@/api/repairRecord'

const router = useRouter()
const route = useRoute()
const repairId = route.params.id

// 加载状态
const loading = ref(false)

// 报修信息
const repairInfo = ref({})

// 状态对应的标签类型
const statusTagType = {
  '待处理': '',
  '处理中': 'warning',
  '已完成': 'success',
  '已取消': 'info'
}

// 获取报修详情
const getDetail = async () => {
  loading.value = true
  try {
    const res = await getRepairRecordById(repairId)
    if (res.code === 200) {
      repairInfo.value = res.data
    } else {
      ElMessage.error(res.message || '获取报修详情失败')
    }
  } catch (error) {
    console.error('获取报修详情失败', error)
    ElMessage.error('获取报修详情失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

// 返回列表
const goBack = () => {
  router.push('/repair/list')
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 初始化
onMounted(() => {
  getDetail()
})
</script>

<template>
  <div class="repair-detail-container">
    <div class="page-header">
      <el-page-header @back="goBack">
        <template #content>
          <span class="page-title">报修详情</span>
        </template>
      </el-page-header>
    </div>
    
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="skeleton-content">
          <el-skeleton-item variant="text" style="width: 30%" />
          <el-skeleton-item variant="text" style="margin-top: 16px; width: 100%" />
          <el-skeleton-item variant="text" style="margin-top: 16px; width: 100%" />
          <el-skeleton-item variant="text" style="margin-top: 16px; width: 80%" />
        </div>
      </template>
      
      <template #default>
        <el-card class="detail-card">
          <div class="detail-header">
            <h2>{{ repairInfo.name }}</h2>
            <el-tag 
              :type="statusTagType[repairInfo.status]" 
              effect="dark"
            >
              {{ repairInfo.status }}
            </el-tag>
          </div>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item label="报修ID">{{ repairInfo.id }}</el-descriptions-item>
            <el-descriptions-item label="报修类型">{{ repairInfo.typeId }}</el-descriptions-item>
            <el-descriptions-item label="维修地点">{{ repairInfo.location }}</el-descriptions-item>
            <el-descriptions-item label="联系方式">{{ repairInfo.phone || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="收费情况">
              <el-tag :type="repairInfo.isCharge ? 'danger' : 'success'" size="small">
                {{ repairInfo.isCharge ? '付费维修' : '免费维修' }}
              </el-tag>
              <span v-if="repairInfo.isCharge && repairInfo.price" class="price-info">
                预估价格: {{ repairInfo.price }}元
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(repairInfo.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDateTime(repairInfo.updateTime) }}</el-descriptions-item>
            <el-descriptions-item label="处理人员">{{ repairInfo.workerName || '暂未分配' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ repairInfo.remark || '暂无备注' }}</el-descriptions-item>
          </el-descriptions>
          
          <div class="content-section">
            <h3>问题描述</h3>
            <div class="content-text">{{ repairInfo.description }}</div>
          </div>
          
          <div class="images-section" v-if="repairInfo.img">
            <h3>报修图片</h3>
            <div class="image-container">
              <el-image 
                :src="repairInfo.img" 
                fit="contain"
                :preview-src-list="[repairInfo.img]"
                class="repair-image"
              />
            </div>
          </div>
        </el-card>
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.repair-detail-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
}

.skeleton-content {
  padding: 20px;
}

.detail-card {
  max-width: 1000px;
  margin: 0 auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.detail-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: bold;
}

.content-section,
.images-section {
  margin-top: 30px;
}

.content-section h3,
.images-section h3 {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  border-left: 4px solid #409EFF;
  padding-left: 10px;
}

.content-text {
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.image-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.repair-image {
  max-width: 100%;
  max-height: 400px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.price-info {
  margin-left: 10px;
  color: #F56C6C;
  font-weight: bold;
}
</style> 