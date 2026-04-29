<template>
  <div class="stack-section">
    <el-card class="filter-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="房间">
          <el-select v-model="query.roomId" clearable style="width: 180px">
            <el-option v-for="item in rooms" :key="item.id" :label="`${item.buildingName} ${item.roomNo}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词"><el-input v-model="query.keyword" placeholder="设施名称/类型/宿舍" clearable /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 140px">
            <el-option label="正常" value="normal" />
            <el-option label="故障" value="fault" />
            <el-option label="维修中" value="repairing" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="loadFacilities">查询</el-button></el-form-item>
        <el-form-item><el-button @click="openDialog()">新增设施</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>设施台账</template>
      <el-table :data="facilities">
        <el-table-column prop="buildingName" label="楼栋" width="90" />
        <el-table-column prop="roomNo" label="宿舍" width="90" />
        <el-table-column prop="facilityName" label="设施名称" min-width="130" />
        <el-table-column prop="facilityType" label="设施类型" width="120" />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="modelNumber" label="型号" width="100" />
        <el-table-column prop="purchaseDate" label="购置日期" width="120" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="openDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="removeFacility(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑设施' : '新增设施'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="所属宿舍">
          <el-select v-model="form.roomId" style="width:100%">
            <el-option v-for="item in rooms" :key="item.id" :label="`${item.buildingName} ${item.roomNo}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="设施名称"><el-input v-model="form.facilityName" /></el-form-item>
        <el-form-item label="设施类型"><el-input v-model="form.facilityType" /></el-form-item>
        <el-form-item label="品牌"><el-input v-model="form.brand" /></el-form-item>
        <el-form-item label="型号"><el-input v-model="form.modelNumber" /></el-form-item>
        <el-form-item label="购置日期"><el-input v-model="form.purchaseDate" placeholder="2025-09-01" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="正常" value="normal" />
            <el-option label="故障" value="fault" />
            <el-option label="维修中" value="repairing" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveFacility">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const query = reactive({ roomId: null, keyword: '', status: '' })
const facilities = ref([])
const rooms = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, roomId: null, facilityName: '', facilityType: '', brand: '', modelNumber: '', purchaseDate: '', status: 'normal', remark: '' })

async function loadRooms() {
  rooms.value = (await api.get('/dorm-admin/rooms')).data.data
}

async function loadFacilities() {
  facilities.value = (await api.get('/dorm-admin/facilities', { params: query })).data.data
}

function openDialog(row) {
  form.id = row?.id || null
  form.roomId = row?.roomId || null
  form.facilityName = row?.facilityName || ''
  form.facilityType = row?.facilityType || ''
  form.brand = row?.brand || ''
  form.modelNumber = row?.modelNumber || ''
  form.purchaseDate = row?.purchaseDate || ''
  form.status = row?.status || 'normal'
  form.remark = row?.remark || ''
  dialogVisible.value = true
}

async function saveFacility() {
  const payload = { roomId: form.roomId, facilityName: form.facilityName, facilityType: form.facilityType, brand: form.brand, modelNumber: form.modelNumber, purchaseDate: form.purchaseDate, status: form.status, remark: form.remark }
  if (form.id) await api.put(`/dorm-admin/facilities/${form.id}`, payload)
  else await api.post('/dorm-admin/facilities', payload)
  ElMessage.success('设施台账已保存')
  dialogVisible.value = false
  await loadFacilities()
}

async function removeFacility(row) {
  await ElMessageBox.confirm(`确认删除设施 ${row.facilityName} 吗？`, '提示', { type: 'warning' })
  await api.delete(`/dorm-admin/facilities/${row.id}`)
  ElMessage.success('设施已删除')
  await loadFacilities()
}

onMounted(async () => {
  await loadRooms()
  await loadFacilities()
})
</script>
