<template>
  <div class="notice-manage">
    <div class="search-form">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="请输入标题或内容" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 150px;">
            <el-option label="已发布" :value="1" />
            <el-option label="已过期" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="handleAdd">新增公告</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" width="200" />
      <el-table-column prop="img" label="图片" width="120">
        <template #default="{row}">
          <el-image 
            style="width: 80px; height: 50px" 
            :src="row.img" 
            fit="cover"
            :preview-src-list="[row.img]">
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '已发布' : '已过期' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{row}">
          {{ new Date(row.createTime).toLocaleString() }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="180">
        <template #default="{row}">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑公告' : '新增公告'"
      width="700px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">已发布</el-radio>
            <el-radio :label="2">已过期</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="图片" prop="img">
          <div class="image-upload-wrapper">
            <el-upload
              class="image-uploader"
              action=""
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleImageChange"
            >
              <div v-if="form.img" class="image-preview">
                <img :src="form.img" class="notice-image" />
                <div class="image-edit-overlay">
                  <el-icon><EditPen /></el-icon>
                </div>
              </div>
              <div v-else class="image-empty">
                <el-icon class="image-icon"><Plus /></el-icon>
                <div class="image-text">点击上传图片</div>
              </div>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="10" 
            placeholder="请输入公告内容" 
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

    <!-- 查看公告详情 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="公告详情"
      width="700px"
    >
      <div class="notice-view">
        <h2 class="notice-title">{{ viewForm.title }}</h2>
        <div class="notice-time">发布时间：{{ new Date(viewForm.createTime).toLocaleString() }}</div>
        <div class="notice-status">
          状态：
          <el-tag :type="viewForm.status === 1 ? 'success' : 'info'">
            {{ viewForm.status === 1 ? '已发布' : '已过期' }}
          </el-tag>
        </div>
        <div class="notice-img" v-if="viewForm.img">
          <img :src="viewForm.img" style="max-width: 100%; margin: 20px 0;" />
        </div>
        <div class="notice-content">{{ viewForm.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getNoticeList, getNoticeById, deleteNotice, saveNotice } from '@/api/notice'
import { uploadFile } from '@/api/file'
import { Plus, EditPen } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const formRef = ref(null)
const imageFile = ref(null)

const queryParams = reactive({
  keyword: '',
  status: ''
})

const form = ref({
  id: '',
  title: '',
  content: '',
  img: '',
  status: 1
})

const viewForm = ref({
  id: '',
  title: '',
  content: '',
  img: '',
  status: 1,
  createTime: ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  img: [{ required: true, message: '请上传图片', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getNoticeList({
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
    console.error('获取公告列表失败:', error)
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

const resetForm = () => {
  form.value = {
    id: '',
    title: '',
    content: '',
    img: '',
    status: 1
  }
  imageFile.value = null
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getNoticeById(row.id)
    if (res.code === 200) {
      form.value = { ...res.data }
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取公告详情失败:', error)
  }
}

const handleView = async (row) => {
  try {
    const res = await getNoticeById(row.id)
    if (res.code === 200) {
      viewForm.value = { ...res.data }
      viewDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取公告详情失败:', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该公告吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteNotice(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除公告失败:', error)
    }
  })
}

const handleImageChange = (file) => {
  imageFile.value = file.raw
  // 预览
  const reader = new FileReader()
  reader.onload = (e) => {
    form.value.img = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const handleSubmit = () => {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      try {
        // 上传图片
        if (imageFile.value) {
          const uploadRes = await uploadFile(imageFile.value)
          if (uploadRes.code === 200) {
            form.value.img = uploadRes.data
          } else {
            ElMessage.error('图片上传失败')
            return
          }
        }
        
        const res = await saveNotice(form.value)
        
        if (res.code === 200) {
          ElMessage.success(form.value.id ? '编辑成功' : '新增成功')
          dialogVisible.value = false
          getList()
        }
      } catch (error) {
        console.error('保存公告失败:', error)
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

.notice-view {
  padding: 0 20px;
}

.notice-title {
  text-align: center;
  font-size: 22px;
  margin-bottom: 10px;
}

.notice-time {
  text-align: center;
  color: #999;
  font-size: 14px;
  margin-bottom: 10px;
}

.notice-status {
  text-align: center;
  margin-bottom: 20px;
}

.notice-content {
  line-height: 1.8;
  white-space: pre-wrap;
}

.image-upload-wrapper {
  width: 300px;
  height: 150px;
}

.image-uploader {
  width: 100%;
  height: 100%;
}

.image-empty {
  width: 300px;
  height: 150px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fafafa;
}

.image-empty:hover {
  border-color: #409EFF;
  background-color: #f0f7ff;
}

.image-icon {
  font-size: 28px;
  color: #8c939d;
  margin-bottom: 8px;
}

.image-text {
  font-size: 14px;
  color: #8c939d;
}

.image-preview {
  width: 300px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  border: 1px solid #e0e0e0;
}

.notice-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-edit-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: #fff;
  font-size: 28px;
}

.image-preview:hover .image-edit-overlay {
  opacity: 1;
}
</style> 