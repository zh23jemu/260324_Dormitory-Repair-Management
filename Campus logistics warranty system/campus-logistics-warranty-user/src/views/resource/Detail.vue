<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { getResourceById } from '@/api/resource'

const route = useRoute()
const router = useRouter()

// 资源详情数据
const resource = ref({})
const loading = ref(false)

// 获取资源详情
const fetchResourceDetail = async () => {
  loading.value = true
  try {
    const res = await getResourceById(route.params.id)
    if (res.code === 200) {
      resource.value = res.data
    } else {
      ElMessage.error(res.msg || '获取资源详情失败')
    }
  } catch (error) {
    console.error('获取资源详情失败', error)
    ElMessage.error('获取资源详情失败')
  } finally {
    loading.value = false
  }
}

// 返回列表页
const goBack = () => {
  router.push({ name: 'resourceList' })
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  fetchResourceDetail()
})
</script>

<template>
  <div class="resource-detail-container">
    <el-card v-loading="loading" class="resource-detail-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button @click="goBack" type="primary" plain size="small">
              <el-icon><Back /></el-icon>
              返回列表
            </el-button>
            <span class="title">资源详情</span>
          </div>
        </div>
      </template>

      <div class="resource-info">
        <h1 class="resource-title">{{ resource.title }}</h1>
        <div class="resource-meta">
          <span class="create-time">发布时间：{{ formatDateTime(resource.createTime) }}</span>
        </div>
        <p class="resource-description">{{ resource.description }}</p>
      </div>

      <div class="pdf-container">
        <iframe
          v-if="resource.url"
          :src="resource.url"
          width="100%"
          height="800"
          frameborder="0"
        ></iframe>
        <div v-else class="no-pdf">
          <el-empty description="暂无PDF文件" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.resource-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
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

.resource-info {
  margin-bottom: 30px;
}

.resource-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 15px;
}

.resource-meta {
  margin-bottom: 15px;
  color: #909399;
  font-size: 14px;
}

.resource-description {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 20px;
}

.pdf-container {
  width: 100%;
  min-height: 800px;
  background-color: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
}

.no-pdf {
  height: 800px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style> 