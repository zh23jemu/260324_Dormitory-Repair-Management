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
      <el-pagination
        v-model:current-page="page.pageNum"
        v-model:page-size="page.pageSize"
        :total="page.total"
        layout="total, sizes, prev, pager, next"
        class="student-pagination"
        @current-change="loadResources"
        @size-change="loadResources"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import api from '../../api'
import { applyPageResult, createPageState, pageParams } from '../../utils/pagination'

const resources = ref([])
const page = reactive(createPageState())

async function loadResources() {
  resources.value = applyPageResult(page, (await api.get('/student/resources', { params: pageParams(page) })).data.data)
}

onMounted(loadResources)
</script>
