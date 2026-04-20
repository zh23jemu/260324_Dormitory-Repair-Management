<template>
  <div class="grid-two">
    <el-card>
      <template #header>楼栋管理</template>
      <el-form :model="buildingForm" inline>
        <el-form-item><el-input v-model="buildingForm.buildingName" placeholder="楼栋名称" /></el-form-item>
        <el-form-item><el-input v-model="buildingForm.buildingCode" placeholder="楼栋编码" /></el-form-item>
        <el-form-item><el-input-number v-model="buildingForm.floorCount" :min="1" /></el-form-item>
        <el-form-item><el-button type="primary" @click="createBuilding">新增楼栋</el-button></el-form-item>
      </el-form>
      <el-table :data="buildings" style="margin-top:12px">
        <el-table-column prop="buildingName" label="名称" />
        <el-table-column prop="buildingCode" label="编码" width="120" />
        <el-table-column prop="floorCount" label="层数" width="90" />
        <el-table-column prop="remark" label="备注" />
      </el-table>
    </el-card>

    <el-card>
      <template #header>宿舍管理</template>
      <el-form :model="roomForm" inline>
        <el-form-item>
          <el-select v-model="roomForm.buildingId" placeholder="选择楼栋" style="width:140px">
            <el-option v-for="item in buildings" :key="item.id" :label="item.buildingName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item><el-input v-model="roomForm.roomNo" placeholder="宿舍号" /></el-form-item>
        <el-form-item><el-input-number v-model="roomForm.capacity" :min="1" /></el-form-item>
        <el-form-item><el-input v-model="roomForm.facilityDesc" placeholder="设施配置摘要" style="width:220px" /></el-form-item>
        <el-form-item><el-button type="primary" @click="createRoom">新增宿舍</el-button></el-form-item>
      </el-form>
      <el-table :data="rooms" style="margin-top:12px">
        <el-table-column prop="buildingName" label="楼栋" width="90" />
        <el-table-column prop="roomNo" label="宿舍" width="90" />
        <el-table-column prop="capacity" label="容量" width="90" />
        <el-table-column prop="currentCount" label="已住" width="90" />
        <el-table-column prop="facilityDesc" label="设施概览" min-width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'

const buildings = ref([])
const rooms = ref([])
const buildingForm = reactive({ buildingName: '', buildingCode: '', floorCount: 6, genderType: '', remark: '' })
const roomForm = reactive({ buildingId: null, roomNo: '', capacity: 4, facilityDesc: '', status: 'enabled', remark: '' })

async function loadAll() {
  buildings.value = (await api.get('/dorm-admin/buildings')).data.data
  rooms.value = (await api.get('/dorm-admin/rooms')).data.data
}

async function createBuilding() {
  await api.post('/dorm-admin/buildings', buildingForm)
  ElMessage.success('楼栋已新增')
  Object.assign(buildingForm, { buildingName: '', buildingCode: '', floorCount: 6, genderType: '', remark: '' })
  await loadAll()
}

async function createRoom() {
  await api.post('/dorm-admin/rooms', roomForm)
  ElMessage.success('宿舍已新增')
  Object.assign(roomForm, { buildingId: null, roomNo: '', capacity: 4, facilityDesc: '', status: 'enabled', remark: '' })
  await loadAll()
}

onMounted(loadAll)
</script>
