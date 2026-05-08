<template>
  <div class="grid-two">
    <el-card>
      <template #header>维修员信息</template>
      <el-table :data="repairers" @row-click="loadDetail">
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="workTypeName" label="维修类型" />
        <el-table-column prop="phone" label="联系方式" />
        <el-table-column prop="completedCount" label="已完成" width="90" />
        <el-table-column prop="avgScore" label="平均评分" width="100" />
      </el-table>
      <el-pagination
        v-model:current-page="page.pageNum"
        v-model:page-size="page.pageSize"
        :total="page.total"
        layout="total, sizes, prev, pager, next"
        class="table-pagination"
        @current-change="loadRepairers"
        @size-change="loadRepairers"
      />
    </el-card>
    <el-card>
      <template #header>{{ detail?.realName || '评价信息' }}</template>
      <div v-if="detail">
        <p>维修类型：{{ detail.workTypeName || '未配置' }}</p>
        <p>完成率：{{ Number(detail.completionRate || 0).toFixed(1) }}%</p>
        <el-divider />
        <div v-for="item in detail.ratings || []" :key="item.id" class="rating-card">
          <strong>{{ item.title }}</strong>
          <div><el-rate :model-value="item.score" disabled /></div>
          <p>{{ item.content || '暂无文字评价' }}</p>
          <small>{{ item.studentName }} / {{ item.createdAt }}</small>
        </div>
        <el-empty v-if="!(detail.ratings || []).length" description="暂无评价" />
      </div>
      <el-empty v-else description="点击左侧维修员查看评价" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import api from '../../api'
import { applyPageResult, createPageState, pageParams } from '../../utils/pagination'

const repairers = ref([])
const detail = ref(null)
const page = reactive(createPageState())

async function loadRepairers() {
  repairers.value = applyPageResult(page, (await api.get('/repairer/repairers', { params: pageParams(page) })).data.data)
}

async function loadDetail(row) {
  detail.value = (await api.get(`/repairer/repairers/${row.id}`)).data.data
}

onMounted(loadRepairers)
</script>

<style scoped>
.rating-card {
  padding: 12px;
  border-radius: 12px;
  background: #f8fafc;
  margin-bottom: 10px;
}
</style>
