<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from '@/store'
import { getRepairRecordList } from '@/api/repairRecord'

const router = useRouter()
const userStore = useStore()

// 查询参数
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  userId: null // 只查询当前用户的报修记录
})

// 报修记录列表
const repairList = ref([])

// 总记录数
const total = ref(0)

// 加载状态
const loading = ref(false)

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 报修状态对应的标签类型
const statusTagType = {
  '待处理': '',
  '处理中': 'warning',
  '已完成': 'success',
  '已取消': 'info'
}

// 是否收费对应的文本
const chargeText = {
  0: '免费',
  1: '收费'
}

// 初始化数据
onMounted(async () => {
  if (userStore.token) {
    // 确保先获取到用户信息
    if (!userInfo.value.id) {
      await userStore.getUserInfo()
    }
    
    // 设置当前用户ID作为查询条件
    queryParams.value.userId = userInfo.value.id
    
    // 加载报修记录
    fetchRepairList()
  }
})

// 获取报修记录列表
const fetchRepairList = async () => {
  loading.value = true
  try {
    const res = await getRepairRecordList(queryParams.value)
    if (res.code === 200) {
      repairList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取报修记录失败')
    }
  } catch (error) {
    console.error('获取报修记录失败', error)
    ElMessage.error('获取报修记录失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

// 查看报修详情
const viewDetail = (id) => {
  router.push(`/repair/detail/${id}`)
}

// 创建新的报修单
const createRepair = () => {
  router.push('/repair/create')
}

// 处理分页变化
const handleCurrentChange = (page) => {
  queryParams.value.pageNum = page
  fetchRepairList()
}

// 处理每页显示数量变化
const handleSizeChange = (size) => {
  queryParams.value.pageSize = size
  queryParams.value.pageNum = 1 // 重置为第一页
  fetchRepairList()
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取图片第一张作为预览
const getFirstImage = (imgStr) => {
  if (!imgStr) return ''
  return imgStr
}
</script>

<template>
  <div class="repair-list-container">
    <div class="page-header">
      <h2>我的报修记录</h2>
      <el-button type="primary" @click="createRepair">
        <el-icon><Plus /></el-icon> 新建报修
      </el-button>
    </div>
    
    <el-card v-loading="loading">
      <template v-if="repairList.length === 0">
        <el-empty description="暂无报修记录">
          <el-button type="primary" @click="createRepair">立即报修</el-button>
        </el-empty>
      </template>
      
      <template v-else>
        <div class="repair-list">
          <el-card 
            v-for="item in repairList" 
            :key="item.id" 
            class="repair-item"
            :body-style="{ padding: '0px' }"
            @click="viewDetail(item.id)"
          >
            <div class="repair-content">
              <div class="repair-info">
                <div class="repair-title">
                  <span class="repair-name">{{ item.name }}</span>
                  <el-tag 
                    :type="statusTagType[item.status]" 
                    size="small"
                  >
                    {{ item.status }}
                  </el-tag>
                </div>
                
                <div class="repair-meta">
                  <span class="repair-location">
                    <el-icon><Location /></el-icon> {{ item.location }}
                  </span>
                  <span class="repair-time">
                    <el-icon><Timer /></el-icon> {{ formatDateTime(item.createTime) }}
                  </span>
                </div>
                
                <div class="repair-description">{{ item.description }}</div>
                
                <div class="repair-footer">
                  <el-tag size="small" :type="item.isCharge ? 'danger' : 'success'">
                    {{ chargeText[item.isCharge] }}
                  </el-tag>
                  
                  <el-button 
                    link 
                    type="primary" 
                    size="small" 
                    @click.stop="viewDetail(item.id)"
                  >
                    查看详情
                  </el-button>
                </div>
              </div>
              
              <div class="repair-image" v-if="item.img">
                <el-image 
                  :src="item.img" 
                  fit="cover"
                  :preview-src-list="[item.img]"
                />
              </div>
            </div>
          </el-card>
        </div>
        
        <div class="pagination">
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[5, 10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </template>
    </el-card>
  </div>
</template>

<style scoped>
.repair-list-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.repair-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.repair-item {
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
}

.repair-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.repair-content {
  display: flex;
  padding: 15px;
}

.repair-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.repair-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.repair-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.repair-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 8px;
  font-size: 13px;
  color: #909399;
}

.repair-location, .repair-time {
  display: flex;
  align-items: center;
  gap: 4px;
}

.repair-description {
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.repair-footer {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.repair-image {
  width: 100px;
  height: 100px;
  margin-left: 15px;
  border-radius: 4px;
  overflow: hidden;
}

.repair-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 