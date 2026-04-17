<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useStore } from '@/store'
import { getMessageList, saveMessage } from '@/api/message'

const userStore = useStore()

// 活动标签页
const activeTab = ref('submit')

// 留言表单
const messageForm = reactive({
  content: '',
  img: ''
})

// 表单验证规则
const messageRules = {
  content: [
    { required: true, message: '请输入留言内容', trigger: 'blur' }
  ]
}

// 表单引用
const messageFormRef = ref(null)

// 加载状态
const loading = ref(false)
const tableLoading = ref(false)

// 我的留言列表
const myMessages = ref([])
const total = ref(0)

// 分页参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

// 加载我的留言列表
const loadMyMessages = async () => {
  tableLoading.value = true
  try {
    const res = await getMessageList(queryParams)
    myMessages.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取留言列表失败', error)
    ElMessage.error('获取留言列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 页面大小变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  loadMyMessages()
}

// 页码变化
const handleCurrentChange = (current) => {
  queryParams.pageNum = current
  loadMyMessages()
}

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
  messageForm.img = response.data
  ElMessage.success('图片上传成功')
}

// 图片上传失败
const handleUploadError = () => {
  ElMessage.error('图片上传失败')
}

// 提交留言
const submitMessage = async () => {
  if (messageFormRef.value) {
    await messageFormRef.value.validate(async (valid) => {
      if (valid) {
        loading.value = true
        try {
          await saveMessage(messageForm)
          ElMessage.success('留言提交成功')
          messageForm.content = ''
          messageForm.img = ''
          // 切换到我的留言标签页并刷新列表
          activeTab.value = 'my'
          loadMyMessages()
        } catch (error) {
          console.error('提交留言失败', error)
          ElMessage.error('提交留言失败')
        } finally {
          loading.value = false
        }
      }
    })
  }
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  return status === 0 ? 'warning' : 'success'
}

// 获取状态文本
const getStatusText = (status) => {
  return status === 0 ? '待回复' : '已回复'
}

onMounted(() => {
  loadMyMessages()
})
</script>

<template>
  <div class="message-container">
    <h1>留言板</h1>
    
    <el-card class="message-card">
      <el-tabs v-model="activeTab" @tab-click="loadMyMessages">
        <el-tab-pane label="提交新留言" name="submit">
          <!-- 留言表单 -->
          <el-form
            ref="messageFormRef"
            :model="messageForm"
            :rules="messageRules"
            label-width="80px"
          >
            <el-form-item label="内容" prop="content">
              <el-input
                v-model="messageForm.content"
                type="textarea"
                :rows="4"
                placeholder="请输入留言内容"
              />
            </el-form-item>
            
            <el-form-item label="图片">
              <el-upload
                class="avatar-uploader"
                action="/api/file/upload"
                :headers="{ Authorization: userStore.token }"
                :show-file-list="false"
                :before-upload="beforeUpload"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
              >
                <img v-if="messageForm.img" :src="messageForm.img" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">支持JPG、PNG格式，不超过2MB</div>
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                @click="submitMessage"
              >
                提交留言
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="我的留言" name="my">
          <el-table
            v-loading="tableLoading"
            :data="myMessages"
            style="width: 100%"
            row-key="id"
          >
            <el-table-column label="留言内容" prop="content" show-overflow-tooltip />
            <el-table-column label="图片" width="100">
              <template #default="scope">
                <el-image
                  v-if="scope.row.img"
                  :src="scope.row.img"
                  :preview-src-list="[scope.row.img]"
                  style="width: 50px; height: 50px"
                  fit="cover"
                />
                <span v-else>无</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusTagType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="提交时间" prop="createTime" width="180" />
            <el-table-column label="回复" min-width="200">
              <template #default="scope">
                <div v-if="scope.row.reply">{{ scope.row.reply }}</div>
                <span v-else class="no-reply">暂无回复</span>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页 -->
          <div class="pagination-container">
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
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.message-container {
  padding: 20px;
}

h1 {
  margin-bottom: 20px;
  font-size: 24px;
}

.message-card {
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
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

.no-reply {
  color: #909399;
  font-style: italic;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 