<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNoticeList, getNoticeById } from '@/api/notice'

const router = useRouter()

// 加载状态
const loading = ref(false)
const detailLoading = ref(false)

// 公告列表
const noticeList = ref([])

// 分页数据
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 搜索关键字
const searchKeyword = ref('')

// 当前选中的公告
const selectedNotice = ref(null)
const noticeDetailVisible = ref(false)

// 获取公告列表
const getNotices = async () => {
  loading.value = true
  try {
    const res = await getNoticeList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value,
      status: 1 // 只获取已发布的公告
    })
    
    if (res.code === 200) {
      noticeList.value = res.data.records || []
      pagination.total = res.data.total || 0
    } else {
      ElMessage.error(res.msg || '获取公告列表失败')
    }
  } catch (error) {
    console.error('获取公告列表失败', error)
    ElMessage.error('获取公告列表失败')
  } finally {
    loading.value = false
  }
}

// 查看公告详情
const viewNoticeDetail = async (notice) => {
  selectedNotice.value = notice
  noticeDetailVisible.value = true
  
  // 加载完整公告内容
  detailLoading.value = true
  try {
    const res = await getNoticeById(notice.id)
    if (res.code === 200) {
      selectedNotice.value = res.data
    } else {
      ElMessage.error(res.msg || '获取公告详情失败')
    }
  } catch (error) {
    console.error('获取公告详情失败', error)
    ElMessage.error('获取公告详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 关闭详情弹窗
const closeNoticeDetail = () => {
  noticeDetailVisible.value = false
  selectedNotice.value = null
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  getNotices()
}

// 重置搜索
const handleReset = () => {
  searchKeyword.value = ''
  pagination.pageNum = 1
  getNotices()
}

// 处理分页变化
const handleCurrentChange = (val) => {
  pagination.pageNum = val
  getNotices()
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 截取内容摘要
const getContentSummary = (content, length = 100) => {
  if (!content) return ''
  if (content.length <= length) return content
  return content.substring(0, length) + '...'
}

// 初始化
onMounted(() => {
  getNotices()
})
</script>

<template>
  <div class="announcement-container">
    <h1 class="page-title">系统公告</h1>
    
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-input
        v-model="searchKeyword"
        placeholder="请输入公告标题关键字"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>
    
    <!-- 公告列表 -->
    <div class="notice-list" v-loading="loading">
      <el-empty v-if="noticeList.length === 0" description="暂无公告" />
      <template v-else>
        <el-card 
          v-for="item in noticeList"
          :key="item.id"
          class="notice-item"
          shadow="hover"
          @click="viewNoticeDetail(item)"
        >
          <div class="notice-content">
            <div class="notice-img-wrapper" v-if="item.img">
              <el-image :src="item.img" fit="cover" class="notice-img" />
            </div>
            <div class="notice-info">
              <div class="notice-header">
                <h3 class="notice-title">{{ item.title }}</h3>
                <span class="notice-date">{{ formatDate(item.createTime) }}</span>
              </div>
              <p class="notice-desc">{{ getContentSummary(item.content) }}</p>
              <div class="notice-footer">
                <span class="notice-views">
                  <el-icon><View /></el-icon> {{ item.views || 0 }}
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </template>
    </div>
    
    <!-- 分页 -->
    <div class="pagination-area" v-if="pagination.total > 0">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        layout="total, prev, pager, next, jumper"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 公告详情弹窗 -->
    <el-dialog
      v-model="noticeDetailVisible"
      :title="selectedNotice?.title"
      width="700px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div v-loading="detailLoading" class="notice-detail">
        <div class="notice-meta">
          <span class="notice-time">发布时间: {{ formatDate(selectedNotice?.createTime) }}</span>
          <span class="notice-views">
            <el-icon><View /></el-icon> {{ selectedNotice?.views || 0 }}次浏览
          </span>
        </div>
        
        <div class="notice-detail-img" v-if="selectedNotice?.img">
          <el-image 
            :src="selectedNotice.img" 
            fit="contain"
            style="width: 100%"
            :preview-src-list="[selectedNotice.img]"
          />
        </div>
        
        <div class="notice-detail-content">
          {{ selectedNotice?.content }}
        </div>

        <div class="notice-attachments" v-if="selectedNotice?.attachments">
          <h4>附件下载:</h4>
          <div class="attachment-list">
            <div 
              v-for="(url, index) in selectedNotice.attachments.split(',')" 
              :key="index"
              class="attachment-item"
            >
              <el-button type="primary" link :href="url" target="_blank">
                <el-icon><Document /></el-icon>
                附件 {{ index + 1 }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeNoticeDetail">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.announcement-container {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  text-align: center;
  color: #409EFF;
}

.search-area {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.search-area .el-input {
  max-width: 300px;
}

.notice-list {
  margin-bottom: 20px;
}

.notice-item {
  margin-bottom: 15px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.notice-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.notice-content {
  display: flex;
  gap: 15px;
}

.notice-img-wrapper {
  flex-shrink: 0;
  width: 120px;
  height: 80px;
  overflow: hidden;
  border-radius: 4px;
}

.notice-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.notice-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notice-title {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.notice-date {
  font-size: 12px;
  color: #909399;
}

.notice-desc {
  margin: 0;
  color: #606266;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.notice-footer {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

.notice-views {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination-area {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.notice-detail {
  padding: 10px;
}

.notice-meta {
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
  color: #606266;
  font-size: 14px;
  display: flex;
  justify-content: space-between;
}

.notice-detail-img {
  margin-bottom: 20px;
  text-align: center;
}

.notice-detail-content {
  line-height: 1.8;
  white-space: pre-line;
  color: #303133;
}

.notice-attachments {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.notice-attachments h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #303133;
}

.attachment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.attachment-item {
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .notice-content {
    flex-direction: column;
  }
  
  .notice-img-wrapper {
    width: 100%;
    height: 150px;
    margin-bottom: 10px;
  }
  
  .notice-meta {
    flex-direction: column;
    gap: 8px;
  }
}
</style> 