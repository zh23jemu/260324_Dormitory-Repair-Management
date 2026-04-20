<template>
  <div class="stack-section">
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="统计时间">
          <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" />
        </el-form-item>
        <el-form-item><el-button type="primary" @click="loadData">查询统计</el-button></el-form-item>
      </el-form>
    </el-card>

    <div class="stat-grid">
      <div class="stat-card"><div>累计工单</div><h2>{{ stats.totalCount || 0 }}</h2></div>
      <div class="stat-card"><div>已完成</div><h2>{{ stats.completedCount || 0 }}</h2></div>
      <div class="stat-card"><div>待接单</div><h2>{{ stats.pendingCount || 0 }}</h2></div>
      <div class="stat-card"><div>处理中</div><h2>{{ stats.processingCount || 0 }}</h2></div>
    </div>
    <div class="stat-grid">
      <div class="stat-card"><div>完成率</div><h2>{{ Number(stats.completionRate || 0).toFixed(1) }}%</h2></div>
      <div class="stat-card"><div>平均处理时长</div><h2>{{ Number(stats.avgHandleHours || 0).toFixed(2) }}h</h2></div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import api from '../../api'

const range = ref([])
const stats = reactive({})

async function loadData() {
  const params = range.value?.length === 2 ? { dateFrom: range.value[0], dateTo: range.value[1] } : {}
  Object.assign(stats, (await api.get('/repairer/statistics', { params })).data.data)
}

onMounted(loadData)
</script>
