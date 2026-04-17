<template>
  <div class="comment-container">
    <div class="header">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="评论内容" clearable></el-input>
        </el-form-item>
        <el-form-item label="帖子ID">
          <el-input v-model="queryParams.postId" placeholder="帖子ID" clearable></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.isDeleted" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="正常" :value="0"></el-option>
            <el-option label="已删除" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="commentList" border style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="postId" label="帖子ID" width="100"></el-table-column>
      <el-table-column prop="userId" label="用户ID" width="100"></el-table-column>
      <el-table-column prop="content" label="评论内容" show-overflow-tooltip></el-table-column>
      <el-table-column prop="likeCount" label="点赞数" width="80"></el-table-column>
      <el-table-column prop="parentId" label="父评论ID" width="100">
        <template #default="scope">
          {{ scope.row.parentId || '无' }}
        </template>
      </el-table-column>
      <el-table-column prop="isDeleted" label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.isDeleted === 0 ? 'success' : 'danger'">
            {{ scope.row.isDeleted === 0 ? '正常' : '已删除' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdTime" label="创建时间" width="180">
        <template #default="scope">
          {{ scope.row.createdTime ? new Date(scope.row.createdTime).toLocaleString() : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button type="primary" link @click="handleView(scope.row)">查看</el-button>
          <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange">
      </el-pagination>
    </div>

    <!-- 查看评论对话框 -->
    <el-dialog
      v-model="viewDialog.visible"
      title="查看评论"
      width="50%"
    >
      <div class="comment-detail">
        <div class="detail-item">
          <span class="label">评论ID：</span>
          <span>{{ currentComment.id }}</span>
        </div>
        <div class="detail-item">
          <span class="label">帖子ID：</span>
          <span>{{ currentComment.postId }}</span>
        </div>
        <div class="detail-item">
          <span class="label">用户ID：</span>
          <span>{{ currentComment.userId }}</span>
        </div>
        <div class="detail-item">
          <span class="label">评论内容：</span>
          <div class="content-box">{{ currentComment.content }}</div>
        </div>
        <div class="detail-item">
          <span class="label">点赞数：</span>
          <span>{{ currentComment.likeCount || 0 }}</span>
        </div>
        <div class="detail-item">
          <span class="label">父评论ID：</span>
          <span>{{ currentComment.parentId || '无' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">状态：</span>
          <el-tag :type="currentComment.isDeleted === 0 ? 'success' : 'danger'">
            {{ currentComment.isDeleted === 0 ? '正常' : '已删除' }}
          </el-tag>
        </div>
        <div class="detail-item">
          <span class="label">创建时间：</span>
          <span>{{ currentComment.createdTime ? new Date(currentComment.createdTime).toLocaleString() : '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">更新时间：</span>
          <span>{{ currentComment.updatedTime ? new Date(currentComment.updatedTime).toLocaleString() : '-' }}</span>
        </div>
      </div>

      <!-- 如果有回复，显示回复列表 -->
      <div v-if="currentComment.reply && currentComment.reply.length > 0" class="reply-list">
        <h4>回复列表</h4>
        <el-table :data="currentComment.reply" border style="width: 100%">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column prop="userId" label="用户ID" width="100"></el-table-column>
          <el-table-column prop="content" label="回复内容"></el-table-column>
          <el-table-column prop="likeCount" label="点赞数" width="80"></el-table-column>
          <el-table-column prop="isDeleted" label="状态" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.isDeleted === 0 ? 'success' : 'danger'">
                {{ scope.row.isDeleted === 0 ? '正常' : '已删除' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdTime" label="创建时间" width="180">
            <template #default="scope">
              {{ scope.row.createdTime ? new Date(scope.row.createdTime).toLocaleString() : '-' }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 回复评论对话框 -->
    <el-dialog
      v-model="replyDialog.visible"
      title="回复评论"
      width="50%"
    >
      <el-form :model="replyForm" :rules="replyRules" ref="replyFormRef" label-width="100px">
        <el-form-item label="回复内容" prop="content">
          <el-input v-model="replyForm.content" type="textarea" :rows="4" placeholder="请输入回复内容"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="replyDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitReply">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCommentList, getCommentById, getCommentByPostId, saveComment, deleteComment } from '@/api/comment'
import { useRouter } from 'vue-router'

const router = useRouter()

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  postId: '',
  isDeleted: ''
})

// 数据相关
const commentList = ref([])
const total = ref(0)
const loading = ref(false)

// 查看评论对话框
const viewDialog = reactive({
  visible: false
})

// 回复评论对话框
const replyDialog = reactive({
  visible: false
})

// 当前查看/操作的评论
const currentComment = ref({})

// 回复表单
const replyFormRef = ref(null)
const replyForm = reactive({
  postId: null,
  parentId: null,
  content: '',
  likeCount: 0,
  isDeleted: 0
})

// 回复表单验证规则
const replyRules = {
  content: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
}

// 初始化
onMounted(() => {
  getList()
})

// 获取列表数据
const getList = async () => {
  loading.value = true
  try {
    const res = await getCommentList(queryParams)
    if (res.code === 200) {
      // 判断返回数据结构
      if (res.data && res.data.records) {
        commentList.value = res.data.records
        total.value = res.data.total
      } else if (Array.isArray(res.data)) {
        commentList.value = res.data
        total.value = res.data.length
      } else {
        commentList.value = []
        total.value = 0
      }
    } else {
      ElMessage.error(res.msg || '获取评论列表失败')
    }
  } catch (error) {
    console.error('获取评论列表出错', error)
    ElMessage.error('获取评论列表出错')
  } finally {
    loading.value = false
  }
}

// 查询按钮
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.postId = ''
  queryParams.isDeleted = ''
  handleQuery()
}

// 分页大小变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  getList()
}

// 页码变化
const handleCurrentChange = (page) => {
  queryParams.pageNum = page
  getList()
}

// 查看评论详情
const handleView = async (row) => {
  try {
    // 如果是一级评论，尝试获取其回复
    if (!row.parentId || row.parentId === 0) {
      const res = await getCommentByPostId(row.postId)
      if (res.code === 200) {
        // 找到当前评论及其回复
        const rootComment = res.data.find(item => item.id === row.id)
        if (rootComment) {
          currentComment.value = rootComment
        } else {
          currentComment.value = row
        }
      }
    } else {
      // 子评论直接显示
      currentComment.value = row
    }
    viewDialog.visible = true
  } catch (error) {
    console.error('获取评论详情出错', error)
    ElMessage.error('获取评论详情出错')
  }
}

// 删除评论
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该评论吗?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteComment(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getList()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      console.error('删除评论出错', error)
      ElMessage.error('删除评论出错')
    }
  }).catch(() => {})
}

// 查看关联帖子
const viewPost = (row) => {
  if (row.postId) {
    router.push(`/post?id=${row.postId}`)
  }
}

// 回复评论
const handleReply = (row) => {
  replyForm.postId = row.postId
  replyForm.parentId = row.id
  replyForm.content = ''
  replyDialog.visible = true
}

// 提交回复
const submitReply = () => {
  replyFormRef.value?.validate(async (valid) => {
    if (valid) {
      try {
        const res = await saveComment(replyForm)
        if (res.code === 200) {
          ElMessage.success('回复成功')
          replyDialog.visible = false
          getList()
        } else {
          ElMessage.error(res.msg || '回复失败')
        }
      } catch (error) {
        console.error('提交回复出错', error)
        ElMessage.error('提交回复出错')
      }
    }
  })
}
</script>

<style scoped>
.comment-container {
  padding: 20px;
}
.header {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  text-align: right;
}
.comment-detail {
  padding: 10px;
}
.detail-item {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}
.detail-item .label {
  font-weight: bold;
  width: 100px;
}
.content-box {
  border: 1px solid #eee;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  flex: 1;
}
.reply-list {
  margin-top: 20px;
  border-top: 1px solid #eee;
  padding-top: 20px;
}
</style> 