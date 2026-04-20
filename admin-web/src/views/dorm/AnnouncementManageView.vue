<template>
  <el-card>
    <template #header>公告管理</template>
    <el-form :model="form" inline>
      <el-form-item><el-input v-model="form.title" placeholder="公告标题" style="width:240px" /></el-form-item>
      <el-form-item><el-input v-model="form.content" placeholder="公告内容" style="width:420px" /></el-form-item>
      <el-form-item><el-button type="primary" @click="publish">发布公告</el-button></el-form-item>
    </el-form>
    <el-table :data="announcements" style="margin-top:12px">
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="content" label="内容" min-width="260" />
      <el-table-column prop="publisherName" label="发布人" width="120" />
      <el-table-column prop="publishedAt" label="发布时间" width="180" />
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'

const announcements = ref([])
const form = reactive({ title: '', content: '', status: 'published' })

async function loadAll() {
  announcements.value = (await api.get('/dorm-admin/announcements')).data.data
}

async function publish() {
  await api.post('/dorm-admin/announcements', form)
  ElMessage.success('公告已发布')
  form.title = ''
  form.content = ''
  await loadAll()
}

onMounted(loadAll)
</script>
