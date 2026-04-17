<template>
  <div class="message-manage">
    <div class="search-form">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="请输入留言内容或回复" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 150px;">
            <el-option label="待回复" :value="0" />
            <el-option label="已回复" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userName" label="用户名" width="120" />
      <el-table-column prop="content" label="留言内容" show-overflow-tooltip />
      <el-table-column prop="img" label="图片" width="100">
        <template #default="{row}">
          <el-image 
            v-if="row.img"
            style="width: 80px; height: 50px" 
            :src="row.img" 
            fit="cover"
            :preview-src-list="[row.img]">
          </el-image>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status === 0 ? 'warning' : 'success'">
            {{ row.status === 0 ? '待回复' : '已回复' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reply" label="回复内容" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{row}">
          {{ new Date(row.createTime).toLocaleString() }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="180">
        <template #default="{row}">
          <el-button type="primary" link @click="handleReply(row)">回复</el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          <el-button type="success" link @click="handleView(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 回复留言对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="回复留言"
      width="600px"
    >
      <div class="message-content">
        <div class="user-info">
          <span class="username">{{ form.userName }}</span>
          <span class="time">{{ new Date(form.createTime).toLocaleString() }}</span>
        </div>
        <div class="content-box">{{ form.content }}</div>
        <div class="image-box" v-if="form.img">
          <el-image 
            style="max-width: 100%; max-height: 300px" 
            :src="form.img" 
            :preview-src-list="[form.img]">
          </el-image>
        </div>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="回复内容" prop="reply">
          <el-input 
            v-model="form.reply" 
            type="textarea" 
            :rows="5" 
            placeholder="请输入回复内容" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看留言详情 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="留言详情"
      width="600px"
    >
      <div class="message-view">
        <div class="message-item user-message">
          <div class="message-header">
            <span class="message-name">用户：{{ viewForm.userName }}</span>
            <span class="message-time">{{ new Date(viewForm.createTime).toLocaleString() }}</span>
          </div>
          <div class="message-body">{{ viewForm.content }}</div>
          <div class="message-image" v-if="viewForm.img">
            <el-image 
              style="max-width: 100%; margin-top: 10px;" 
              :src="viewForm.img" 
              :preview-src-list="[viewForm.img]">
            </el-image>
          </div>
        </div>
        <div class="message-status">
          <el-tag :type="viewForm.status === 0 ? 'warning' : 'success'">
            {{ viewForm.status === 0 ? '待回复' : '已回复' }}
          </el-tag>
        </div>
        <div class="message-item admin-message" v-if="viewForm.reply">
          <div class="message-header">
            <span class="message-name">管理员回复</span>
            <span class="message-time">{{ new Date(viewForm.updateTime).toLocaleString() }}</span>
          </div>
          <div class="message-body">{{ viewForm.reply }}</div>
        </div>
        <div class="no-reply" v-else>暂无回复</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getMessageList, getMessageById, deleteMessage, saveMessage } from '@/api/message'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const formRef = ref(null)

const queryParams = reactive({
  keyword: '',
  status: ''
})

const form = ref({
  id: '',
  userId: '',
  userName: '',
  content: '',
  reply: '',
  img: '',
  status: 0,
  createTime: ''
})

const viewForm = ref({
  id: '',
  userId: '',
  userName: '',
  content: '',
  reply: '',
  img: '',
  status: 0,
  createTime: '',
  updateTime: ''
})

const rules = {
  reply: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getMessageList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: queryParams.keyword,
      status: queryParams.status
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取留言列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pageNum.value = 1
  getList()
}

const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.status = ''
  handleQuery()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  getList()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  getList()
}

const handleReply = async (row) => {
  try {
    const res = await getMessageById(row.id)
    if (res.code === 200) {
      form.value = { ...res.data }
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取留言详情失败:', error)
  }
}

const handleView = async (row) => {
  try {
    const res = await getMessageById(row.id)
    if (res.code === 200) {
      viewForm.value = { ...res.data }
      viewDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取留言详情失败:', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该留言吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteMessage(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除留言失败:', error)
    }
  })
}

const handleSubmit = () => {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      try {
        // 设置状态为已回复
        form.value.status = 1
        
        const res = await saveMessage(form.value)
        
        if (res.code === 200) {
          ElMessage.success('回复成功')
          dialogVisible.value = false
          getList()
        }
      } catch (error) {
        console.error('保存留言回复失败:', error)
      }
    }
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.message-content {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.user-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.username {
  font-weight: bold;
  color: #409EFF;
}

.time {
  color: #999;
  font-size: 14px;
}

.content-box {
  line-height: 1.6;
  white-space: pre-wrap;
  margin-bottom: 10px;
}

.image-box {
  margin-top: 10px;
  text-align: center;
}

.message-view {
  padding: 0 10px;
}

.message-item {
  margin-bottom: 20px;
  padding: 15px;
  border-radius: 8px;
}

.message-status {
  text-align: center;
  margin: 15px 0;
}

.user-message {
  background-color: #f5f7fa;
}

.admin-message {
  background-color: #f0f9eb;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.message-name {
  font-weight: bold;
}

.message-time {
  color: #999;
  font-size: 14px;
}

.message-body {
  line-height: 1.6;
  white-space: pre-wrap;
}

.message-image {
  margin-top: 10px;
}

.no-reply {
  text-align: center;
  color: #999;
  font-style: italic;
  padding: 20px 0;
}
</style> 