<template>
  <el-card>
    <template #header>公告管理</template>
    <el-form :model="form" inline>
      <el-form-item><el-input v-model="form.title" placeholder="公告标题" style="width:240px" /></el-form-item>
      <el-form-item><el-input v-model="form.content" placeholder="公告内容" style="width:420px" /></el-form-item>
      <el-form-item>
        <el-upload :show-file-list="false" :http-request="uploadImage">
          <el-button>上传配图</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item><el-button type="primary" @click="publish">发布公告</el-button></el-form-item>
    </el-form>
    <div v-if="form.imagePath" class="announcement-preview">
      <img :src="fileUrl(form.imagePath)" alt="公告配图预览" />
      <el-button link type="danger" @click="form.imagePath = ''">移除配图</el-button>
    </div>
    <el-table :data="announcements" style="margin-top:12px">
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="content" label="内容" min-width="260" />
      <el-table-column label="配图" width="110">
        <template #default="{ row }">
          <el-image v-if="row.imagePath" :src="fileUrl(row.imagePath)" :preview-src-list="[fileUrl(row.imagePath)]" style="width:64px;height:44px;border-radius:8px" fit="cover" />
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column prop="publisherName" label="发布人" width="120" />
      <el-table-column prop="publishedAt" label="发布时间" width="180" />
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'
import { fileUrl } from '../../utils/file'

const announcements = ref([])
const form = reactive({ title: '', content: '', imagePath: '', status: 'published' })

async function loadAll() {
  announcements.value = (await api.get('/dorm-admin/announcements')).data.data
}

async function publish() {
  await api.post('/dorm-admin/announcements', form)
  ElMessage.success('公告已发布')
  form.title = ''
  form.content = ''
  form.imagePath = ''
  await loadAll()
}

async function uploadImage(option) {
  const formData = new FormData()
  formData.append('file', option.file)
  const { data } = await api.post('/common/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
  form.imagePath = data.data.filePath
  ElMessage.success('公告配图已上传')
}

onMounted(loadAll)
</script>

<style scoped>
.announcement-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 10px;
}

.announcement-preview img {
  width: 160px;
  height: 90px;
  object-fit: cover;
  border-radius: 10px;
}
</style>
