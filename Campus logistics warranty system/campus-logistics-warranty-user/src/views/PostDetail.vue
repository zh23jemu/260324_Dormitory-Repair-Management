<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from '@/store'
import { getPostById, savePost } from '@/api/post'
import { getCommentByPostId, saveComment } from '@/api/comment'
import { saveCollect } from '@/api/collect'
import CommentReplies from '@/components/CommentReplies.vue'

const route = useRoute()
const router = useRouter()
const userStore = useStore()

// 帖子ID
const postId = ref(route.params.id)

// 帖子详情
const postDetail = ref({})

// 评论列表
const commentList = ref([])

// 加载状态
const loading = ref(false)
const commentLoading = ref(false)
const submitLoading = ref(false)
const collectLoading = ref(false)

// 评论表单
const commentForm = reactive({
  postId: '',
  content: '',
  parentId: null
})

// 回复对话框
const replyDialogVisible = ref(false)
const currentReplyComment = ref(null)

// 当前用户ID
const userId = computed(() => userStore.userInfo?.id)

// 评论表单验证规则
const commentRules = {
  content: [
    { required: true, message: '请输入评论内容', trigger: 'blur' }
  ]
}

// 评论表单引用
const commentFormRef = ref(null)
const replyFormRef = ref(null)

// 加载帖子详情
const loadPostDetail = async () => {
  if (!postId.value) return
  
  loading.value = true
  try {
    const res = await getPostById(postId.value)
    postDetail.value = res.data
  } catch (error) {
    console.error('获取帖子详情失败', error)
    ElMessage.error('获取帖子详情失败')
  } finally {
    loading.value = false
  }
}

// 加载评论列表
const loadComments = async () => {
  if (!postId.value) return
  
  commentLoading.value = true
  try {
    const res = await getCommentByPostId(postId.value)
    commentList.value = res.data
  } catch (error) {
    console.error('获取评论列表失败', error)
    ElMessage.error('获取评论列表失败')
  } finally {
    commentLoading.value = false
  }
}

// 打开回复对话框
const openReplyDialog = (comment) => {
  currentReplyComment.value = comment
  commentForm.parentId = comment.id
  commentForm.content = ''
  replyDialogVisible.value = true
  
  // 下一帧等待对话框出现后设置焦点
  setTimeout(() => {
    document.getElementById('reply-input')?.focus()
  }, 100)
}

// 关闭回复对话框
const closeReplyDialog = () => {
  currentReplyComment.value = null
  commentForm.parentId = null
  commentForm.content = ''
  replyDialogVisible.value = false
}

// 点赞评论
const likeComment = async (comment) => {
  try {
    // 更新点赞数量
    const updatedComment = { 
      ...comment, 
      likeCount: (comment.likeCount || 0) + 1 
    }
    
    await saveComment(updatedComment)
    ElMessage.success('点赞成功')
    
    // 重新加载评论列表
    await loadComments()
  } catch (error) {
    console.error('点赞失败', error)
    ElMessage.error('点赞失败')
  }
}

// 点赞帖子
const likePost = async () => {
  try {
    // 更新点赞数量
    const updatedPost = {
      ...postDetail.value,
      likes: (postDetail.value.likes || 0) + 1
    }
    
    await savePost(updatedPost)
    ElMessage.success('点赞成功')
    
    // 更新本地帖子数据
    postDetail.value = updatedPost
  } catch (error) {
    console.error('点赞失败', error)
    ElMessage.error('点赞失败')
  }
}

// 收藏帖子
const collectPost = async () => {
  if (!userId.value) {
    ElMessage.warning('请先登录')
    return
  }
  
  collectLoading.value = true
  try {
    const data = {
      postId: postId.value,
      userId: userId.value
    }
    
    const res = await saveCollect(data)
    if (res.code === 200) {
      ElMessage.success('收藏成功')
    } else {
      ElMessage.info(res.msg || '收藏失败')
    }
  } catch (error) {
    console.error('收藏失败', error)
    ElMessage.error('收藏失败')
  } finally {
    collectLoading.value = false
  }
}

