<template>
  <div class="repair-record-manage">
    <div class="search-form">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="描述/位置/备注" clearable />
        </el-form-item>
        <el-form-item label="维修类型">
          <el-select v-model="queryParams.typeId" placeholder="请选择维修类型" clearable filterable style="width: 250px;">
            <el-option 
              v-for="item in typeOptions" 
              :key="item.id" 
              :label="item.name || item.repairTypeName" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="维修工">
          <el-select v-model="queryParams.workerId" placeholder="请选择维修工" clearable filterable style="width: 250px;">
            <el-option 
              v-for="item in workerOptions" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button v-if="!isWorker" type="primary" @click="handleAdd">新增记录</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userName" label="用户名称" width="120" />
      <el-table-column prop="name" label="报修标题" width="150" />
      <el-table-column prop="typeId" label="维修类型" width="150">
        <template #default="{row}">
          {{ getTypeName(row.typeId) }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="故障描述" width="180" />
      <el-table-column prop="location" label="位置" width="150" />
      <el-table-column prop="phone" label="联系方式" width="120" />
      <el-table-column prop="images" label="图片" width="100">
        <template #default="{row}">
          <el-image 
            v-if="row.images || row.img" 
            :src="(row.images || row.img).split(',')[0]" 
            style="width: 50px; height: 50px" 
            :preview-src-list="(row.images || row.img).split(',')"
          />
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="getStatusType(row.status)">{{ typeof row.status === 'string' ? row.status : getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="workerName" label="维修工" width="150" />
      <el-table-column prop="isCharge" label="收费" width="80">
        <template #default="{row}">
          <el-tag :type="row.isCharge === 1 ? 'danger' : 'success'">
            {{ row.isCharge === 1 ? '收费' : '免费' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{row}">
          {{ row.isCharge === 1 && row.price ? `¥${row.price}` : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" width="150" />
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{row}">
          {{ new Date(row.createTime).toLocaleString() }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="240">
        <template #default="{row}">
          <el-button v-if="!isWorker" type="success" link @click="handleAssign(row)">派单</el-button>
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="!isWorker" type="danger" link @click="handleDelete(row)">删除</el-button>
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
      v-model="assignDialogVisible"
      title="派单"
      width="600px"
    >
      <div class="assign-dialog-content">
        <div v-if="currentAssignRecord" class="record-info">
          <h4>报修信息</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="报修标题">{{ currentAssignRecord.name }}</el-descriptions-item>
            <el-descriptions-item label="维修类型">{{ getTypeName(currentAssignRecord.typeId) }}</el-descriptions-item>
            <el-descriptions-item label="位置">{{ currentAssignRecord.location }}</el-descriptions-item>
            <el-descriptions-item label="联系方式">{{ currentAssignRecord.phone }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div v-if="recommendedWorkers.length > 0" class="worker-section">
          <h4>
            <el-icon style="vertical-align: middle; margin-right: 5px;"><Star /></el-icon>
            推荐维修工（匹配标签）
          </h4>
          <div class="worker-list">
            <el-card 
              v-for="worker in recommendedWorkers" 
              :key="worker.id" 
              class="worker-card recommended"
              shadow="hover"
            >
              <div class="worker-info">
                <el-avatar :src="worker.avatar || '/src/assets/images/user-default.png'" :size="50" />
                <div class="worker-details">
                  <div class="worker-name">{{ worker.name }}</div>
                  <div class="worker-tags">
                    <el-tag 
                      v-for="(tag, index) in (worker.tags || '').split(',').filter(t => t.trim())" 
                      :key="index" 
                      size="small"
                      type="success"
                      style="margin-right: 5px;"
                    >
                      {{ tag.trim() }}
                    </el-tag>
                  </div>
                  <div class="worker-contact">{{ worker.phone }}</div>
                </div>
              </div>
              <el-button type="primary" @click="confirmAssign(worker.id)">选择</el-button>
            </el-card>
          </div>
        </div>
        
        <div v-if="otherWorkers.length > 0" class="worker-section">
          <h4>其他维修工</h4>
          <div class="worker-list">
            <el-card 
              v-for="worker in otherWorkers" 
              :key="worker.id" 
              class="worker-card"
              shadow="hover"
            >
              <div class="worker-info">
                <el-avatar :src="worker.avatar || '/src/assets/images/user-default.png'" :size="50" />
                <div class="worker-details">
                  <div class="worker-name">{{ worker.name }}</div>
                  <div class="worker-tags" v-if="worker.tags">
                    <el-tag 
                      v-for="(tag, index) in worker.tags.split(',').filter(t => t.trim())" 
                      :key="index" 
                      size="small"
                      style="margin-right: 5px;"
                    >
                      {{ tag.trim() }}
                    </el-tag>
                  </div>
                  <div class="worker-contact">{{ worker.phone }}</div>
                </div>
              </div>
              <el-button @click="confirmAssign(worker.id)">选择</el-button>
            </el-card>
          </div>
        </div>
        
        <el-empty v-if="recommendedWorkers.length === 0 && otherWorkers.length === 0" description="暂无可用维修工" />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="assignDialogVisible = false">取消</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑报修记录' : '新增报修记录'"
      width="650px"
    >
      <el-form :model="form" :rules="currentRules" ref="formRef" label-width="100px">
        <el-form-item v-if="!isWorker" label="报修标题" prop="name">
          <el-input v-model="form.name" placeholder="请输入报修标题" />
        </el-form-item>
        <el-form-item v-if="!isWorker" label="维修类型" prop="typeId">
          <el-select v-model="form.typeId" placeholder="请选择维修类型" filterable style="width: 100%">
            <el-option 
              v-for="item in typeOptions" 
              :key="item.id" 
              :label="item.name || item.repairTypeName" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isWorker" label="故障描述" prop="description">
          <el-input v-model="form.description" placeholder="请输入故障描述" />
        </el-form-item>
        <el-form-item v-if="!isWorker" label="位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入位置" />
        </el-form-item>
        <el-form-item v-if="!isWorker" label="联系方式" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系方式" maxlength="11" />
        </el-form-item>
        <el-form-item v-if="!isWorker" label="故障图片">
          <el-upload
            class="upload-demo"
            action=""
            :auto-upload="false"
            :on-change="handleImageChange"
            multiple
            :limit="5"
            list-type="picture-card"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="待处理" :value="typeof form.status === 'string' ? '待处理' : 0" />
            <el-option label="处理中" :value="typeof form.status === 'string' ? '处理中' : 1" />
            <el-option label="已完成" :value="typeof form.status === 'string' ? '已完成' : 2" />
            <el-option label="已取消" :value="typeof form.status === 'string' ? '已取消' : 3" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isWorker" label="维修工" prop="workerId">
          <el-select v-model="form.workerId" placeholder="请选择维修工" filterable style="width: 100%">
            <el-option 
              v-for="item in workerOptions" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isWorker" label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item v-if="!isWorker" label="是否收费" prop="isCharge">
          <el-radio-group v-model="form.isCharge">
            <el-radio :label="0">免费</el-radio>
            <el-radio :label="1">收费</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="!isWorker && form.isCharge === 1" label="费用" prop="price">
          <el-input-number v-model="form.price" :min="0" placeholder="请输入费用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { getRepairRecordList, getRepairRecordById, deleteRepairRecord, saveRepairRecord } from '@/api/repairRecord'
import { getRepairTypeList } from '@/api/repairType'
import { getUserList, getUserByToken } from '@/api/user'
import { uploadFile } from '@/api/file'
import { Plus, Star } from '@element-plus/icons-vue'
import { useStore } from '@/store/index.js'
import { useUserStore } from '@/store/user.js'

const store = useStore()
const userStore = useUserStore()
const userInfo = computed(() => store.state.userInfo)

// 判断是否是维修工
const isWorker = computed(() => userStore.isWorker)

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const assignDialogVisible = ref(false)
const formRef = ref(null)
const typeOptions = ref([])
const workerOptions = ref([])
const recommendedWorkers = ref([])
const otherWorkers = ref([])
const currentAssignRecord = ref(null)
const imageFileList = ref([])

const queryParams = reactive({
  keyword: '',
  typeId: '',
  workerId: ''
})

const form = ref({
  id: '',
  userId: '',
  typeId: '',
  description: '',
  location: '',
  phone: '',
  images: '',
  img: '',
  status: 0,
  workerId: '',
  remark: '',
  name: '',
  isCharge: 0,
  price: null
})

const rules = {
  name: [{ required: true, message: '请输入报修标题', trigger: 'blur' }],
  typeId: [{ required: true, message: '请选择维修类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入故障描述', trigger: 'blur' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 维修工的验证规则（只需要状态）
const workerRules = {
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 根据角色选择验证规则
const currentRules = computed(() => {
  return isWorker.value ? workerRules : rules
})

// 获取维修类型列表
const getTypeList = async () => {
  try {
    const res = await getRepairTypeList({
      pageNum: 1,
      pageSize: 100
    })
    if (res.code === 200) {
      typeOptions.value = res.data.records
    }
  } catch (error) {
    console.error('获取维修类型列表失败:', error)
  }
}

// 获取维修工列表
const getWorkerList = async () => {
  try {
    const res = await getUserList({
      pageNum: 1,
      pageSize: 100,
      role: 'worker'
    })
    if (res.code === 200) {
      workerOptions.value = res.data.records
    }
  } catch (error) {
    console.error('获取维修工列表失败:', error)
  }
}

// 获取报修记录列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getRepairRecordList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...queryParams
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取报修记录列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取维修类型名称
const getTypeName = (typeId) => {
  const type = typeOptions.value.find(item => item.id === typeId)
  return type ? (type.name || type.repairTypeName || '未知类型名称') : '未知类型'
}

// 获取状态类型
const getStatusType = (status) => {
  if (typeof status === 'string') {
    switch (status) {
      case '待处理':
        return 'warning'
      case '处理中':
        return 'primary'
      case '已完成':
        return 'success'
      case '已拒绝':
      case '已取消':
        return 'danger'
      default:
        return 'info'
    }
  } else {
    switch (status) {
      case 0:
        return 'warning'
      case 1:
        return 'primary'
      case 2:
        return 'success'
      case 3:
        return 'danger'
      default:
        return 'info'
    }
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0:
      return '待处理'
    case 1:
      return '处理中'
    case 2:
      return '已完成'
    case 3:
      return '已取消'
    default:
      return '未知'
  }
}

const handleQuery = () => {
  pageNum.value = 1
  getList()
}

const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.typeId = ''
  queryParams.workerId = ''
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
    userId: userInfo.value?.id || '',
    typeId: '',
    description: '',
    location: '',
    phone: '',
    images: '',
    img: '',
    status: typeof form.value.status === 'string' ? '待处理' : 0,
    workerId: '',
    remark: '',
    name: '',
    isCharge: 0,
    price: null
  }
  imageFileList.value = []
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getRepairRecordById(row.id)
    if (res.code === 200) {
      form.value = { ...res.data }
      dialogVisible.value = true

      // 处理图片预览
      if (form.value.images || form.value.img) {
        const imageUrls = (form.value.images || form.value.img).split(',')
        imageFileList.value = imageUrls.map((url, index) => ({
          name: `image-${index}`,
          url
        }))
      } else {
        imageFileList.value = []
      }
    }
  } catch (error) {
    console.error('获取报修记录详情失败:', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该报修记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteRepairRecord(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除报修记录失败:', error)
    }
  })
}

// 处理派单
const handleAssign = async (row) => {
  currentAssignRecord.value = row
  
  // 获取维修类型名称
  const typeName = getTypeName(row.typeId)
  
  // 根据维修类型匹配维修工的tags
  recommendedWorkers.value = []
  otherWorkers.value = []
  
  workerOptions.value.forEach(worker => {
    if (worker.tags) {
      // 将tags字符串按英文逗号分割
      const tags = worker.tags.split(',').map(tag => tag.trim())
      // 检查是否有标签匹配维修类型
      const isMatch = tags.some(tag => typeName.includes(tag) || tag.includes(typeName))
      
      if (isMatch) {
        recommendedWorkers.value.push(worker)
      } else {
        otherWorkers.value.push(worker)
      }
    } else {
      otherWorkers.value.push(worker)
    }
  })
  
  assignDialogVisible.value = true
}

// 确认派单
const confirmAssign = async (workerId) => {
  try {
    const res = await saveRepairRecord({
      id: currentAssignRecord.value.id,
      workerId: workerId,
      status: typeof currentAssignRecord.value.status === 'string' ? '处理中' : 1
    })
    
    if (res.code === 200) {
      ElMessage.success('派单成功')
      assignDialogVisible.value = false
      getList()
    }
  } catch (error) {
    console.error('派单失败:', error)
    ElMessage.error('派单失败')
  }
}


const handleImageChange = (file, fileList) => {
  imageFileList.value = fileList
}

const handleSubmit = () => {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      try {
        // 如果是维修工，只提交状态字段
        let submitData
        if (isWorker.value) {
          submitData = {
            id: form.value.id,
            status: form.value.status
          }
        } else {
          // 管理员提交完整数据
          // 上传图片
          const imageUrls = []
          if (imageFileList.value.length > 0) {
            for (const item of imageFileList.value) {
              // 如果是新上传的图片，需要上传到服务器
              if (item.raw) {
                const uploadRes = await uploadFile(item.raw)
                if (uploadRes.code === 200) {
                  imageUrls.push(uploadRes.data)
                }
              } else if (item.url) {
                // 如果是已有的图片，直接使用 URL
                imageUrls.push(item.url)
              }
            }
            form.value.images = imageUrls.join(',')
            // 同时设置img字段，确保兼容性
            form.value.img = form.value.images
          } else {
            form.value.images = ''
            form.value.img = ''
          }
          
          submitData = form.value
        }
        
        // 保存表单
        const res = await saveRepairRecord(submitData)
        
        if (res.code === 200) {
          ElMessage.success(form.value.id ? '编辑成功' : '新增成功')
          dialogVisible.value = false
          getList()
        }
      } catch (error) {
        console.error('保存报修记录失败:', error)
      }
    }
  })
}

onMounted(async () => {
  // 获取当前登录用户信息
  if (!userInfo.value) {
    try {
      const res = await getUserByToken()
      if (res.code === 200) {
        store.state.userInfo = res.data
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
  
  // 获取维修类型和维修工列表
  await Promise.all([getTypeList(), getWorkerList()])
  
  // 获取报修记录列表
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

.upload-demo :deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

.upload-demo :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}

.assign-dialog-content {
  max-height: 600px;
  overflow-y: auto;
}

.record-info {
  margin-bottom: 20px;
}

.record-info h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
}

.worker-section {
  margin-top: 20px;
}

.worker-section h4 {
  margin: 0 0 15px 0;
  font-size: 15px;
  color: #303133;
  display: flex;
  align-items: center;
}

.worker-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.worker-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
}

.worker-card.recommended {
  border: 2px solid #67c23a;
  background-color: #f0f9ff;
}

.worker-info {
  display: flex;
  align-items: center;
  gap: 15px;
  flex: 1;
}

.worker-details {
  flex: 1;
}

.worker-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.worker-tags {
  margin-bottom: 5px;
}

.worker-contact {
  font-size: 13px;
  color: #909399;
}
</style>