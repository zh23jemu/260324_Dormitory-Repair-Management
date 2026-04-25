<template>
  <div class="page-stack">
    <div class="section">
      <div class="section-title">维修人员展示</div>
      <div class="resource-grid">
        <div v-for="item in repairers" :key="item.id" class="resource-card" @click="$router.push(`/repairers/${item.id}`)">
          <strong>{{ item.realName }}</strong>
          <span>{{ item.workTypeName || '未配置工种' }}</span>
          <p>累计工单：{{ item.totalCount || 0 }} / 完成：{{ item.completedCount || 0 }}</p>
          <p>平均评分：{{ item.avgScore || 0 }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../../api'

const repairers = ref([])

onMounted(async () => {
  repairers.value = (await api.get('/portal/repairers')).data.data
})
</script>
