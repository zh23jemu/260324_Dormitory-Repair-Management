<template>
  <div class="page-stack">
    <div class="section" v-if="detail">
      <div class="section-title">{{ detail.realName }}</div>
      <van-cell-group inset>
        <van-cell title="工种" :value="detail.workTypeName || '未配置'" />
        <van-cell title="联系方式" :value="detail.phone || '未填写'" />
        <van-cell title="累计工单" :value="String(detail.totalCount || 0)" />
        <van-cell title="已完成" :value="String(detail.completedCount || 0)" />
        <van-cell title="完成率" :value="`${detail.completionRate || 0}%`" />
        <van-cell title="平均评分" :value="String(detail.avgScore || 0)" />
      </van-cell-group>
      <div class="detail-block">
        <strong>近期工单</strong>
        <van-cell-group inset>
          <van-cell v-for="item in detail.recentOrders || []" :key="item.id" :title="item.title" :label="item.repairTypeName" :value="item.status" />
        </van-cell-group>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import api from '../../api'

const route = useRoute()
const detail = ref(null)

onMounted(async () => {
  detail.value = (await api.get(`/student/repairers/${route.params.id}`)).data.data
})
</script>
