<template>
  <div class="post-container">
    <div class="header">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="标题/摘要/内容" clearable></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="待审核" :value="0"></el-option>
            <el-option label="已审核" :value="1"></el-option>
            <el-option label="已置顶" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button type="success" @click="handleAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="postList" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="title" label="标题" width="180"></el-table-column>
      <el-table-column prop="summary" label="摘要"></el-table-column>
      <el-table-column label="封面图" width="120">
        <template #default="scope">
          <el-image 
            v-if="scope.row.cover" 
            :src="scope.row.cover" 
            style="width: 80px; height: 80px;"
            :preview-src-list="[scope.row.cover]">
          </el-image>
          <span v-else>无图片</span>
        </template>
      </el-table-column>
      <el-table-column label="作者" width="120">
        <template #default="scope">
          <div class="author-info">
            <el-avatar v-if="scope.row.authorAvatar" :src="scope.row.authorAvatar" size="small"></el-avatar>
            <span>{{ scope.row.authorName || `用户${scope.row.authorId}` }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="views" label="浏览量" width="100"></el-table-column>
      <el-table-column prop="likes" label="点赞数" width="100"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="info">待审核</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="success">已审核</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="warning">已置顶</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdTime" label="创建时间" width="180"></el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          <el-button 
            size="small" 
            type="warning" 
            v-if="scope.row.status === 0"
            @click="handleChangeStatus(scope.row, 1)">审核</el-button>
          <el-button 
            size="small" 
            type="success" 
            v-if="scope.row.status === 1"
            @click="handleChangeStatus(scope.row, 2)">置顶</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="queryParams.pageNum"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="queryParams.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </div>

    <!-- 添加/编辑帖子对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="60%">
      <el-form :model="postForm" label-width="80px" :rules="rules" ref="postFormRef">
        <el-form-item label="标题" prop="title">
          <el-input v-model="postForm.title" placeholder="请输入标题"></el-input>
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input v-model="postForm.summary" type="textarea" :rows="3" placeholder="请输入摘要"></el-input>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="postForm.content" type="textarea" :rows="10" placeholder="请输入内容"></el-input>
        </el-form-item>
        <el-form-item label="封面图">
          <div class="upload-box">
            <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :http-request="handleUpload">
              <img v-if="postForm.cover" :src="postForm.cover" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><plus /></el-icon>
            </el-upload>
            <div v-if="postForm.cover" class="image-actions">
              <el-button type="danger" size="small" @click="removeImage">删除图片</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="postForm.status" placeholder="请选择状态">
            <el-option label="待审核" :value="0"></el-option>
            <el-option label="已审核" :value="1"></el-option>
            <el-option label="已置顶" :value="2"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { getPostList, getPostById, savePost, deletePost } from '@/api/post'
import { uploadFile } from '@/api/file'
import { getUserByToken } from '@/api/user'
import { useStore } from '@/store'
import { Plus } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const store = useStore()
const userInfo = ref({})

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: null,
  authorId: null
})

// 帖子列表数据
const postList = ref([])
const total = ref(0)

// 对话框相关
const dialog = reactive({
  visible: false,
  title: '添加帖子'
})

// 表单相关
const postFormRef = ref(null)
const postForm = reactive({
  id: null,
  title: '',
  summary: '',
  content: '',
  cover: '',
  status: 0,
  authorId: 1 // 默认作者ID，实际应从用户信息中获取
})

// 表单校验规则
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  summary: [{ required: true, message: '请输入摘要', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  return dateTimeStr.replace('T', ' ')
}

// 初始化
onMounted(() => {
  getUserInfo()
  getList()
})

// 获取列表数据
const getList = async () => {
  try {
    const res = await getPostList(queryParams)
    if (res.code === 200) {
      postList.value = res.data.records.map(item => {
        return {
          ...item,
          createdTime: formatDateTime(item.createdTime),
          updatedTime: formatDateTime(item.updatedTime)
        }
      })
      total.value = res.data.total
    } else {
      ElMessage.error(res.msg || '获取帖子列表失败')
    }
  } catch (error) {
    console.error('获取帖子列表出错', error)
    ElMessage.error('获取帖子列表出错')
  }
}

// 查询按钮
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
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

// 新增按钮
const handleAdd = () => {
  resetForm()
  dialog.title = '添加帖子'
  dialog.visible = true
}

// 编辑按钮
const handleEdit = async (row) => {
  resetForm()
  const res = await getPostById(row.id)
  if (res.code === 200) {
    Object.assign(postForm, res.data)
    dialog.title = '编辑帖子'
    dialog.visible = true
  } else {
    ElMessage.error(res.msg || '获取帖子详情失败')
  }
}

// 删除按钮
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该帖子吗?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deletePost(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getList()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      console.error('删除帖子出错', error)
      ElMessage.error('删除帖子出错')
    }
  }).catch(() => {})
}

// 修改状态
const handleChangeStatus = async (row, status) => {
  try {
    const postData = { ...row, status }
    const res = await savePost(postData)
    if (res.code === 200) {
      ElMessage.success('状态修改成功')
      getList()
    } else {
      ElMessage.error(res.msg || '状态修改失败')
    }
  } catch (error) {
    console.error('修改状态出错', error)
    ElMessage.error('修改状态出错')
  }
}

// 提交表单
const submitForm = () => {
  postFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await savePost(postForm)
        if (res.code === 200) {
          ElMessage.success('保存成功')
          dialog.visible = false
          getList() // 不需要await，让它异步执行即可
        } else {
          ElMessage.error(res.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存帖子出错', error)
        ElMessage.error('保存帖子出错')
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  postForm.id = null
  postForm.title = ''
  postForm.summary = ''
  postForm.content = ''
  postForm.cover = ''
  postForm.status = 0
  postForm.authorId = userInfo.value.id || 1 // 使用当前用户的ID
  
  if (postFormRef.value) {
    postFormRef.value.resetFields()
  }
}

// 上传前验证
const beforeUpload = (file) => {
  // 验证文件类型
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  
  // 验证文件大小 (小于2MB)
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB!')
    return false
  }
  
  return true
}

// 处理文件上传
const handleUpload = async (options) => {
  try {
    const res = await uploadFile(options.file)
    if (res.code === 200) {
      postForm.cover = res.data
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error(res.msg || '图片上传失败')
    }
  } catch (error) {
    console.error('图片上传出错', error)
    ElMessage.error('图片上传出错')
  }
}

// 删除已上传的图片
const removeImage = () => {
  postForm.cover = ''
}

// 获取当前用户信息
const getUserInfo = async () => {
  try {
    const res = await getUserByToken()
    if (res.code === 200) {
      userInfo.value = res.data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}
</script>

<style scoped>
.post-container {
  padding: 20px;
}
.header {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  text-align: right;
}
.upload-box {
  display: flex;
  align-items: center;
}
.avatar-uploader {
  width: 150px;
  height: 150px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader:hover {
  border-color: #409EFF;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 150px;
  height: 150px;
  line-height: 150px;
  text-align: center;
}
.avatar {
  width: 150px;
  height: 150px;
  display: block;
  object-fit: cover;
}
.image-actions {
  margin-left: 20px;
}
.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style> 