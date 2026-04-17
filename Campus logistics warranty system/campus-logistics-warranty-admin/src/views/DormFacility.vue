<template>
  <div class="dorm-facility-manage">
    <div class="search-form">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="请输入物品名称或品牌" clearable />
        </el-form-item>
        <el-form-item label="所属房间">
          <el-select v-model="queryParams.roomId" placeholder="请选择" clearable filterable style="width: 150px;">
            <el-option v-for="room in roomList" :key="room.roomId" 
              :label="room.roomId" :value="room.roomId" />
          </el-select>
        </el-form-item>
        <el-form-item label="物品类型">
          <el-select v-model="queryParams.facilityType" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="家具" :value="1" />
            <el-option label="电器" :value="2" />
            <el-option label="卫浴" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="损坏" :value="0" />
            <el-option label="正常" :value="1" />
            <el-option label="维修中" :value="2" />
            <el-option label="报废" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="handleAdd">新增物品</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="roomId" label="房间ID" width="100" />
      <el-table-column prop="facilityName" label="物品名称" width="120" />
      <el-table-column prop="facilityType" label="物品类型" width="90">
        <template #default="{row}">
          {{ getFacilityTypeText(row.facilityType) }}
        </template>
      </el-table-column>
      <el-table-column prop="brand" label="品牌" width="100" />
      <el-table-column prop="modelNumber" label="型号" width="120" />
      <el-table-column prop="purchaseDate" label="购置日期" width="110">
        <template #default="{row}">
          {{ row.purchaseDate || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{row}">
          <el-tag :type="getStatusTag(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remarks" label="备注" min-width="150" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{row}">
          {{ row.createdAt ? new Date(row.createdAt).toLocaleString() : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="180">
        <template #default="{row}">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
      :title="form.id ? '编辑物品' : '新增物品'"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="所属房间" prop="roomId">
          <el-select v-model="form.roomId" placeholder="请选择房间" filterable style="width: 100%">
            <el-option v-for="room in roomList" :key="room.roomId" 
              :label="`${room.roomId} - ${room.buildingId}栋${room.floor}层${room.roomNumber}室`" 
              :value="room.roomId" />
          </el-select>
        </el-form-item>
        <el-form-item label="物品名称" prop="facilityName">
          <el-input v-model="form.facilityName" placeholder="请输入物品名称，如 空调、书桌" />
        </el-form-item>
        <el-form-item label="物品类型" prop="facilityType">
          <el-select v-model="form.facilityType" placeholder="请选择物品类型" style="width: 100%">
            <el-option label="家具" :value="1" />
            <el-option label="电器" :value="2" />
            <el-option label="卫浴" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌" prop="brand">
          <el-input v-model="form.brand" placeholder="请输入品牌" />
        </el-form-item>
        <el-form-item label="型号" prop="modelNumber">
          <el-input v-model="form.modelNumber" placeholder="请输入型号" />
        </el-form-item>
        <el-form-item label="购置日期" prop="purchaseDate">
          <el-date-picker v-model="form.purchaseDate" type="date" placeholder="请选择购置日期" 
            format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="损坏" :value="0" />
            <el-option label="正常" :value="1" />
            <el-option label="维修中" :value="2" />
            <el-option label="报废" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input v-model="form.remarks" type="textarea" :rows="3" placeholder="请输入备注" />
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
import { ref, onMounted, reactive } from 'vue'
import { getDormFacilityList, getDormFacilityById, deleteDormFacility, saveDormFacility, updateDormFacility } from '@/api/dormFacility'
import { getAllDormRooms } from '@/api/dormRoom'

const loading = ref(false)
const tableData = ref([])
const roomList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const queryParams = reactive({
  keyword: '',
  roomId: '',
  facilityType: '',
  status: ''
})

const form = ref({
  id: '',
  roomId: '',
  facilityName: '',
  facilityType: 1,
  brand: '',
  modelNumber: '',
  purchaseDate: '',
  status: 1,
  remarks: ''
})

const rules = {
  roomId: [{ required: true, message: '请选择所属房间', trigger: 'change' }],
  facilityName: [{ required: true, message: '请输入物品名称', trigger: 'blur' }],
  facilityType: [{ required: true, message: '请选择物品类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const getFacilityTypeText = (type) => {
  const map = { 1: '家具', 2: '电器', 3: '卫浴', 4: '其他' }
  return map[type] || '-'
}

const getStatusText = (status) => {
  const map = { 0: '损坏', 1: '正常', 2: '维修中', 3: '报废' }
  return map[status] || '-'
}

const getStatusTag = (status) => {
  const map = { 0: 'danger', 1: 'success', 2: 'warning', 3: 'info' }
  return map[status] || ''
}

const getRoomList = async () => {
  try {
    const res = await getAllDormRooms()
    if (res.code === 200) {
      roomList.value = res.data
    }
  } catch (error) {
    console.error('获取房间列表失败:', error)
  }
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getDormFacilityList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...queryParams
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取物品列表失败:', error)
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
  queryParams.roomId = ''
  queryParams.facilityType = ''
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
    roomId: '',
    facilityName: '',
    facilityType: 1,
    brand: '',
    modelNumber: '',
    purchaseDate: '',
    status: 1,
    remarks: ''
  }
}

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getDormFacilityById(row.id)
    if (res.code === 200) {
      form.value = { ...res.data }
      isEdit.value = true
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取物品详情失败:', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该物品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteDormFacility(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除物品失败:', error)
    }
  })
}

const handleSubmit = () => {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      try {
        const res = isEdit.value ? await updateDormFacility(form.value) : await saveDormFacility(form.value)
        
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
          dialogVisible.value = false
          getList()
        }
      } catch (error) {
        console.error('保存物品失败:', error)
      }
    }
  })
}

onMounted(() => {
  getRoomList()
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
</style>
