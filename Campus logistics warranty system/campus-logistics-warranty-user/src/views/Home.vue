<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from '@/store'
import { getNoticeList, getNoticeById } from '@/api/notice'

const router = useRouter()
const userStore = useStore()
const isLoggedIn = computed(() => !!userStore.token)

// 公告数据
const noticeList = ref([])
const loading = ref(false)

// 公告详情弹窗
const selectedNotice = ref(null)
const noticeDetailVisible = ref(false)
const detailLoading = ref(false)

// 获取公告数据
onMounted(async () => {
  loading.value = true
  try {
    const res = await getNoticeList({
      pageNum: 1,
      pageSize: 5,
      status: 1 // 只获取已发布的公告
    })
    
    if (res.code === 200) {
      noticeList.value = res.data.records || []
    }
  } catch (error) {
    console.error('获取公告失败', error)
  } finally {
    loading.value = false
  }
})

// 处理立即报修按钮点击
const handleRepairClick = () => {
  if (isLoggedIn.value) {
    router.push('/repair/create')
  } else {
    ElMessageBox.confirm('您需要登录后才能提交报修申请，是否现在登录？', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'info'
    }).then(() => {
      router.push('/login')
    }).catch(() => {})
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
    }
  } catch (error) {
    console.error('获取公告详情失败', error)
  } finally {
    detailLoading.value = false
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString()
}

// 查看更多公告
const viewAllNotices = () => {
  router.push('/announcement')
}
</script>

<template>
  <div class="home-container">
    <div class="banner">
      <div class="banner-content">
        <h1>欢迎使用校园后勤报修系统</h1>
        <p>一站式解决校园物业问题，提供快速、高效的报修服务</p>
        <el-button type="primary" size="large" @click="handleRepairClick">
          <el-icon><Check /></el-icon>
          立即报修
        </el-button>
      </div>
    </div>
    
    <!-- 系统公告 -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">系统公告</h2>
        <el-button text type="primary" @click="viewAllNotices">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      
      <div v-loading="loading">
        <el-empty v-if="noticeList.length === 0" description="暂无公告" />
        <div v-else class="notice-list">
          <el-card 
            v-for="item in noticeList" 
            :key="item.id" 
            class="notice-card"
            shadow="hover"
            @click="viewNoticeDetail(item)"
          >
            <div class="notice-content">
              <div class="notice-img-wrapper" v-if="item.img">
                <el-image 
                  :src="item.img" 
                  fit="cover" 
                  class="notice-img"
                />
              </div>
              <div class="notice-info">
                <div class="notice-header">
                  <h3>{{ item.title }}</h3>
                  <span class="notice-date">{{ formatDate(item.createTime) }}</span>
                </div>
                <p class="notice-text">{{ item.content }}</p>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>
    
    <!-- 操作指南 -->
    <div class="section guide-section">
      <h2 class="section-title">操作指南</h2>
      <el-steps :active="4" finish-status="success">
        <el-step title="注册登录" description="注册账号并登录系统" />
        <el-step title="填写信息" description="填写保修所需信息" />
        <el-step title="提交申请" description="提交保修申请" />
        <el-step title="进度查询" description="在个人中心查询保修进度" />
      </el-steps>
    </div>
    
    <!-- 公告详情弹窗 -->
    <el-dialog
      v-model="noticeDetailVisible"
      :title="selectedNotice?.title"
      width="700px"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="notice-detail">
        <div class="notice-meta">
          <span class="notice-time">发布时间: {{ formatDate(selectedNotice?.createTime) }}</span>
        </div>
        
        <div class="notice-detail-img" v-if="selectedNotice?.img">
          <el-image 
            :src="selectedNotice.img" 
            fit="cover"
            class="detail-img"
            :preview-src-list="[selectedNotice.img]"
          />
        </div>
        
        <div class="notice-detail-content">
          {{ selectedNotice?.content }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.home-container {
  padding: 0;
}

.banner {
  height: 300px;
  background: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('https://img.freepik.com/free-photo/group-students-university-campus_23-2148178615.jpg');
  background-size: cover;
  background-position: center;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
  text-align: center;
}

.banner-content {
  max-width: 800px;
  padding: 0 20px;
}

.banner h1 {
  font-size: 32px;
  margin-bottom: 15px;
  font-weight: bold;
}

.banner p {
  font-size: 16px;
  margin-bottom: 25px;
}

.section {
  padding: 0 20px;
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 22px;
  font-weight: bold;
  border-left: 4px solid #409EFF;
  padding-left: 15px;
  margin: 0;
}

.notice-list {
  display: grid;
  grid-gap: 15px;
}

.notice-card {
  transition: transform 0.3s ease;
  cursor: pointer;
}

.notice-card:hover {
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
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.notice-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
}

.notice-date {
  color: #999;
  font-size: 12px;
}

.notice-text {
  color: #666;
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.guide-section {
  margin-top: 40px;
  margin-bottom: 40px;
  padding: 30px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.notice-detail {
  padding: 10px;
}

.notice-meta {
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
  color: #666;
  font-size: 14px;
}

.notice-detail-img {
  margin-bottom: 20px;
  text-align: center;
}

.detail-img {
  max-width: 100%;
  max-height: 300px;
  border-radius: 4px;
  object-fit: contain;
}

.notice-detail-content {
  line-height: 1.8;
  white-space: pre-line;
}

@media (max-width: 768px) {
  .banner {
    height: 200px;
  }
  
  .banner h1 {
    font-size: 24px;
  }
  
  .notice-content {
    flex-direction: column;
  }
  
  .notice-img-wrapper {
    width: 100%;
    height: 150px;
  }
}
</style> 