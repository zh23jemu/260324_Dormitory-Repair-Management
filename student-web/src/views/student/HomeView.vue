<template>
  <div class="page-stack">
    <div class="section">
      <div class="section-head">
        <div class="section-title">系统公告</div>
        <van-button size="small" plain type="primary" to="/resources">查看知识库</van-button>
      </div>
      <van-notice-bar v-if="announcements.length" :text="announcements[0].title + '：' + announcements[0].content" left-icon="volume-o" />
      <van-empty v-else description="暂无公告" />
    </div>

    <div class="section quick-grid">
      <div class="quick-card" @click="$router.push('/repair/create')">
        <strong>我要报修</strong>
        <span>提交宿舍故障工单</span>
      </div>
      <div class="quick-card" @click="$router.push('/orders')">
        <strong>我的工单</strong>
        <span>查看进度与评价</span>
      </div>
      <div class="quick-card" @click="$router.push('/repairers')">
        <strong>维修人员</strong>
        <span>查看维修工信息</span>
      </div>
      <div class="quick-card" @click="$router.push('/messages')">
        <strong>服务留言</strong>
        <span>提交建议与反馈</span>
      </div>
    </div>

    <div class="section stat-section">
      <div class="stat-box"><div>我的工单</div><h3>{{ summary.myOrderCount || 0 }}</h3></div>
      <div class="stat-box"><div>待处理</div><h3>{{ summary.pendingCount || 0 }}</h3></div>
      <div class="stat-box"><div>知识条目</div><h3>{{ summary.resourceCount || 0 }}</h3></div>
      <div class="stat-box"><div>留言反馈</div><h3>{{ summary.messageCount || 0 }}</h3></div>
    </div>

    <div class="section">
      <div class="section-head">
        <div class="section-title">最近工单</div>
        <van-button size="small" plain type="primary" to="/orders">全部工单</van-button>
      </div>
      <van-cell-group inset>
        <van-cell v-for="item in summary.latestOrders || []" :key="item.id" :title="item.title" :label="item.orderNo" :value="item.status" is-link @click="$router.push(`/orders/${item.id}`)" />
      </van-cell-group>
    </div>

    <div class="section">
      <div class="section-head">
        <div class="section-title">报修知识推荐</div>
        <van-button size="small" plain type="primary" to="/resources">更多知识</van-button>
      </div>
      <van-cell-group inset>
        <van-cell v-for="item in summary.latestResources || []" :key="item.id" :title="item.title" :label="item.category" is-link @click="$router.push(`/resources/${item.id}`)" />
      </van-cell-group>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../../api'

const announcements = ref([])
const summary = ref({})

onMounted(async () => {
  announcements.value = (await api.get('/student/announcements')).data.data
  summary.value = (await api.get('/student/home-summary')).data.data
})
</script>
