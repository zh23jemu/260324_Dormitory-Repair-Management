<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

import { getResourceList } from '@/api/resource'

const router = useRouter()

// 资源列表数据
const resources = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)

// 获取资源列表
const fetchResources = async () => {
  loading.value = true
  try {
    const res = await getResourceList({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      resources.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.msg || '获取资源列表失败')
    }
  } catch (error) {
    console.error('获取资源列表失败', error)
    ElMessage.error('获取资源列表失败')
  } finally {
    loading.value = false
  }
}

// 查看资源详情
const viewResource = (id) => {
  router.push({
    name: 'resourceDetail',
    params: { id }
  })
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 处理分页变化
const handlePageChange = (page) => {
  pageNum.value = page
  fetchResources()
}

// 处理每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  pageNum.value = 1
  fetchResources()
}

onMounted(() => {
  fetchResources()
})
</script>

<template>
  <div class="resource-list-container">
    <div class="page-header">
      <h2>资源大厅</h2>
      <p class="subtitle">浏览和下载各类学习资源</p>
    </div>

    <el-row v-loading="loading" :gutter="20" class="resource-grid">
      <el-col 
        v-for="resource in resources" 
        :key="resource.id" 
        :xs="24" 
        :sm="12" 
        :md="8" 
        :lg="6"
      >
        <el-card 
          class="resource-card" 
          shadow="hover" 
          @click="viewResource(resource.id)"
        >
          <div class="resource-cover">
            <el-image 
              :src="resource.cover || 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'"
              fit="cover"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Document /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
          
          <div class="resource-info">
            <h3 class="resource-title">{{ resource.title }}</h3>
            <p class="resource-description">{{ resource.description }}</p>
            <div class="resource-meta">
              <span class="create-time">{{ formatDateTime(resource.createTime) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 36, 48]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.resource-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h2 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
}

.subtitle {
  color: #909399;
  font-size: 16px;
}

.resource-grid {
  margin-bottom: 30px;
}

.resource-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.resource-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.resource-cover {
  height: 200px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 15px;
}

.resource-cover .el-image {
  width: 100%;
  height: 100%;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 48px;
}

.resource-info {
  padding: 0 10px;
}

.resource-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resource-description {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
  height: 42px;
}

.resource-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #909399;
  font-size: 12px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style> 