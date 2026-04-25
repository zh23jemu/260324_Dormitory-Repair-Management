<template>
  <div class="page-stack">
    <div class="section">
      <div class="section-title">报修知识库</div>
      <div class="resource-grid">
        <div v-for="item in resources" :key="item.id" class="resource-card" @click="$router.push(`/resources/${item.id}`)">
          <strong>{{ item.title }}</strong>
          <span>{{ item.category }}</span>
          <p>{{ item.summary }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../../api'

const resources = ref([])

onMounted(async () => {
  resources.value = (await api.get('/student/resources')).data.data
})
</script>
