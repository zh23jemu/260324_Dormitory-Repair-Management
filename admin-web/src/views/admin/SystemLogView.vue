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
    <el-pagination
      v-model:current-page="page.pageNum"
      v-model:page-size="page.pageSize"
      :total="page.total"
      layout="total, sizes, prev, pager, next, jumper"
      class="table-pagination"
      @current-change="loadLogs"
      @size-change="loadLogs"
    />
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import api from '../../api'
import { applyPageResult, createPageState, pageParams } from '../../utils/pagination'

const logs = ref([])
const page = reactive(createPageState())

async function loadLogs() {
  logs.value = applyPageResult(page, (await api.get('/admin/logs', { params: pageParams(page) })).data.data)
}

onMounted(loadLogs)
</script>
