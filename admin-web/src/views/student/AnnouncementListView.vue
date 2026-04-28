<template>
  <div class="student-page">
    <section class="student-hero">
      <div class="student-hero__copy">
        <div class="student-hero__eyebrow">系统公告中心</div>
        <h1>公告信息</h1>
        <p>查看宿舍维修通知、巡检安排和系统更新信息，支持图片公告展示。</p>
      </div>
    </section>

    <section class="student-layout">
      <div class="student-main">
        <div class="student-card">
          <div class="student-card__header"><div><h2>公告列表</h2></div></div>
      <div v-if="announcements.length" class="announcement-list">
        <div
          v-for="item in announcements"
          :key="item.id"
          :class="['announcement-card', { 'announcement-card--with-image': !!item.imagePath }]"
        >
          <img v-if="item.imagePath" :src="fileUrl(item.imagePath)" alt="公告图片" />
          <div class="announcement-card__content">
            <strong>{{ item.title }}</strong>
            <p>{{ item.content }}</p>
            <span>发布人：{{ item.publisherName || '系统' }} / {{ item.publishedAt || item.createdAt }}</span>
          </div>
        </div>
      </div>
      <van-empty v-else description="暂无公告" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../../api'
import { fileUrl } from '../../utils/file'

const announcements = ref([])

onMounted(async () => {
  announcements.value = (await api.get('/portal/announcements')).data.data
})
</script>
