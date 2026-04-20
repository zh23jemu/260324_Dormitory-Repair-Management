<template>
  <div class="stack-section">
    <div class="stat-grid stats-six">
      <div class="stat-card"><div>总工单</div><h2>{{ overview.totalRepairCount || 0 }}</h2></div>
      <div class="stat-card"><div>处理中</div><h2>{{ overview.processingCount || 0 }}</h2></div>
      <div class="stat-card"><div>待评价</div><h2>{{ overview.pendingRatingCount || 0 }}</h2></div>
      <div class="stat-card"><div>完成率</div><h2>{{ toPercent(overview.completionRate) }}</h2></div>
      <div class="stat-card"><div>满意度</div><h2>{{ toPercent(overview.satisfactionRate) }}</h2></div>
      <div class="stat-card"><div>平均处理时长</div><h2>{{ Number(overview.avgHandleHours || 0).toFixed(2) }}h</h2></div>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="统计时间">
          <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" />
        </el-form-item>
        <el-form-item>
          <el-button @click="loadData">刷新数据</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="grid-two">
      <el-card><template #header>报修类型分布</template><div ref="typeChartRef" class="chart-box"></div></el-card>
      <el-card><template #header>楼栋报修热度</template><div ref="buildingChartRef" class="chart-box"></div></el-card>
    </div>
    <div class="grid-two">
      <el-card><template #header>工单状态分布</template><div ref="statusChartRef" class="chart-box"></div></el-card>
      <el-card><template #header>维修人员处理量排行</template><div ref="repairerChartRef" class="chart-box"></div></el-card>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import api from '../../api'
import { toPercent } from '../../utils/file'
import { repairOrderStatusText } from '../../utils/status'

const overview = reactive({})
const dateRange = ref([])
const repairTypeStats = ref([])
const buildingHeatStats = ref([])
const statusStats = ref([])
const repairerStats = ref([])
const typeChartRef = ref(null)
const buildingChartRef = ref(null)
const statusChartRef = ref(null)
const repairerChartRef = ref(null)

let typeChart = null
let buildingChart = null
let statusChart = null
let repairerChart = null

async function loadData() {
  Object.assign(overview, (await api.get('/admin/statistics/overview')).data.data)
  repairTypeStats.value = (await api.get('/admin/statistics/repair-type')).data.data
  buildingHeatStats.value = (await api.get('/admin/statistics/building-heat')).data.data
  statusStats.value = (await api.get('/admin/statistics/status')).data.data
  repairerStats.value = (await api.get('/admin/statistics/repairer-workload')).data.data
  await nextTick()
  renderCharts()
}

function renderCharts() {
  if (typeChartRef.value) {
    typeChart ||= echarts.init(typeChartRef.value)
    typeChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{ type: 'pie', radius: ['40%', '72%'], data: repairTypeStats.value.map((item) => ({ name: item.name, value: Number(item.value || 0) })) }]
    })
  }
  if (buildingChartRef.value) {
    buildingChart ||= echarts.init(buildingChartRef.value)
    buildingChart.setOption({
      tooltip: {},
      xAxis: { type: 'category', data: buildingHeatStats.value.map((item) => item.name) },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: buildingHeatStats.value.map((item) => Number(item.value || 0)), itemStyle: { color: '#2563eb' } }]
    })
  }
  if (statusChartRef.value) {
    statusChart ||= echarts.init(statusChartRef.value)
    statusChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{ type: 'pie', radius: '62%', data: statusStats.value.map((item) => ({ name: repairOrderStatusText(item.name), value: Number(item.value || 0) })) }]
    })
  }
  if (repairerChartRef.value) {
    repairerChart ||= echarts.init(repairerChartRef.value)
    repairerChart.setOption({
      tooltip: {},
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: repairerStats.value.map((item) => item.name) },
      series: [{ type: 'bar', data: repairerStats.value.map((item) => Number(item.value || 0)), itemStyle: { color: '#14b8a6' } }]
    })
  }
}

function handleResize() {
  typeChart?.resize()
  buildingChart?.resize()
  statusChart?.resize()
  repairerChart?.resize()
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)
  await loadData()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  typeChart?.dispose()
  buildingChart?.dispose()
  statusChart?.dispose()
  repairerChart?.dispose()
})
</script>
