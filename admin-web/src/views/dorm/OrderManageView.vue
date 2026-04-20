<template>
  <div class="stack-section">
    <el-card class="filter-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词"><el-input v-model="query.keyword" placeholder="工单号/学生/标题/宿舍" clearable /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 140px">
            <el-option label="待审核" value="pending_review" />
            <el-option label="已驳回" value="rejected" />
            <el-option label="待接单" value="pending_accept" />
            <el-option label="处理中" value="processing" />
            <el-option label="待评价" value="pending_rating" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.repairTypeId" clearable style="width: 140px">
            <el-option v-for="item in repairTypes" :key="item.id" :label="item.typeName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼栋">
          <el-select v-model="query.buildingId" clearable style="width: 140px">
            <el-option v-for="item in buildings" :key="item.id" :label="item.buildingName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="维修人员">
          <el-select v-model="query.repairerId" clearable style="width: 160px">
            <el-option v-for="item in repairers" :key="item.id" :label="item.realName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="loadOrders">查询</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div class="toolbar" style="margin-bottom:0">
          <span>工单列表</span>
          <el-button @click="loadOrders">刷新</el-button>
        </div>
      </template>
      <el-table :data="orders" stripe>
        <el-table-column prop="orderNo" label="工单号" min-width="170" />
        <el-table-column prop="studentName" label="学生" width="100" />
        <el-table-column prop="repairTypeName" label="类型" width="100" />
        <el-table-column prop="buildingName" label="楼栋" width="90" />
        <el-table-column prop="roomNo" label="宿舍" width="90" />
        <el-table-column prop="facilityName" label="关联设施" min-width="120" />
        <el-table-column prop="expectTime" label="期望时间" width="170" />
        <el-table-column prop="submittedAt" label="提交时间" width="170" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">{{ repairOrderStatusText(row.status) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'pending_review'" size="small" type="primary" @click="openAssign(row)">分配</el-button>
            <el-button v-if="row.status === 'pending_review'" size="small" type="danger" plain @click="reject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <OrderDetailDialog v-model="detailVisible" :order="currentOrder" />
    <AssignRepairerDialog v-model="assignVisible" :repairers="recommendedRepairers" @assign="assign" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'
import { repairOrderStatusText } from '../../utils/status'
import AssignRepairerDialog from './components/AssignRepairerDialog.vue'
import OrderDetailDialog from './components/OrderDetailDialog.vue'

const query = reactive({ keyword: '', status: '', repairTypeId: null, buildingId: null, repairerId: null })
const orders = ref([])
const repairTypes = ref([])
const buildings = ref([])
const repairers = ref([])
const recommendedRepairers = ref([])
const detailVisible = ref(false)
const assignVisible = ref(false)
const currentOrder = ref(null)
const currentAssignOrder = ref(null)

async function loadBase() {
  repairTypes.value = (await api.get('/dorm-admin/repair-types')).data.data
  buildings.value = (await api.get('/dorm-admin/buildings')).data.data
  repairers.value = (await api.get('/dorm-admin/repairers')).data.data
}

async function loadOrders() {
  orders.value = (await api.post('/dorm-admin/repair-orders/query', query)).data.data
}

async function openDetail(row) {
  currentOrder.value = (await api.get(`/dorm-admin/repair-orders/${row.id}`)).data.data
  detailVisible.value = true
}

async function openAssign(row) {
  currentAssignOrder.value = row
  recommendedRepairers.value = (await api.get('/dorm-admin/repairers/recommended', { params: { repairTypeId: row.repairTypeId } })).data.data
  assignVisible.value = true
}

async function assign(repairer) {
  await api.post(`/dorm-admin/repair-orders/${currentAssignOrder.value.id}/assign`, { repairerId: repairer.id, remark: `宿管分配给${repairer.realName}` })
  ElMessage.success('分配成功')
  assignVisible.value = false
  await loadOrders()
}

async function reject(row) {
  await ElMessageBox.confirm(`确认驳回工单 ${row.orderNo} 吗？`, '提示', { type: 'warning' })
  await api.post(`/dorm-admin/repair-orders/${row.id}/reject`, { rejectReason: '信息不完整，请补充详细故障说明。' })
  ElMessage.success('已驳回')
  await loadOrders()
}

onMounted(async () => {
  await loadBase()
  await loadOrders()
})
</script>
