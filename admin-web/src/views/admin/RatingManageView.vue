<template>
  <el-card>
    <template #header>评价管理</template>

    <el-form :model="query" inline>
      <el-form-item label="评分">
        <el-select v-model="query.score" clearable style="width: 140px" placeholder="全部评分">
          <el-option v-for="score in [5, 4, 3, 2, 1]" :key="score" :label="`${score} 星`" :value="score" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadRatings">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="ratings" style="margin-top: 12px">
      <el-table-column prop="orderNo" label="工单号" width="160" />
      <el-table-column prop="orderTitle" label="工单标题" min-width="180" />
      <el-table-column prop="repairTypeName" label="报修类型" width="110" />
      <el-table-column prop="studentName" label="评价学生" width="110" />
      <el-table-column prop="repairerName" label="维修人员" width="110" />
      <el-table-column label="评分" width="150">
        <template #default="{ row }">
          <el-rate :model-value="Number(row.score || 0)" disabled />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评价内容" min-width="240" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="评价时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="danger" plain @click="removeRating(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const ratings = ref([])
const query = reactive({ score: null })

async function loadRatings() {
  // 评价管理用于管理员查看服务质量反馈，支持按星级快速定位差评或异常评价。
  const params = {}
  if (query.score) params.score = query.score
  ratings.value = (await api.get('/admin/ratings', { params })).data.data
}

async function resetQuery() {
  query.score = null
  await loadRatings()
}

async function removeRating(row) {
  await ElMessageBox.confirm(`确认删除工单 ${row.orderNo} 的评价吗？删除后工单仍保持已完成状态，仅不再展示该评价。`, '提示', { type: 'warning' })
  await api.delete(`/admin/ratings/${row.id}`)
  ElMessage.success('评价已删除')
  await loadRatings()
}

onMounted(loadRatings)
</script>
