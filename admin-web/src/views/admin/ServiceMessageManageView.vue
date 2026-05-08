<template>
  <div class="stack-section">
    <el-card>
      <template #header>服务留言反馈</template>
      <el-table :data="messages">
        <el-table-column prop="studentName" label="学生" width="100" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="content" label="内容" min-width="220" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="commonStatusTagType(row.status)">{{ commonStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column prop="replyContent" label="回复内容" min-width="220" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="reply(row)">回复</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page.pageNum"
        v-model:page-size="page.pageSize"
        :total="page.total"
        layout="total, sizes, prev, pager, next, jumper"
        class="table-pagination"
        @current-change="loadAll"
        @size-change="loadAll"
      />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'
import { commonStatusTagType, commonStatusText } from '../../utils/status'
import { applyPageResult, createPageState, pageParams } from '../../utils/pagination'

const messages = ref([])
const page = reactive(createPageState())

async function loadAll() {
  messages.value = applyPageResult(page, (await api.get('/admin/service-messages', { params: pageParams(page) })).data.data)
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
