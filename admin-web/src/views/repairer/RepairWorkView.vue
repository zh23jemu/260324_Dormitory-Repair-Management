<template>
  <el-card>
    <template #header>维修处理中心</template>
    <el-table :data="orders">
      <el-table-column prop="orderNo" label="工单号" min-width="160" />
      <el-table-column prop="studentName" label="学生" width="100" />
      <el-table-column prop="repairTypeName" label="类型" width="100" />
      <el-table-column prop="buildingName" label="楼栋" width="90" />
      <el-table-column prop="roomNo" label="宿舍" width="90" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">{{ repairOrderStatusText(row.status) }}</template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" plain @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'pending_accept'" size="small" type="primary" plain @click="accept(row)">接单</el-button>
            <el-button v-if="row.status === 'processing'" size="small" type="success" plain @click="openFeedback(row)">提交结果</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <RepairOrderDetailDialog v-model="detailVisible" :order="currentOrder" />
  <RepairFeedbackDialog v-model="feedbackVisible" :form="feedbackForm" :materials="materials" @submit="submitFeedback" />
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'
import { repairOrderStatusText } from '../../utils/status'
import RepairFeedbackDialog from './components/RepairFeedbackDialog.vue'
import RepairOrderDetailDialog from './components/RepairOrderDetailDialog.vue'

const orders = ref([])
const materials = ref([])
const currentOrder = ref(null)
const currentRepairOrder = ref(null)
const detailVisible = ref(false)
const feedbackVisible = ref(false)
const feedbackForm = reactive({ resultDesc: '', materialsUsed: '', finishTime: '', imagePaths: [], materialUsages: [] })

async function loadOrders() {
  orders.value = (await api.get('/repairer/repair-orders')).data.data
}

async function loadMaterials() {
  materials.value = (await api.get('/repairer/materials')).data.data
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
  feedbackForm.materialUsages = []
  feedbackVisible.value = true
}

async function submitFeedback() {
  // 完工提交前先在前端做一次明确校验，避免空耗材、0 数量进入后端造成用户不清楚失败原因。
  for (const item of feedbackForm.materialUsages) {
    if (!item.materialId || !item.quantity || item.quantity <= 0) {
      ElMessage.error('请完整选择耗材并填写大于 0 的使用数量')
      return
    }
  }
  await api.post(`/repairer/repair-orders/${currentRepairOrder.value.id}/feedback`, feedbackForm)
  ElMessage.success('维修结果已提交')
  feedbackVisible.value = false
  await Promise.all([loadOrders(), loadMaterials()])
}

onMounted(async () => {
  await Promise.all([loadOrders(), loadMaterials()])
})
</script>
