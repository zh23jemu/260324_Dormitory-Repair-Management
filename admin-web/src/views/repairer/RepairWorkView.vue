<template>
  <el-card>
    <template #header>维修处理中心</template>
    <el-table :data="orders">
      <el-table-column prop="orderNo" label="工单号" min-width="160" />
      <el-table-column prop="studentName" label="学生" width="100" />
      <el-table-column prop="repairTypeName" label="类型" width="100" />
      <el-table-column prop="buildingName" label="楼栋" width="90" />
      <el-table-column prop="roomNo" label="宿舍" width="90" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="openDetail(row)">详情</el-button>
          <el-button v-if="row.status === 'pending_accept'" size="small" type="primary" @click="accept(row)">接单</el-button>
          <el-button v-if="row.status === 'processing'" size="small" type="success" @click="openFeedback(row)">提交结果</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="detailVisible" title="工单详情" width="860px">
    <div v-if="currentOrder" class="detail-grid">
      <div><strong>工单号：</strong>{{ currentOrder.orderNo }}</div>
      <div><strong>状态：</strong>{{ currentOrder.status }}</div>
      <div><strong>学生：</strong>{{ currentOrder.studentName }}</div>
      <div><strong>报修类型：</strong>{{ currentOrder.repairTypeName }}</div>
      <div><strong>宿舍：</strong>{{ currentOrder.buildingName }} {{ currentOrder.roomNo }}</div>
      <div><strong>关联设施：</strong>{{ currentOrder.facilityName || '未关联' }}</div>
      <div style="grid-column:1 / -1"><strong>描述：</strong>{{ currentOrder.description }}</div>
    </div>
  </el-dialog>

  <el-dialog v-model="feedbackVisible" title="提交维修结果" width="560px">
    <el-form :model="feedbackForm" label-width="90px">
      <el-form-item label="处理结果"><el-input v-model="feedbackForm.resultDesc" type="textarea" rows="4" /></el-form-item>
      <el-form-item label="使用材料"><el-input v-model="feedbackForm.materialsUsed" /></el-form-item>
      <el-form-item label="完成时间"><el-input v-model="feedbackForm.finishTime" placeholder="2026-04-17 18:00:00" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="feedbackVisible = false">取消</el-button>
      <el-button type="primary" @click="submitFeedback">提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'

const orders = ref([])
const currentOrder = ref(null)
const currentRepairOrder = ref(null)
const detailVisible = ref(false)
const feedbackVisible = ref(false)
const feedbackForm = reactive({ resultDesc: '', materialsUsed: '', finishTime: '', imagePaths: [] })

async function loadOrders() {
  orders.value = (await api.get('/repairer/repair-orders')).data.data
}

async function openDetail(row) {
  currentOrder.value = (await api.get(`/repairer/repair-orders/${row.id}`)).data.data
  detailVisible.value = true
}

async function accept(row) {
  await api.post(`/repairer/repair-orders/${row.id}/accept`)
  ElMessage.success('接单成功')
  await loadOrders()
}

function openFeedback(row) {
  currentRepairOrder.value = row
  feedbackForm.resultDesc = ''
  feedbackForm.materialsUsed = ''
  feedbackForm.finishTime = new Date().toISOString().slice(0, 19).replace('T', ' ')
  feedbackVisible.value = true
}

async function submitFeedback() {
  await api.post(`/repairer/repair-orders/${currentRepairOrder.value.id}/feedback`, feedbackForm)
  ElMessage.success('维修结果已提交')
  feedbackVisible.value = false
  await loadOrders()
}

onMounted(loadOrders)
</script>
