<template>
  <el-card>
    <template #header>论坛管理</template>

    <el-form :model="query" inline>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable style="width: 150px" placeholder="全部状态">
          <el-option label="公开" value="published" />
          <el-option label="隐藏" value="hidden" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadPosts">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="posts" style="margin-top: 12px">
      <el-table-column prop="studentName" label="发布学生" width="110" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
      <el-table-column label="配图" width="110">
        <template #default="{ row }">
          <el-image
            v-if="row.imagePath"
            :src="fileUrl(row.imagePath)"
            :preview-src-list="[fileUrl(row.imagePath)]"
            fit="cover"
            style="width: 56px; height: 56px; border-radius: 8px"
          />
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'published' ? 'success' : 'info'">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="发布时间" width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button v-if="row.status !== 'published'" size="small" type="success" plain @click="changeStatus(row, 'published')">公开</el-button>
            <el-button v-if="row.status !== 'hidden'" size="small" type="warning" plain @click="changeStatus(row, 'hidden')">隐藏</el-button>
            <el-button size="small" type="danger" plain @click="removePost(row)">删除</el-button>
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

const posts = ref([])
const query = reactive({ status: '' })

function statusText(status) {
  return { published: '公开', hidden: '隐藏' }[status] || status || '-'
}

async function loadPosts() {
  // 管理员需要看到全部论坛帖子；当选择状态时，交给后端按状态过滤。
  const params = {}
  if (query.status) params.status = query.status
  posts.value = (await api.get('/admin/forum-posts', { params })).data.data
}

async function resetQuery() {
  query.status = ''
  await loadPosts()
}

async function changeStatus(row, status) {
  await api.put(`/admin/forum-posts/${row.id}/status`, { status })
  ElMessage.success(status === 'published' ? '帖子已公开' : '帖子已隐藏')
  await loadPosts()
}

async function removePost(row) {
  await ElMessageBox.confirm(`确认删除论坛帖子“${row.title}”吗？`, '提示', { type: 'warning' })
  await api.delete(`/admin/forum-posts/${row.id}`)
  ElMessage.success('论坛帖子已删除')
  await loadPosts()
}

onMounted(loadPosts)
</script>
