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
          <template v-for="item in detail.recentOrders || []" :key="`rating-${item.id}`">
            <div v-if="item.ratingContent" class="rating-card">
              <strong>{{ item.orderNo }}</strong>
              <p>{{ item.ratingContent }}</p>
              <span>{{ item.ratedAt || item.completedAt }}</span>
            </div>
          </template>
          <van-empty v-if="!(detail.recentOrders || []).length" description="暂无已完成工单评价" />
        </div>
      </div>
    </section>

    <section class="student-layout" v-else>
      <div class="student-main">
        <div class="student-card">
          <van-empty :description="loading ? '维修人员信息加载中' : errorMessage">
            <van-button v-if="!loading" type="primary" size="small" @click="loadDetail">重新加载</van-button>
          </van-empty>
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
const loading = ref(false)
const errorMessage = ref('维修人员信息加载失败')

async function loadDetail() {
  loading.value = true
  errorMessage.value = '维修人员信息加载失败'
  try {
    detail.value = (await api.get(`/portal/repairers/${route.params.id}`)).data.data
  } catch (error) {
    detail.value = null
    errorMessage.value = error?.response?.data?.message || error?.message || '维修人员信息加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)
</script>
