<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useStore } from '@/store'
import { useRouter } from 'vue-router'
import { getPostList, savePost } from '@/api/post'

const userStore = useStore()
const router = useRouter()

// 发帖表单
const postForm = reactive({
  title: '',
  content: '',
  summary: '',
  cover: ''
})

// 是否显示发布弹窗
const dialogVisible = ref(false)

// 发布按钮状态
const loading = ref(false)
const tableLoading = ref(false)

// 帖子列表
const postList = ref([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: 1 // 固定为审核通过的帖子
})

// 表单验证规则
const postRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' }
  ]
}

// 表单引用
const postFormRef = ref(null)

// 上传图片前的验证
const beforeUpload = (file) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('上传图片只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 图片上传成功
const handleUploadSuccess = (response) => {
  postForm.cover = response.data
  ElMessage.success('封面上传成功')
}

// 图片上传失败
const handleUploadError = () => {
  ElMessage.error('封面上传失败')
}

// 获取帖子列表
const fetchPosts = async () => {
  tableLoading.value = true
  try {
    const res = await getPostList(queryParams)
    postList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取帖子列表失败', error)
    ElMessage.error('获取帖子列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 页面大小变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  fetchPosts()
}

// 页码变化
const handleCurrentChange = (current) => {
  queryParams.pageNum = current
  fetchPosts()
}

// 搜索
const handleSearch = () => {
  queryParams.pageNum = 1
  fetchPosts()
}

// 打开发布弹窗
const openPublishDialog = () => {
  resetForm()
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  postForm.title = ''
  postForm.content = ''
  postForm.summary = ''
  postForm.cover = ''
  if (postFormRef.value) {
    postFormRef.value.resetFields()
  }
}

// 跳转到帖子详情
const goToDetail = (id) => {
  if (id) {
    router.push(`/post/detail/${id}`)
  }
}

// 发布帖子
const submitPost = async () => {
  if (postFormRef.value) {
    await postFormRef.value.validate(async (valid) => {
      if (valid) {
        // 自动生成摘要（取内容前50个字符）
        postForm.summary = postForm.content.substring(0, 50)
        
        loading.value = true
        try {
          await savePost(postForm)
          ElMessage.success('发布成功')
          dialogVisible.value = false
          resetForm()
          // 重新加载帖子列表
          fetchPosts()
        } catch (error) {
          console.error('发布失败', error)
          ElMessage.error('发布失败')
        } finally {
          loading.value = false
        }
      }
    })
  }
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 页面初始化时加载帖子列表
onMounted(() => {
  fetchPosts()
})
</script>

<template>
  <div class="forum-container">
    <div class="forum-header">
      <h1>校园论坛</h1>
      
      <div class="forum-actions">
        <!-- 搜索栏 -->
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索帖子"
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <!-- 发布按钮 -->
        <el-button type="primary" @click="openPublishDialog">
          <el-icon><Plus /></el-icon> 发布帖子
        </el-button>
      </div>
    </div>
    
    <!-- 帖子列表 -->
    <div v-loading="tableLoading" class="post-list-container">
      <el-empty v-if="postList.length === 0" description="暂无帖子内容" />
      
      <div v-else class="post-grid">
        <el-card 
          v-for="post in postList" 
          :key="post.id" 
          class="post-card"
          shadow="hover"
          @click="goToDetail(post.id)"
        >
          <div class="post-cover">
            <el-image 
              :src="post.cover" 
              fit="cover"
              :lazy="true"
              class="cover-img"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
          
          <div class="post-content">
            <h3 class="post-title">{{ post.title }}</h3>
            <p class="post-summary">{{ post.summary }}</p>
            
            <div class="post-meta">
              <div class="post-author">
                <el-avatar :size="32" :src="post.authorAvatar">
                  {{ post.authorName ? post.authorName.charAt(0) : 'U' }}
                </el-avatar>
                <span class="author-name">{{ post.authorName }}</span>
              </div>
              
              <div class="post-stats">
                <div class="stat-item">
                  <el-icon><View /></el-icon>
                  <span>{{ post.views }}</span>
                </div>
                <div class="stat-item">
                  <el-icon><Star /></el-icon>
                  <span>{{ post.likes }}</span>
                </div>
                <div class="post-time">
                  {{ formatDateTime(post.createdTime) }}
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      
      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[9, 12, 18, 24]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 发布帖子弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="发布帖子"
      width="650px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="postFormRef"
        :model="postForm"
        :rules="postRules"
        label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="postForm.title" placeholder="请输入帖子标题" />
        </el-form-item>
        
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="postForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入帖子内容"
          />
        </el-form-item>
        
        <el-form-item label="封面">
          <el-upload
            class="avatar-uploader"
            action="/api/file/upload"
            :headers="{ Authorization: userStore.token }"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
          >
            <img v-if="postForm.cover" :src="postForm.cover" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">支持JPG、PNG格式，不超过2MB</div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="loading" @click="submitPost">发布</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.forum-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.forum-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.forum-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.forum-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input {
  width: 300px;
}

.post-list-container {
  min-height: 500px;
}

/* 网格布局 */
.post-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.post-card {
  cursor: pointer;
  transition: transform 0.3s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.post-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.post-cover {
  height: 200px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 12px;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.post-card:hover .cover-img {
  transform: scale(1.05);
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 32px;
}

.post-content {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-summary {
  color: #606266;
  font-size: 14px;
  margin: 0 0 16px 0;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  flex-grow: 1;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  font-size: 13px;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-name {
  font-weight: 500;
  color: #303133;
}

.post-stats {
  display: flex;
  align-items: center;
  color: #909399;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-time {
  font-size: 12px;
  color: #909399;
}

.avatar-uploader {
  display: flex;
  justify-content: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  width: 178px;
  height: 178px;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 