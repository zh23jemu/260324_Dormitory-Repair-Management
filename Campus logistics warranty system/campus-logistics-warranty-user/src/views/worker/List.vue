<script setup>
import { ref, onMounted } from 'vue'
import { getUserList } from '@/api/user'
import { useRouter } from 'vue-router'

// 路由
const router = useRouter()

// 维修工列表
const workerList = ref([])

// 加载状态
const loading = ref(false)

// 查询参数
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  role: 'worker' // 筛选角色为维修工
})

// 总数
const total = ref(0)

// 获取维修工列表
const fetchWorkerList = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryParams.value)
    if (res.code === 200) {
      workerList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.msg || '获取维修工列表失败')
    }
  } catch (error) {
    console.error('获取维修工列表失败', error)
    ElMessage.error('获取维修工列表失败')
  } finally {
    loading.value = false
  }
}

// 处理分页变化
const handleCurrentChange = (page) => {
  queryParams.value.pageNum = page
  fetchWorkerList()
}

// 处理每页显示数量变化
const handleSizeChange = (size) => {
  queryParams.value.pageSize = size
  queryParams.value.pageNum = 1
  fetchWorkerList()
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 跳转到维修工详情页
const goToWorkerDetail = (workerId) => {
  router.push({
    name: 'workerDetail',
    params: { id: workerId }
  })
}

// 页面加载时获取维修工列表
onMounted(() => {
  fetchWorkerList()
})
</script>

<template>
  <div class="worker-list-container">
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>维修工列表</span>
        </div>
      </template>
      
      <div v-loading="loading" class="worker-container">
        <el-empty v-if="workerList.length === 0" description="暂无维修工信息"/>
        
        <template v-else>
          <div class="worker-card-grid">
            <el-card v-for="worker in workerList" :key="worker.id" class="worker-card" shadow="hover" @click="goToWorkerDetail(worker.id)">
              <div class="worker-card-header">
                <el-avatar :size="70" :src="worker.avatar" class="worker-avatar">
                  {{ worker.name ? worker.name.charAt(0) : 'W' }}
                </el-avatar>
                <div class="worker-basic-info">
                  <h2 class="worker-name">{{ worker.name }}</h2>
                  <div class="worker-status">
                    <el-tag :type="worker.status === 1 ? 'success' : 'info'" effect="dark">
                      {{ worker.status === 1 ? '在线' : '离线' }}
                    </el-tag>
                  </div>
                </div>
              </div>
              
              <el-divider />
              
              <div class="worker-info-grid">
                <div class="worker-info-item">
                  <el-icon><Phone /></el-icon>
                  <span class="info-label">电话:</span>
                  <span class="info-value">{{ worker.phone || '-' }}</span>
                </div>
                
                <div class="worker-info-item">
                  <el-icon><Message /></el-icon>
                  <span class="info-label">邮箱:</span>
                  <span class="info-value">{{ worker.email || '-' }}</span>
                </div>
                
                <div class="worker-info-item">
                  <el-icon><User /></el-icon>
                  <span class="info-label">用户名:</span>
                  <span class="info-value highlight">{{ worker.username }}</span>
                </div>
                
                <div class="worker-info-item" v-if="worker.sex">
                  <el-icon><Male v-if="worker.sex === '男'" /><Female v-else /></el-icon>
                  <span class="info-label">性别:</span>
                  <span class="info-value">{{ worker.sex }}</span>
                </div>
                
                <div class="worker-info-item worker-time">
                  <el-icon><Calendar /></el-icon>
                  <span class="info-label">注册时间:</span>
                  <span class="info-value">{{ formatDateTime(worker.createTime) }}</span>
                </div>
              </div>
              
              <div v-if="worker.tags" class="worker-tags">
                <div class="tags-label">
                  <el-icon><PriceTag /></el-icon>
                  <span>擅长领域:</span>
                </div>
                <div class="tags-content">
                  <el-tag 
                    v-for="(tag, index) in worker.tags.split(',')" 
                    :key="index"
                    type="success"
                    effect="plain"
                    size="small"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
              </div>
              
              <div class="card-footer">
                <el-button type="primary" size="small" @click.stop="goToWorkerDetail(worker.id)">
                  <el-icon><View /></el-icon>
                  查看详情
                </el-button>
              </div>
            </el-card>
          </div>
          
          <div class="pagination">
            <el-pagination
              v-model:current-page="queryParams.pageNum"
              v-model:page-size="queryParams.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </template>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.worker-list-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
}

.worker-container {
  min-height: 400px;
}

.worker-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 30px;
}

.worker-card {
  transition: all 0.3s;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}

.worker-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 20px rgba(0, 0, 0, 0.12);
}

.worker-card-header {
  display: flex;
  align-items: center;
  padding: 5px 0;
}

.worker-avatar {
  margin-right: 20px;
  border: 2px solid #f0f2f5;
}

.worker-basic-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.worker-name {
  margin: 0;
  font-size: 22px;
  color: #303133;
  line-height: 1.2;
}

.worker-status {
  margin-top: 5px;
}

.worker-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 24px;
}

.worker-info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.worker-time {
  grid-column: span 2;
  margin-top: 5px;
}

.info-label {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.info-value.highlight {
  color: #409EFF;
  font-weight: 600;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
}

.el-divider {
  margin: 15px 0;
}

.card-footer {
  margin-top: 15px;
  display: flex;
  justify-content: center;
}

.worker-tags {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #e4e7ed;
}

.tags-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.tags-content {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style> 