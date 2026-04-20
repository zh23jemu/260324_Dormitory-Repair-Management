<template>
  <el-card>
    <template #header>系统日志</template>
    <el-table :data="logs">
      <el-table-column prop="moduleName" label="模块" width="140" />
      <el-table-column prop="operationType" label="操作" width="100" />
      <el-table-column prop="userName" label="用户" width="120" />
      <el-table-column prop="operationDesc" label="描述" min-width="220" />
      <el-table-column prop="createdAt" label="时间" width="180" />
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../../api'

const logs = ref([])

onMounted(async () => {
  logs.value = (await api.get('/admin/logs')).data.data
})
</script>
