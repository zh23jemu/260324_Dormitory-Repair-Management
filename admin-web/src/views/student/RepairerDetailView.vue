<template>
  <div class="student-page">
    <section class="student-hero" v-if="detail">
      <div class="student-hero__copy">
        <div class="student-hero__eyebrow">维修人员详情</div>
        <h1>{{ detail.realName }}</h1>
        <p>查看维修人员工种、完成率和已完成工单评价。</p>
      </div>
    </section>

    <section class="student-layout" v-if="detail">
      <div class="student-main">
        <div class="student-card">
          <div class="student-card__header"><div><h2>基础信息</h2></div></div>
          <div class="student-info-grid">
            <div class="student-info-item"><span>工种</span><strong>{{ detail.workTypeName || '未配置' }}</strong></div>
            <div class="student-info-item"><span>联系方式</span><strong>{{ detail.phone || '未填写' }}</strong></div>
            <div class="student-info-item"><span>累计工单</span><strong>{{ String(detail.totalCount || 0) }}</strong></div>
            <div class="student-info-item"><span>已完成</span><strong>{{ String(detail.completedCount || 0) }}</strong></div>
            <div class="student-info-item"><span>完成率</span><strong>{{ `${detail.completionRate || 0}%` }}</strong></div>
            <div class="student-info-item"><span>平均评分</span><strong>{{ String(detail.avgScore || 0) }}</strong></div>
          </div>
        </div>

        <div class="student-card">
          <div class="student-card__header"><div><h2>已完成工单评价</h2></div></div>
          <van-cell-group inset>
            <van-cell v-for="item in detail.recentOrders || []" :key="item.id" :title="item.title" :label="`${item.repairTypeName || ''} / ${item.studentName || '学生'}`">
              <template #value>
                <van-rate v-if="item.score" :model-value="item.score" readonly size="14" />
                <span v-else>暂无评价</span>
              </template>
            </van-cell>
          </van-cell-group>
          <div v-for="item in detail.recentOrders || []" :key="`rating-${item.id}`" class="rating-card" v-if="item.ratingContent">
            <strong>{{ item.orderNo }}</strong>
            <p>{{ item.ratingContent }}</p>
            <span>{{ item.ratedAt || item.completedAt }}</span>
          </div>
          <van-empty v-if="!(detail.recentOrders || []).length" description="暂无已完成工单评价" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import api from '../../api'

const route = useRoute()
const detail = ref(null)

onMounted(async () => {
  detail.value = (await api.get(`/portal/repairers/${route.params.id}`)).data.data
})
</script>
