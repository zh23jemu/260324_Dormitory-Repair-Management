<template>
  <div class="page-stack">
    <div class="section">
      <div class="section-head">
        <div class="section-title">系统公告</div>
        <van-button size="small" plain type="primary" to="/announcements">更多公告</van-button>
      </div>
      <van-notice-bar v-if="announcements.length" :text="announcements[0].title + '：' + announcements[0].content" left-icon="volume-o" />
      <div v-if="announcements.length" class="announcement-list">
        <div v-for="item in announcements.slice(0, 3)" :key="item.id" class="announcement-card">
          <img v-if="item.imagePath" :src="fileUrl(item.imagePath)" alt="公告图片" />
          <div>
            <strong>{{ item.title }}</strong>
            <p>{{ item.content }}</p>
            <span>{{ item.publishedAt || item.createdAt }}</span>
          </div>
        </div>
      </div>
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
      <div class="quick-card" @click="$router.push('/forum')">
        <strong>公开论坛</strong>
        <span>查看同学交流内容</span>
      </div>
      <div class="quick-card" @click="$router.push('/announcements')">
        <strong>公告信息</strong>
        <span>查看全部公告</span>
      </div>
    </div>

    <div class="section stat-section">
      <div class="stat-box"><div>我的工单</div><h3>{{ summary.myOrderCount || 0 }}</h3></div>
      <div class="stat-box"><div>待处理</div><h3>{{ summary.pendingCount || 0 }}</h3></div>
      <div class="stat-box"><div>论坛帖子</div><h3>{{ summary.forumCount || 0 }}</h3></div>
      <div class="stat-box"><div>留言反馈</div><h3>{{ summary.messageCount || 0 }}</h3></div>
    </div>

    <div class="section">
      <div class="section-head">
        <div class="section-title">最近工单</div>
        <van-button size="small" plain type="primary" to="/orders">全部工单</van-button>
      </div>
      <van-cell-group inset>
        <van-cell v-for="item in summary.latestOrders || []" :key="item.id" :title="item.title" :label="item.orderNo" :value="repairOrderStatusText(item.status)" is-link @click="$router.push(`/orders/${item.id}`)" />
      </van-cell-group>
    </div>

    <div class="section">
      <div class="section-head">
        <div class="section-title">论坛动态</div>
        <van-button size="small" plain type="primary" to="/forum">进入论坛</van-button>
      </div>
      <van-cell-group inset>
        <van-cell v-for="item in summary.latestForumPosts || []" :key="item.id" :title="item.title" :label="item.studentName" :value="item.createdAt" />
      </van-cell-group>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../../api'
import { fileUrl } from '../../utils/file'
import { repairOrderStatusText } from '../../utils/status'

const announcements = ref([])
const summary = ref({})

onMounted(async () => {
  announcements.value = (await api.get('/student/announcements')).data.data
  summary.value = (await api.get('/student/home-summary')).data.data
})
</script>
