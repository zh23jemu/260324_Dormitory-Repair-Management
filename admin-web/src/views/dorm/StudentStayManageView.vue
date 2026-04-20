<template>
  <el-card>
    <template #header>学生住宿管理</template>
    <el-table :data="students">
      <el-table-column prop="studentNo" label="学号" width="110" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="buildingName" label="楼栋" width="90" />
      <el-table-column prop="roomNo" label="宿舍" width="90" />
      <el-table-column prop="bedNo" label="床位" width="90" />
      <el-table-column prop="college" label="学院" min-width="140" />
      <el-table-column prop="major" label="专业" min-width="140" />
      <el-table-column prop="className" label="班级" min-width="120" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="editRow(row)">调整</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'

const students = ref([])
const rooms = ref([])

async function loadAll() {
  students.value = (await api.get('/dorm-admin/students')).data.data
  rooms.value = (await api.get('/dorm-admin/rooms')).data.data
}

async function editRow(row) {
  const roomInput = window.prompt('请输入新的宿舍号或宿舍ID', row.roomNo || '')
  if (roomInput === null) return
  const bedNo = window.prompt('请输入床位号', row.bedNo || '')
  if (bedNo === null) return
  const normalized = String(roomInput).trim()
  let targetRoom = rooms.value.find((item) => String(item.id) === normalized) || null
  if (!targetRoom) {
    targetRoom = rooms.value.find((item) => String(item.roomNo) === normalized) || null
  }
  if (!targetRoom) {
    return ElMessage.error('宿舍不存在')
  }
  await api.put(`/dorm-admin/students/${row.id}/room`, { buildingId: targetRoom.buildingId, roomId: targetRoom.id, bedNo })
  ElMessage.success('住宿信息已更新')
  await loadAll()
}

onMounted(loadAll)
</script>
