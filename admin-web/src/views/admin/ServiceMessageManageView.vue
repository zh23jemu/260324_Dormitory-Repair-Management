<template>
  <div class="stack-section">
    <el-card>
      <template #header>服务留言反馈</template>
      <el-table :data="messages">
        <el-table-column prop="studentName" label="学生" width="100" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="content" label="内容" min-width="220" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column prop="replyContent" label="回复内容" min-width="220" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" @click="reply(row)">回复</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'

const messages = ref([])

async function loadAll() {
  messages.value = (await api.get('/admin/service-messages')).data.data
}

async function reply(row) {
  const content = window.prompt('请输入回复内容', row.replyContent || '')
  if (content === null || !content.trim()) return
  await api.put(`/admin/service-messages/${row.id}/reply`, { replyContent: content.trim() })
  ElMessage.success('回复已提交')
  await loadAll()
}

onMounted(loadAll)
</script>
