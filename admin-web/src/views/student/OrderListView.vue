<template>
  <div class="student-page">
    <section class="student-hero">
      <div class="student-hero__copy">
        <div class="student-hero__eyebrow">学生工单中心</div>
        <h1>我的报修工单</h1>
        <p>集中查看个人报修记录、当前处理状态和历史完成情况，支持按工单状态快速筛选。</p>
      </div>
      <div class="student-hero__metrics">
        <div class="student-hero__metric">
          <span>工单总数</span>
          <strong>{{ page.total }}</strong>
        </div>
      </div>
    </section>

    <section class="student-layout">
      <div class="student-main">
        <div class="student-card">
          <div class="student-card__header">
            <div>
              <h2>工单列表</h2>
              <p>按状态过滤并查看每条工单的报修类型、位置和处理进度。</p>
            </div>
            <div class="student-card__action">
              <select v-model="status" class="student-status-select" aria-label="工单状态筛选">
                <option v-for="item in statusOptions" :key="item.value" :value="item.value">
                  {{ item.text }}
                </option>
              </select>
            </div>
          </div>

          <div v-if="orders.length" class="student-order-grid">
            <article v-for="item in orders" :key="item.id" class="student-order-item" @click="$router.push(`/student/orders/${item.id}`)">
              <div class="student-order-item__head">
                <strong>{{ item.title }}</strong>
                <span class="student-status-pill">{{ repairOrderStatusText(item.status) }}</span>
              </div>
              <p>{{ item.repairTypeName || '未分类' }} / {{ item.buildingName || '' }}{{ item.roomNo || '' }}</p>
              <small>工单号：{{ item.orderNo }}</small>
            </article>
          </div>
          <van-empty v-else description="暂无工单" />
          <el-pagination
            v-model:current-page="page.pageNum"
            v-model:page-size="page.pageSize"
            :total="page.total"
            layout="total, sizes, prev, pager, next"
            class="student-pagination"
            @current-change="loadOrders"
            @size-change="loadOrders"
          />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import api from '../../api'
import { repairOrderStatusText } from '../../utils/status'
import { applyPageResult, createPageState, pageParams } from '../../utils/pagination'

const status = ref('')
const orders = ref([])
const page = ref(createPageState())
const statusOptions = [
  { text: '全部状态', value: '' },
  { text: '待审核', value: 'pending_review' },
  { text: '待接单', value: 'pending_accept' },
  { text: '处理中', value: 'processing' },
  { text: '待评价', value: 'pending_rating' },
  { text: '已完成', value: 'completed' }
]

async function loadOrders() {
  orders.value = applyPageResult(page.value, (await api.get('/student/repair-orders', { params: { status: status.value || undefined, ...pageParams(page.value) } })).data.data)
}

watch(status, async () => {
  page.value.pageNum = 1
  await loadOrders()
})
onMounted(loadOrders)
</script>
