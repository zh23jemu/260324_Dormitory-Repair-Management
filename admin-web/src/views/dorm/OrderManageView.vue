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
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'pending_review'" size="small" type="primary" @click="openAssign(row)">分配</el-button>
            <el-button v-if="row.status === 'pending_review'" size="small" type="danger" plain @click="reject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="工单详情" width="900px">
      <div v-if="currentOrder" class="detail-grid">
        <div><strong>工单号：</strong>{{ currentOrder.orderNo }}</div>
        <div><strong>状态：</strong>{{ currentOrder.status }}</div>
        <div><strong>学生：</strong>{{ currentOrder.studentName }}</div>
        <div><strong>维修人员：</strong>{{ currentOrder.repairerName || '待分配' }}</div>
        <div><strong>报修类型：</strong>{{ currentOrder.repairTypeName }}</div>
        <div><strong>宿舍：</strong>{{ currentOrder.buildingName }} {{ currentOrder.roomNo }}</div>
        <div><strong>关联设施：</strong>{{ currentOrder.facilityName || '未关联' }}</div>
        <div><strong>期望时间：</strong>{{ currentOrder.expectTime || '未填写' }}</div>
        <div style="grid-column:1 / -1"><strong>标题：</strong>{{ currentOrder.title }}</div>
        <div style="grid-column:1 / -1"><strong>描述：</strong>{{ currentOrder.description }}</div>
      </div>
      <div class="section-title" style="margin-top:18px">处理时间线</div>
      <el-timeline>
        <el-timeline-item v-for="item in currentOrder?.flows || []" :key="item.id" :timestamp="item.created_at">
          {{ item.operator_name || '系统' }}：{{ item.remark }}
        </el-timeline-item>
      </el-timeline>
      <div class="section-title">图片记录</div>
      <div v-if="currentOrder?.images?.length" class="image-grid">
        <div v-for="img in currentOrder.images" :key="img.id" class="image-card">
          <div class="image-tag">{{ img.image_type === 'result' ? '维修结果' : '故障图片' }}</div>
          <img :src="fileUrl(img.file_path)" @click="previewImage(img.file_path)" />
        </div>
      </div>
      <div v-else class="empty-text">暂无图片</div>
    </el-dialog>

    <el-dialog v-model="assignVisible" title="分配维修人员" width="760px">
      <el-table :data="recommendedRepairers" size="small">
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="workTypeName" label="工种" width="120" />
        <el-table-column prop="phone" label="联系方式" width="130" />
        <el-table-column prop="totalCount" label="累计工单" width="90" />
        <el-table-column prop="completedCount" label="已完成" width="90" />
        <el-table-column prop="avgScore" label="平均评分" width="90" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="assign(row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="图片预览" width="760px">
      <div class="preview-wrap"><img v-if="previewUrl" :src="previewUrl" class="preview-image" @click="previewVisible = false" /></div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'
import { fileUrl } from '../../utils/file'

const query = reactive({ keyword: '', status: '', repairTypeId: null, buildingId: null, repairerId: null })
const orders = ref([])
const repairTypes = ref([])
const buildings = ref([])
const repairers = ref([])
const recommendedRepairers = ref([])
const detailVisible = ref(false)
const assignVisible = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')
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

function previewImage(path) {
  previewUrl.value = fileUrl(path)
  previewVisible.value = true
}

onMounted(async () => {
  await loadBase()
  await loadOrders()
})
</script>