// 提交评论
const submitComment = async () => {
  if (commentFormRef.value) {
    await commentFormRef.value.validate(async (valid) => {
      if (valid) {
        submitLoading.value = true
        try {
          const data = { 
            ...commentForm, 
            postId: postId.value,
            parentId: null
          }
          
          await saveComment(data)
          ElMessage.success('评论成功')
          commentForm.content = ''
          
          // 重新加载评论列表
          await loadComments()
        } catch (error) {
          console.error('评论失败', error)
          ElMessage.error('评论失败')
        } finally {
          submitLoading.value = false
        }
      }
    })
  }
}

// 提交回复
const submitReply = async () => {
  if (replyFormRef.value) {
    await replyFormRef.value.validate(async (valid) => {
      if (valid) {
        submitLoading.value = true
        try {
          const data = { 
            ...commentForm, 
            postId: postId.value,
            parentId: currentReplyComment.value.id
          }
          
          await saveComment(data)
          ElMessage.success('回复成功')
          closeReplyDialog()
          
          // 重新加载评论列表
          await loadComments()
        } catch (error) {
          console.error('回复失败', error)
          ElMessage.error('回复失败')
        } finally {
          submitLoading.value = false
        }
      }
    })
  }
}

// 渲染评论和回复 - 修改为使用后端返回的嵌套结构
const renderComments = () => {
  return commentList.value.filter(comment => comment.parentId === null)
}

// 获取某个评论的所有子回复 - 直接使用后端返回的reply字段
const getReplies = (comment) => {
  return comment.reply || []
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 页面初始化时加载数据
onMounted(() => {
  loadPostDetail()
  loadComments()
})
</script>

<template>
  <div class="post-detail-container">
    <!-- 返回按钮 -->
    <div class="back-link">
      <el-button text @click="router.push('/forum')">
        <el-icon><ArrowLeft /></el-icon> 返回论坛
      </el-button>
    </div>
    
    <!-- 帖子详情 -->
    <div v-loading="loading" class="post-card">
      <div v-if="!postDetail.id" class="post-empty">
        <el-empty description="帖子不存在或已被删除"/>
      </div>
      
      <template v-else>
        <div class="post-header">
          <h1 class="post-title">{{ postDetail.title }}</h1>
          
          <div class="post-meta">
            <div class="author-info">
              <el-avatar :size="40" :src="postDetail.authorAvatar">
                {{ postDetail.authorName ? postDetail.authorName.charAt(0) : 'U' }}
              </el-avatar>
              <span class="author-name">{{ postDetail.authorName || '用户' }}</span>
            </div>
            
            <div class="post-stats">
              <div class="stat-item">
                <el-icon><View /></el-icon>
                <span>{{ postDetail.views }}</span>
              </div>
              <div class="stat-item like-btn" @click="likePost">
                <el-icon><Star /></el-icon>
                <span>{{ postDetail.likes || 0 }}</span>
              </div>
              <div class="post-time">
                发布于 {{ formatDateTime(postDetail.createdTime) }}
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="postDetail.cover" class="post-cover">
          <el-image 
            :src="postDetail.cover" 
            fit="cover"
            :preview-src-list="[postDetail.cover]"
          />
        </div>
        
        <div class="post-content">
          {{ postDetail.content }}
        </div>
        
        <div class="post-actions">
          <el-button type="primary" @click="likePost" :icon="Star">
            点赞
          </el-button>
          <el-button type="success" @click="collectPost" :loading="collectLoading" :icon="Star">
            收藏
          </el-button>
        </div>
      </template>
    </div>
    
    <!-- 评论区 -->
    <div class="comments-section">
      <div class="section-title">
        <h2>评论区 ({{ commentList.length }})</h2>
      </div>
      
      <!-- 评论输入 -->
      <div class="comment-form">
        <el-form
          ref="commentFormRef"
          :model="commentForm"
          :rules="commentRules"
        >
          <el-form-item prop="content">
            <el-input
              v-model="commentForm.content"
              type="textarea"
              :rows="3"
              placeholder="写下你的评论..."
            />
          </el-form-item>
          
          <div class="form-actions">
            <el-button type="primary" :loading="submitLoading" @click="submitComment">提交评论</el-button>
          </div>
        </el-form>
      </div>
      
      <!-- 评论列表 -->
      <div v-loading="commentLoading" class="comments-list">
        <el-empty v-if="commentList.length === 0" description="暂无评论" />
        
        <template v-else>
          <!-- 顶级评论 -->
          <div v-for="comment in renderComments()" :key="comment.id" class="comment-thread">
            <div class="comment-item">
              <div class="comment-avatar">
                <el-avatar :size="36" :src="comment.userAvatar">
                  {{ comment.username ? comment.username.charAt(0) : 'U' }}
                </el-avatar>
              </div>
              
              <div class="comment-main">
                <div class="comment-header">
                  <span class="user-name">{{ comment.username }}</span>
                  <span class="comment-time">{{ formatDateTime(comment.createdTime) }}</span>
                </div>
                
                <div class="comment-content">
                  {{ comment.content }}
                </div>
                
                <div class="comment-actions">
                  <span class="action-item" @click="openReplyDialog(comment)">
                    <el-icon><ChatDotRound /></el-icon> 回复
                  </span>
                  
                  <span class="action-item" @click="likeComment(comment)">
                    <el-icon><StarFilled /></el-icon> {{ comment.likeCount || 0 }}
                  </span>
                </div>
                
                <!-- 子评论递归组件，使用后端返回的嵌套结构 -->
                <CommentReplies 
                  v-if="getReplies(comment).length > 0"
                  :comments="getReplies(comment)"
                  :parent-id="null"
                  @reply="openReplyDialog"
                  @like="likeComment"
                />
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
    
    <!-- 回复对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      title="回复评论"
      width="500px"
      :close-on-click-modal="false"
    >
      <template v-if="currentReplyComment">
        <div class="reply-to-comment">
          <div class="reply-header">
            <el-avatar :size="28" :src="currentReplyComment.userAvatar">
              {{ currentReplyComment.username ? currentReplyComment.username.charAt(0) : 'U' }}
            </el-avatar>
            <span class="reply-user">{{ currentReplyComment.username }}</span>
          </div>
          <div class="reply-content">{{ currentReplyComment.content }}</div>
        </div>
        
        <el-form
          ref="replyFormRef"
          :model="commentForm"
          :rules="commentRules"
          class="reply-form"
        >
          <el-form-item prop="content">
            <el-input
              id="reply-input"
              v-model="commentForm.content"
              type="textarea"
              :rows="3"
              placeholder="写下你的回复..."
            />
          </el-form-item>
        </el-form>
      </template>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeReplyDialog">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitReply">
            提交回复
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.post-detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px 20px;
}

