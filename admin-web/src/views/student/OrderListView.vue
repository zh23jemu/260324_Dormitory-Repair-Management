<template>
  <div class="page-stack">
    <div class="section">
      <div class="section-head">
        <div class="section-title">我的工单</div>
        <van-dropdown-menu>
          <van-dropdown-item v-model="status" :options="statusOptions" />
        </van-dropdown-menu>
      </div>
      <van-cell-group inset>
        <van-cell
          v-for="item in orders"
          :key="item.id"
          :title="item.title"
          :label="`${item.repairTypeName || ''} / ${item.buildingName || ''}${item.roomNo || ''}`"
          :value="repairOrderStatusText(item.status)"
          is-link
          @click="$router.push(`/student/orders/${item.id}`)"
        />
      </van-cell-group>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import api from '../../api'
import { repairOrderStatusText } from '../../utils/status'

const status = ref('')
const orders = ref([])
const statusOptions = [
  { text: '全部状态', value: '' },
  { text: '待审核', value: 'pending_review' },
  { text: '待接单', value: 'pending_accept' },
  { text: '处理中', value: 'processing' },
  { text: '待评价', value: 'pending_rating' },
  { text: '已完成', value: 'completed' }
]

async function loadOrders() {
  orders.value = (await api.get('/student/repair-orders', { params: { status: status.value || undefined } })).data.data
}

watch(status, loadOrders)
onMounted(loadOrders)
</script>
