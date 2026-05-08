<template>
  <div class="student-page">
    <section class="student-hero">
      <div class="student-hero__copy">
        <div class="student-hero__eyebrow">维修服务展示</div>
        <h1>维修人员信息</h1>
        <p>公开展示维修人员工种、处理量和评分情况，方便学生了解服务能力。</p>
      </div>
    </section>

    <section class="student-layout">
      <div class="student-main">
        <div class="student-card">
          <div class="student-card__header"><div><h2>维修人员列表</h2></div></div>
      <div class="resource-grid">
        <div v-for="item in repairers" :key="item.id" class="resource-card" @click="$router.push(`/repairers/${item.id}`)">
          <strong>{{ item.realName }}</strong>
          <span>{{ item.workTypeName || '未配置工种' }}</span>
          <p>累计工单：{{ item.totalCount || 0 }} / 完成：{{ item.completedCount || 0 }}</p>
          <p>平均评分：{{ item.avgScore || 0 }}</p>
        </div>
      </div>
      <el-pagination
        v-model:current-page="page.pageNum"
        v-model:page-size="page.pageSize"
        :total="page.total"
        layout="total, sizes, prev, pager, next"
        class="student-pagination"
        @current-change="loadRepairers"
        @size-change="loadRepairers"
      />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import api from '../../api'
import { applyPageResult, createPageState, pageParams } from '../../utils/pagination'

const repairers = ref([])
const page = reactive(createPageState())

async function loadRepairers() {
  repairers.value = applyPageResult(page, (await api.get('/portal/repairers', { params: pageParams(page) })).data.data)
}

onMounted(loadRepairers)
</script>
