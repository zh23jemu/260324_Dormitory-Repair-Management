<template>
  <div class="page-stack">
    <div class="section">
      <div class="section-title">公告信息</div>
      <div v-if="announcements.length" class="announcement-list">
        <div v-for="item in announcements" :key="item.id" class="announcement-card">
          <img v-if="item.imagePath" :src="fileUrl(item.imagePath)" alt="公告图片" />
          <div>
            <strong>{{ item.title }}</strong>
            <p>{{ item.content }}</p>
            <span>发布人：{{ item.publisherName || '系统' }} / {{ item.publishedAt || item.createdAt }}</span>
          </div>
        </div>
      </div>
      <van-empty v-else description="暂无公告" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../../api'
import { fileUrl } from '../../utils/file'

const announcements = ref([])

onMounted(async () => {
  announcements.value = (await api.get('/student/announcements')).data.data
})
</script>
