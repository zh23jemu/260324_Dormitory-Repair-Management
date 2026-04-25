<template>
  <div class="page-stack">
    <div class="section" v-if="resource">
      <div class="section-title">{{ resource.title }}</div>
      <div class="meta-line">{{ resource.category }} / {{ resource.publisherName || '系统发布' }}</div>
      <p class="resource-summary">{{ resource.summary }}</p>
      <div class="resource-content">{{ resource.content }}</div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import api from '../../api'

const route = useRoute()
const resource = ref(null)

onMounted(async () => {
  resource.value = (await api.get(`/student/resources/${route.params.id}`)).data.data
})
</script>
