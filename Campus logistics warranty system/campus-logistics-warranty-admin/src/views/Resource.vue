<template>
  <div class="resource-container">
    <div class="header">
      <div class="title">资源管理</div>
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item>
          <el-input v-model="queryParams.keyword" placeholder="搜索资源" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="handleAdd">新增资源</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="main">
      <el-table :data="resourceList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column label="封面" width="100">
          <template #default="scope">
            <el-image 
              v-if="scope.row.cover" 
              :src="scope.row.cover" 
              :preview-src-list="[scope.row.cover]"
              style="width: 60px; height: 60px; object-fit: cover;"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="url" label="资源链接">
          <template #default="scope">
            <el-link type="primary" :href="scope.row.url" target="_blank" v-if="scope.row.url">
              {{ scope.row.url }}
            </el-link>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
            <el-button type="success" size="small" @click="handlePreviewPDF(scope.row)" v-if="scope.row.url">查看</el-button>
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
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 资源表单弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="resourceForm" label-width="100px" ref="resourceFormRef" :rules="rules">
        <el-form-item label="标题" prop="title">
          <el-input v-model="resourceForm.title" placeholder="请输入资源标题" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="resourceForm.description" type="textarea" :rows="4" placeholder="请输入资源描述" />
        </el-form-item>
        <el-form-item label="资源文件">
          <el-upload
            class="resource-upload"
            :action="''"
            :http-request="handleUpload"
            :before-upload="beforeUploadFile"
            :limit="1"
            :on-exceed="handleExceed"
            accept=".pdf"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">请上传PDF格式资源文件，大小不超过100MB</div>
            </template>
          </el-upload>
          <div v-if="resourceForm.url" class="uploaded-file">
            <el-link type="primary" :underline="false">已上传: {{getFileName(resourceForm.url)}}</el-link>
          </div>
        </el-form-item>
        <el-form-item label="封面图片">
          <el-upload
            class="cover-upload"
            :action="''"
            :http-request="handleCoverUpload"
            :before-upload="beforeUploadCover"
            :limit="1"
            :on-exceed="handleExceed"
            :on-success="handleCoverSuccess"
            :show-file-list="false"
          >
            <el-image
              v-if="resourceForm.cover"
              :src="resourceForm.cover"
              class="cover-image"
            />
            <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="el-upload__tip">请上传封面图片，建议尺寸 300x300，大小不超过5MB</div>
        </el-form-item>
        <el-form-item label="资源链接" prop="url">
          <el-input v-model="resourceForm.url" placeholder="请输入资源链接" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">提交</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getResourceList, getResourceById, deleteResource, saveResource } from '@/api/resource'
import { uploadFile } from '@/api/file'
import { useUserStore } from '@/store/user'
import { Plus } from '@element-plus/icons-vue'

const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const resourceList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const resourceFormRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: ''
})

const resourceForm = reactive({
  id: undefined,
  title: '',
  description: '',
  url: '',
  cover: ''
})

const rules = {
  title: [{ required: true, message: '请输入资源标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入资源描述', trigger: 'blur' }],
  url: [{ required: true, message: '请上传PDF资源文件', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑资源' : '新增资源')

// 加载资源列表
const loadResourceList = async () => {
  loading.value = true
  try {
    const res = await getResourceList(queryParams)
    resourceList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取资源列表失败', error)
    ElMessage.error('获取资源列表失败')
  } finally {
    loading.value = false
  }
}

// 查询按钮点击
const handleQuery = () => {
  queryParams.pageNum = 1
  loadResourceList()
}

// 重置查询
const resetQuery = () => {
  queryParams.pageNum = 1
  queryParams.keyword = ''
  loadResourceList()
}

// 分页大小改变
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  loadResourceList()
}

// 页码改变
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  loadResourceList()
}

// 新增资源
const handleAdd = () => {
  isEdit.value = false
  resourceForm.id = undefined
  resourceForm.title = ''
  resourceForm.description = ''
  resourceForm.url = ''
  resourceForm.cover = ''
  dialogVisible.value = true
}

// 编辑资源
const handleEdit = async (row) => {
  isEdit.value = true
  try {
    const res = await getResourceById(row.id)
    Object.assign(resourceForm, res.data)
    dialogVisible.value = true
  } catch (error) {
    console.error('获取资源详情失败', error)
    ElMessage.error('获取资源详情失败')
  }
}

// 删除资源
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该资源?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteResource(row.id)
      ElMessage.success('删除成功')
      loadResourceList()
    } catch (error) {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 上传前检查
const beforeUploadFile = (file) => {
  const maxSize = 100 * 1024 * 1024 // 100MB
  const isPdf = file.type === 'application/pdf'
  
  if (!isPdf) {
    ElMessage.error('只能上传PDF格式文件')
    return false
  }
  
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过100MB')
    return false
  }
  return true
}

// 封面上传前检查
const beforeUploadCover = (file) => {
  const maxSize = 5 * 1024 * 1024 // 5MB
  const isImage = file.type.startsWith('image/')
  
  if (!isImage) {
    ElMessage.error('请上传图片格式文件')
    return false
  }
  
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  
  return true
}

// 上传超出限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传一个文件')
}

// 封面上传成功
const handleCoverSuccess = (res) => {
  resourceForm.cover = res.data
}

// 自定义上传
const handleUpload = async (options) => {
  const { file } = options
  try {
    const res = await uploadFile(file)
    resourceForm.url = res.data // 保存PDF文件路径
    ElMessage.success('文件上传成功')
  } catch (error) {
    console.error('上传失败', error)
    ElMessage.error('文件上传失败')
  }
}

// 封面图片上传
const handleCoverUpload = async (options) => {
  const { file } = options
  try {
    const res = await uploadFile(file)
    resourceForm.cover = res.data
    ElMessage.success('封面上传成功')
  } catch (error) {
    console.error('上传失败', error)
    ElMessage.error('封面上传失败')
  }
}

// 提交表单
const submitForm = async () => {
  if (!resourceFormRef.value) return
  
  await resourceFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      await saveResource(resourceForm)
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      loadResourceList()
    } catch (error) {
      console.error('保存失败', error)
      ElMessage.error('保存失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 预览PDF文件
const handlePreviewPDF = (row) => {
  if (row.url) {
    window.open(row.url, '_blank')
  } else {
    ElMessage.warning('该资源没有可预览的PDF文件')
  }
}

// 从URL中获取文件名
const getFileName = (url) => {
  if (!url) return ''
  const parts = url.split('/')
  return parts[parts.length - 1]
}

onMounted(() => {
  // 获取用户信息
  if (!userStore.userInfo) {
    userStore.getUserInfo()
  }
  // 加载资源列表
  loadResourceList()
})
</script>

<style lang="less" scoped>
.resource-container {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .title {
      font-size: 20px;
      font-weight: bold;
    }
  }

  .main {
    background-color: #fff;
    padding: 20px;
    border-radius: 5px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
  }
  
  .resource-upload {
    width: 100%;
  }
  
  .cover-upload {
    display: flex;
    flex-direction: column;
    
    .cover-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 100px;
      height: 100px;
      text-align: center;
      line-height: 100px;
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
    }
    
    .cover-image {
      width: 100px;
      height: 100px;
      border-radius: 6px;
      object-fit: cover;
      cursor: pointer;
    }
  }
}
</style> 