.back-link {
  margin-bottom: 16px;
}

.post-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  padding: 30px;
  margin-bottom: 30px;
}

.post-empty {
  padding: 40px 0;
  text-align: center;
}

.post-header {
  margin-bottom: 24px;
}

.post-title {
  font-size: 26px;
  font-weight: 600;
  margin: 0 0 16px;
  color: #303133;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-name {
  font-weight: 500;
  font-size: 16px;
  color: #303133;
}

.post-stats {
  display: flex;
  align-items: center;
  color: #909399;
  gap: 16px;
  font-size: 14px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.like-btn {
  cursor: pointer;
}

.like-btn:hover {
  color: #409EFF;
}

.post-time {
  font-size: 14px;
  color: #909399;
}

.post-cover {
  margin-bottom: 24px;
  text-align: center;
}

.post-cover img {
  max-width: 100%;
  max-height: 500px;
  object-fit: contain;
  border-radius: 4px;
}

.post-content {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}

.post-actions {
  display: flex;
  justify-content: flex-start;
  gap: 15px;
  margin-top: 30px;
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.comments-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  padding: 30px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title h2 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #303133;
}

.comment-form {
  margin-bottom: 30px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

.comments-list {
  margin-top: 20px;
}

.comment-thread {
  margin-bottom: 24px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.comment-thread:last-child {
  border-bottom: none;
}

.comment-item {
  display: flex;
  margin-bottom: 16px;
  gap: 12px;
}

.comment-main {
  flex: 1;
}

.comment-header {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-weight: 500;
  color: #303133;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  margin-bottom: 8px;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
  cursor: pointer;
}

.action-item:hover {
  color: #409EFF;
}

.nested-replies {
  margin-left: 12px;
  margin-top: 12px;
  padding-left: 12px;
  border-left: 2px solid #ebeef5;
}

.reply-item {
  margin-bottom: 12px;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-to-comment {
  background-color: #f7f9fc;
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 16px;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.reply-user {
  font-weight: 500;
}

.reply-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.reply-form {
  margin-top: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 