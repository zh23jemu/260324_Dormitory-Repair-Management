<template>
  <div class="grid-two">
    <el-card>
      <template #header>
        <div class="dorm-section-header">
          <span>楼栋管理</span>
          <el-button type="primary" @click="openBuildingCreate">新增楼栋</el-button>
        </div>
      </template>
      <el-alert
        title="填写说明：楼栋名称用于页面展示，例如“一号楼”；楼栋编码用于系统唯一识别，例如“B1”；层数填写该楼栋实际楼层数量。"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 12px"
      />
      <el-table :data="buildings" style="margin-top:12px">
        <el-table-column prop="buildingName" label="名称" />
        <el-table-column prop="buildingCode" label="编码" width="120" />
        <el-table-column prop="floorCount" label="层数" width="90" />
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="openBuildingEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="deleteBuilding(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card>
      <template #header>
        <div class="dorm-section-header">
          <span>宿舍管理</span>
          <el-button type="primary" @click="openRoomCreate">新增宿舍</el-button>
        </div>
      </template>
      <el-alert
        title="填写说明：先选择所属楼栋；宿舍号填写门牌号，例如“202”；容量为可入住床位数；设施配置摘要用于概括宿舍已有设施，例如“空调、书桌、热水器”。"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 12px"
      />
      <el-table :data="rooms" style="margin-top:12px">
        <el-table-column prop="buildingName" label="楼栋" width="90" />
        <el-table-column prop="roomNo" label="宿舍" width="90" />
        <el-table-column prop="capacity" label="容量" width="90" />
        <el-table-column prop="currentCount" label="已住" width="90" />
        <el-table-column prop="facilityDesc" label="设施概览" min-width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="openRoomEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="deleteRoom(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="buildingDialogVisible" :title="buildingDialogTitle" width="520px">
      <el-form :model="buildingEditForm" label-width="92px">
        <el-form-item label="楼栋名称">
          <el-input v-model="buildingEditForm.buildingName" placeholder="如：一号楼" />
        </el-form-item>
        <el-form-item label="楼栋编码">
          <el-input v-model="buildingEditForm.buildingCode" placeholder="如：B1，不可重复" />
        </el-form-item>
        <el-form-item label="楼层数">
          <el-input-number v-model="buildingEditForm.floorCount" :min="1" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="buildingEditForm.remark" type="textarea" :rows="3" placeholder="可填写楼栋说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buildingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBuilding">{{ buildingSubmitText }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roomDialogVisible" :title="roomDialogTitle" width="560px">
      <el-form :model="roomEditForm" label-width="96px">
        <el-form-item label="所属楼栋">
          <el-select v-model="roomEditForm.buildingId" placeholder="选择楼栋" style="width: 100%">
            <el-option v-for="item in buildings" :key="item.id" :label="item.buildingName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="宿舍号">
          <el-input v-model="roomEditForm.roomNo" placeholder="如：202" />
        </el-form-item>
        <el-form-item label="床位容量">
          <el-input-number v-model="roomEditForm.capacity" :min="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="roomEditForm.status" placeholder="选择状态" style="width: 100%">
            <el-option label="启用" value="enabled" />
            <el-option label="停用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item label="设施配置">
          <el-input v-model="roomEditForm.facilityDesc" placeholder="如：空调、书桌、热水器" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="roomEditForm.remark" type="textarea" :rows="3" placeholder="可填写宿舍说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roomDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRoom">{{ roomSubmitText }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const buildings = ref([])
const rooms = ref([])
const buildingDialogVisible = ref(false)
const roomDialogVisible = ref(false)
const buildingDialogMode = ref('create')
const roomDialogMode = ref('create')
const buildingEditForm = reactive({ id: null, buildingName: '', buildingCode: '', floorCount: 6, genderType: '', remark: '' })
const roomEditForm = reactive({ id: null, buildingId: null, roomNo: '', capacity: 4, facilityDesc: '', status: 'enabled', remark: '' })
const buildingDialogTitle = computed(() => (buildingDialogMode.value === 'create' ? '新增楼栋信息' : '编辑楼栋信息'))
const buildingSubmitText = computed(() => (buildingDialogMode.value === 'create' ? '确认新增' : '保存修改'))
const roomDialogTitle = computed(() => (roomDialogMode.value === 'create' ? '新增宿舍信息' : '编辑宿舍信息'))
const roomSubmitText = computed(() => (roomDialogMode.value === 'create' ? '确认新增' : '保存修改'))

async function loadAll() {
  buildings.value = (await api.get('/dorm-admin/buildings')).data.data
  rooms.value = (await api.get('/dorm-admin/rooms')).data.data
}

function resetBuildingForm() {
  Object.assign(buildingEditForm, { id: null, buildingName: '', buildingCode: '', floorCount: 6, genderType: '', remark: '' })
}

function resetRoomForm() {
  Object.assign(roomEditForm, { id: null, buildingId: null, roomNo: '', capacity: 4, facilityDesc: '', status: 'enabled', remark: '' })
}

function openBuildingCreate() {
  // 新增和编辑共用同一个弹窗表单，减少页面顶部长期占位，让楼栋列表区域更清爽。
  buildingDialogMode.value = 'create'
  resetBuildingForm()
  buildingDialogVisible.value = true
}

function openRoomCreate() {
  // 宿舍新增也统一走弹窗，字段布局与编辑保持一致，便于一次填写完整信息。
  roomDialogMode.value = 'create'
  resetRoomForm()
  roomDialogVisible.value = true
}

async function createBuilding() {
  await api.post('/dorm-admin/buildings', {
    buildingName: buildingEditForm.buildingName,
    buildingCode: buildingEditForm.buildingCode,
    floorCount: buildingEditForm.floorCount,
    genderType: buildingEditForm.genderType,
    remark: buildingEditForm.remark
  })
  ElMessage.success('楼栋已新增')
  buildingDialogVisible.value = false
  resetBuildingForm()
  await loadAll()
}

async function createRoom() {
  await api.post('/dorm-admin/rooms', {
    buildingId: roomEditForm.buildingId,
    roomNo: roomEditForm.roomNo,
    capacity: roomEditForm.capacity,
    facilityDesc: roomEditForm.facilityDesc,
    status: roomEditForm.status,
    remark: roomEditForm.remark
  })
  ElMessage.success('宿舍已新增')
  roomDialogVisible.value = false
  resetRoomForm()
  await loadAll()
}

function openBuildingEdit(row) {
  buildingDialogMode.value = 'edit'
  Object.assign(buildingEditForm, {
    id: row.id,
    buildingName: row.buildingName || '',
    buildingCode: row.buildingCode || '',
    floorCount: Number(row.floorCount || 1),
    genderType: row.genderType || '',
    remark: row.remark || ''
  })
  buildingDialogVisible.value = true
}

async function submitBuilding() {
  if (buildingDialogMode.value === 'create') {
    await createBuilding()
    return
  }
  await updateBuilding()
}

async function updateBuilding() {
  await api.put(`/dorm-admin/buildings/${buildingEditForm.id}`, {
    buildingName: buildingEditForm.buildingName,
    buildingCode: buildingEditForm.buildingCode,
    floorCount: buildingEditForm.floorCount,
    genderType: buildingEditForm.genderType,
    remark: buildingEditForm.remark
  })
  ElMessage.success('楼栋信息已更新')
  buildingDialogVisible.value = false
  await loadAll()
}

async function deleteBuilding(row) {
  try {
    await ElMessageBox.confirm(`确定删除楼栋“${row.buildingName}”吗？楼栋下仍有宿舍时系统会阻止删除。`, '删除楼栋', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await api.delete(`/dorm-admin/buildings/${row.id}`)
    ElMessage.success('楼栋已删除')
    await loadAll()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') throw error
  }
}

function openRoomEdit(row) {
  roomDialogMode.value = 'edit'
  Object.assign(roomEditForm, {
    id: row.id,
    buildingId: row.buildingId,
    roomNo: row.roomNo || '',
    capacity: Number(row.capacity || 1),
    facilityDesc: row.facilityDesc || '',
    status: row.status || 'enabled',
    remark: row.remark || ''
  })
  roomDialogVisible.value = true
}

async function submitRoom() {
  if (roomDialogMode.value === 'create') {
    await createRoom()
    return
  }
  await updateRoom()
}

async function updateRoom() {
  await api.put(`/dorm-admin/rooms/${roomEditForm.id}`, {
    buildingId: roomEditForm.buildingId,
    roomNo: roomEditForm.roomNo,
    capacity: roomEditForm.capacity,
    facilityDesc: roomEditForm.facilityDesc,
    status: roomEditForm.status,
    remark: roomEditForm.remark
  })
  ElMessage.success('宿舍信息已更新')
  roomDialogVisible.value = false
  await loadAll()
}

async function deleteRoom(row) {
  try {
    await ElMessageBox.confirm(`确定删除宿舍“${row.buildingName || ''}${row.roomNo}”吗？存在入住学生、工单或设施台账时系统会阻止删除。`, '删除宿舍', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await api.delete(`/dorm-admin/rooms/${row.id}`)
    ElMessage.success('宿舍已删除')
    await loadAll()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') throw error
  }
}

onMounted(loadAll)
</script>

<style scoped>
.dorm-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
</style>
