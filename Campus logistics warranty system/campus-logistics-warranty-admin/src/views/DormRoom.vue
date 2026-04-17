<template>
  <div class="dorm-room-manage">
    <div class="search-form">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="请输入房间ID或房间号" clearable />
        </el-form-item>
        <el-form-item label="所属楼栋">
          <el-select v-model="queryParams.buildingId" placeholder="请选择" clearable style="width: 150px;">
            <el-option v-for="building in buildingList" :key="building.buildingId" 
              :label="building.buildingName" :value="building.buildingId" />
          </el-select>
        </el-form-item>
        <el-form-item label="房型">
          <el-select v-model="queryParams.roomType" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="四人间" :value="1" />
            <el-option label="六人间" :value="2" />
            <el-option label="双人间" :value="3" />
            <el-option label="单人间" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="空闲" :value="0" />
            <el-option label="部分入住" :value="1" />
            <el-option label="已满" :value="2" />
            <el-option label="维修中" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="handleAdd">新增房间</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="roomId" label="房间ID" width="100" />
      <el-table-column prop="buildingId" label="所属楼栋" width="90" />
      <el-table-column prop="floor" label="楼层" width="70" />
      <el-table-column prop="roomNumber" label="房间号" width="80" />
      <el-table-column prop="roomType" label="房型" width="90">
        <template #default="{row}">
          {{ getRoomTypeText(row.roomType) }}
        </template>
      </el-table-column>
      <el-table-column prop="maxCapacity" label="最大容纳" width="80" />
      <el-table-column prop="currentOccupancy" label="当前入住" width="80" />
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
      :title="form.roomId ? '编辑房间' : '新增房间'"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="房间ID" prop="roomId">
          <el-input v-model="form.roomId" placeholder="请输入房间ID，如 A-201" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="所属楼栋" prop="buildingId">
          <el-select v-model="form.buildingId" placeholder="请选择楼栋" style="width: 100%">
            <el-option v-for="building in buildingList" :key="building.buildingId" 
              :label="building.buildingName" :value="building.buildingId" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层" prop="floor">
          <el-input-number v-model="form.floor" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="房间号" prop="roomNumber">
          <el-input v-model="form.roomNumber" placeholder="请输入房间号，如 201" />
        </el-form-item>
        <el-form-item label="房型" prop="roomType">
          <el-select v-model="form.roomType" placeholder="请选择房型" style="width: 100%">
            <el-option label="四人间" :value="1" />
            <el-option label="六人间" :value="2" />
            <el-option label="双人间" :value="3" />
            <el-option label="单人间" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大容纳" prop="maxCapacity">
          <el-input-number v-model="form.maxCapacity" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="当前入住" prop="currentOccupancy">
          <el-input-number v-model="form.currentOccupancy" :min="0" :max="form.maxCapacity" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="空闲" :value="0" />
            <el-option label="部分入住" :value="1" />
            <el-option label="已满" :value="2" />
            <el-option label="维修中" :value="3" />
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
import { getDormRoomList, getDormRoomById, deleteDormRoom, saveDormRoom, updateDormRoom } from '@/api/dormRoom'
import { getAllDormBuildings } from '@/api/dormBuilding'

const loading = ref(false)
const tableData = ref([])
const buildingList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const queryParams = reactive({
  keyword: '',
  buildingId: '',
  roomType: '',
  status: ''
})

const form = ref({
  roomId: '',
  buildingId: '',
  floor: 1,
  roomNumber: '',
  maxCapacity: 4,
  currentOccupancy: 0,
  roomType: 1,
  status: 0,
  remarks: ''
})

const rules = {
  roomId: [{ required: true, message: '请输入房间ID', trigger: 'blur' }],
  buildingId: [{ required: true, message: '请选择所属楼栋', trigger: 'change' }],
  floor: [{ required: true, message: '请输入楼层', trigger: 'blur' }],
  roomNumber: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  maxCapacity: [{ required: true, message: '请输入最大容纳人数', trigger: 'blur' }],
  roomType: [{ required: true, message: '请选择房型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const getRoomTypeText = (type) => {
  const map = { 1: '四人间', 2: '六人间', 3: '双人间', 4: '单人间' }
  return map[type] || '-'
}

const getStatusText = (status) => {
  const map = { 0: '空闲', 1: '部分入住', 2: '已满', 3: '维修中' }
  return map[status] || '-'
}

const getStatusTag = (status) => {
  const map = { 0: 'success', 1: 'warning', 2: 'danger', 3: 'info' }
  return map[status] || ''
}

const getBuildingList = async () => {
  try {
    const res = await getAllDormBuildings()
    if (res.code === 200) {
      buildingList.value = res.data
    }
  } catch (error) {
    console.error('获取宿舍楼列表失败:', error)
  }
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getDormRoomList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...queryParams
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取房间列表失败:', error)
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
  queryParams.buildingId = ''
  queryParams.roomType = ''
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
    roomId: '',
    buildingId: '',
    floor: 1,
    roomNumber: '',
    maxCapacity: 4,
    currentOccupancy: 0,
    roomType: 1,
    status: 0,
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
    const res = await getDormRoomById(row.roomId)
    if (res.code === 200) {
      form.value = { ...res.data }
      isEdit.value = true
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取房间详情失败:', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该房间吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteDormRoom(row.roomId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除房间失败:', error)
    }
  })
}

const handleSubmit = () => {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      try {
        const res = isEdit.value ? await updateDormRoom(form.value) : await saveDormRoom(form.value)
        
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
          dialogVisible.value = false
          getList()
        }
      } catch (error) {
        console.error('保存房间失败:', error)
      }
    }
  })
}

onMounted(() => {
  getBuildingList()
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
