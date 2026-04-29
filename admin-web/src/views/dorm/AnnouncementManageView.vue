<template>
  <el-card>
    <template #header>公告管理</template>
    <el-form :model="form" class="announcement-form">
      <el-form-item label="公告标题">
        <el-input v-model="form.title" placeholder="公告标题" />
      </el-form-item>
      <el-form-item label="公告内容" class="announcement-content-item">
        <el-input
          v-model="form.content"
          placeholder="请输入公告正文，可填写巡检安排、注意事项、维修通知等详细内容"
          type="textarea"
          :rows="4"
          maxlength="800"
          show-word-limit
        />
      </el-form-item>
      <el-form-item>
        <el-select v-model="form.status" style="width: 120px">
          <el-option label="发布" value="published" />
          <el-option label="草稿" value="draft" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-upload :show-file-list="false" :http-request="uploadImage">
          <el-button>上传配图</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveAnnouncement">{{ form.id ? '保存修改' : '发布公告' }}</el-button>
        <el-button v-if="form.id" @click="resetForm">取消编辑</el-button>
      </el-form-item>
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
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" type="primary" plain @click="editAnnouncement(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="removeAnnouncement(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'
import { fileUrl } from '../../utils/file'

const announcements = ref([])
const form = reactive({ id: null, title: '', content: '', imagePath: '', status: 'published' })

async function loadAll() {
  announcements.value = (await api.get('/dorm-admin/announcements')).data.data
}

function resetForm() {
  form.id = null
  form.title = ''
  form.content = ''
  form.imagePath = ''
  form.status = 'published'
}

function editAnnouncement(row) {
  // 将表格中的公告回填到顶部表单，管理员可以一次性修改标题、内容、状态和配图。
  form.id = row.id
  form.title = row.title || ''
  form.content = row.content || ''
  form.imagePath = row.imagePath || ''
  form.status = row.status || 'published'
}

async function saveAnnouncement() {
  if (!form.title.trim()) return ElMessage.warning('请输入公告标题')
  if (!form.content.trim()) return ElMessage.warning('请输入公告内容')

  const payload = {
    title: form.title.trim(),
    content: form.content.trim(),
    imagePath: form.imagePath,
    status: form.status || 'published',
  }
  if (form.id) {
    await api.put(`/dorm-admin/announcements/${form.id}`, payload)
    ElMessage.success('公告已更新')
  } else {
    await api.post('/dorm-admin/announcements', payload)
    ElMessage.success('公告已发布')
  }
  resetForm()
  await loadAll()
}

async function removeAnnouncement(row) {
  await ElMessageBox.confirm(`确认删除公告“${row.title}”吗？删除后首页将不再展示该公告。`, '提示', { type: 'warning' })
  await api.delete(`/dorm-admin/announcements/${row.id}`)
  ElMessage.success('公告已删除')
  if (form.id === row.id) resetForm()
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

.announcement-form {
  display: grid;
  grid-template-columns: minmax(220px, 280px) minmax(360px, 1fr) 120px auto auto;
  gap: 12px;
  align-items: flex-start;
}

.announcement-form :deep(.el-form-item) {
  margin-right: 0;
  margin-bottom: 0;
}

.announcement-form :deep(.el-form-item__label) {
  font-weight: 700;
  color: #334155;
}

.announcement-content-item :deep(.el-textarea__inner) {
  min-height: 104px !important;
  line-height: 1.7;
  resize: vertical;
}

@media (max-width: 960px) {
  .announcement-form {
    grid-template-columns: 1fr;
  }
}
</style>
