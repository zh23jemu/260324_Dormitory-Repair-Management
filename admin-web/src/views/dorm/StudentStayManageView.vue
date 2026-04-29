<template>
  <el-card>
    <template #header>学生住宿管理</template>
    <el-table :data="students" row-key="id">
      <el-table-column prop="studentNo" label="学号" width="110" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column label="楼栋" width="150">
        <template #default="{ row }">
          <el-select
            v-if="isEditing(row)"
            v-model="editForm.buildingId"
            placeholder="选择楼栋"
            filterable
            class="stay-edit-control"
            @change="handleBuildingChange"
          >
            <el-option
              v-for="building in buildings"
              :key="building.id"
              :label="building.buildingName"
              :value="building.id"
            />
          </el-select>
          <span v-else>{{ row.buildingName || '未分配' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="宿舍" width="150">
        <template #default="{ row }">
          <el-select
            v-if="isEditing(row)"
            v-model="editForm.roomId"
            placeholder="选择宿舍"
            filterable
            class="stay-edit-control"
            :disabled="!editForm.buildingId"
          >
            <el-option
              v-for="room in editableRooms"
              :key="room.id"
              :label="room.roomNo"
              :value="room.id"
            />
          </el-select>
          <span v-else>{{ row.roomNo || '未分配' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="床位" width="130">
        <template #default="{ row }">
          <el-input
            v-if="isEditing(row)"
            v-model.trim="editForm.bedNo"
            placeholder="如 1号床"
            maxlength="20"
            class="stay-edit-control"
          />
          <span v-else>{{ row.bedNo || '未填写' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="college" label="学院" min-width="140" />
      <el-table-column prop="major" label="专业" min-width="140" />
      <el-table-column prop="className" label="班级" min-width="120" />
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <div v-if="isEditing(row)" class="stay-actions">
            <el-button class="stay-primary-btn" size="small" type="primary" :loading="saving" @click="saveRow(row)">
              保存
            </el-button>
            <el-button class="stay-cancel-btn" size="small" :disabled="saving" @click="cancelEdit">取消</el-button>
          </div>
          <el-button v-else class="stay-primary-btn" size="small" type="primary" @click="startEdit(row)">
            编辑住宿
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'

const students = ref([])
const rooms = ref([])
const buildings = ref([])
const editingId = ref(null)
const saving = ref(false)

// 行内编辑表单只负责住宿分配字段，避免弹窗打断操作，也避免一次只能输入一个字段。
const editForm = reactive({
  buildingId: null,
  roomId: null,
  bedNo: '',
})

// 宿舍下拉根据已选楼栋动态过滤，保证“楼栋-宿舍”关系不会选错。
const editableRooms = computed(() => {
  if (!editForm.buildingId) return []
  return rooms.value.filter((room) => Number(room.buildingId) === Number(editForm.buildingId))
})

async function loadAll() {
  const [studentRes, roomRes, buildingRes] = await Promise.all([
    api.get('/dorm-admin/students'),
    api.get('/dorm-admin/rooms'),
    api.get('/dorm-admin/buildings'),
  ])
  students.value = studentRes.data.data || []
  rooms.value = roomRes.data.data || []
  buildings.value = buildingRes.data.data || []
}

function isEditing(row) {
  return editingId.value === row.id
}

function startEdit(row) {
  editingId.value = row.id
  editForm.buildingId = row.buildingId || null
  editForm.roomId = row.roomId || null
  editForm.bedNo = row.bedNo || ''
}

function cancelEdit() {
  editingId.value = null
  editForm.buildingId = null
  editForm.roomId = null
  editForm.bedNo = ''
}

function handleBuildingChange() {
  // 切换楼栋后，如果原宿舍不属于新楼栋，则清空宿舍，提示用户重新选择。
  const roomStillValid = editableRooms.value.some((room) => Number(room.id) === Number(editForm.roomId))
  if (!roomStillValid) {
    editForm.roomId = null
  }
}

async function saveRow(row) {
  if (!editForm.buildingId) return ElMessage.warning('请选择楼栋')
  if (!editForm.roomId) return ElMessage.warning('请选择宿舍')
  if (!editForm.bedNo) return ElMessage.warning('请输入床位号')

  const targetRoom = rooms.value.find((room) => Number(room.id) === Number(editForm.roomId))
  if (!targetRoom) return ElMessage.error('宿舍不存在')

  saving.value = true
  try {
    await api.put(`/dorm-admin/students/${row.id}/room`, {
      buildingId: targetRoom.buildingId,
      roomId: targetRoom.id,
      bedNo: editForm.bedNo,
    })
    ElMessage.success('住宿信息已更新')
    cancelEdit()
    await loadAll()
  } finally {
    saving.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.stay-edit-control {
  width: 100%;
}

.stay-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: nowrap;
}

/* 本页操作按钮使用高对比色，避免被全局渐变/朴素按钮样式覆盖后文字发浅。 */
.stay-primary-btn {
  min-width: 82px;
  border: none !important;
  background: linear-gradient(135deg, #0f766e 0%, #2563eb 100%) !important;
  color: #ffffff !important;
  font-weight: 700;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.24);
}

.stay-primary-btn :deep(span) {
  color: #ffffff !important;
}

.stay-primary-btn:hover,
.stay-primary-btn:focus {
  background: linear-gradient(135deg, #0d5f59 0%, #1d4ed8 100%) !important;
  color: #ffffff !important;
}

.stay-cancel-btn {
  min-width: 64px;
  border-color: #cbd5e1 !important;
  background: #ffffff !important;
  color: #334155 !important;
  font-weight: 700;
}

.stay-cancel-btn :deep(span) {
  color: #334155 !important;
}
</style>
