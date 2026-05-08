<template>
  <div class="stack-section">
    <el-card>
      <template #header>服务留言反馈</template>
      <el-table :data="messages">
        <el-table-column prop="studentName" label="学生" width="100" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="commonStatusTagType(row.status)">{{ commonStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column prop="replyContent" label="回复内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="openDetail(row)">查看</el-button>
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

    <el-dialog v-model="detailVisible" title="服务留言详情" width="760px" class="message-detail-dialog">
      <div v-if="currentMessage" class="message-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学生">{{ currentMessage.studentName || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentMessage.studentPhone || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="commonStatusTagType(currentMessage.status)">
              {{ commonStatusText(currentMessage.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentMessage.createdAt || '未记录' }}</el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">{{ currentMessage.title || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="留言内容" :span="2">
            <div class="message-content">{{ currentMessage.content || '未填写' }}</div>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentMessage.imagePath" label="留言图片" :span="2">
            <img
              class="message-image"
              :src="fileUrl(currentMessage.imagePath)"
              alt="留言图片"
              @click="openPreview(fileUrl(currentMessage.imagePath))"
            />
          </el-descriptions-item>
          <el-descriptions-item v-if="currentMessage.replyContent" label="当前回复" :span="2">
            <div class="message-content">{{ currentMessage.replyContent }}</div>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentMessage.repliedByName" label="回复人">
            {{ currentMessage.repliedByName }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentMessage.repliedAt" label="回复时间">
            {{ currentMessage.repliedAt }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="reply-editor">
          <div class="reply-editor-title">管理员回复</div>
          <el-input
            v-model="replyForm.replyContent"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="请输入给学生的回复内容"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">提交回复</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="previewVisible"
      title="图片预览"
      width="760px"
      destroy-on-close
      append-to-body
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      @closed="previewUrl = ''"
    >
      <div class="preview-wrap" @click.self="closePreview">
        <img v-if="previewUrl" :src="previewUrl" alt="留言图片预览" class="preview-image" @click="closePreview" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'
import { commonStatusTagType, commonStatusText } from '../../utils/status'
import { applyPageResult, createPageState, pageParams } from '../../utils/pagination'
import { fileUrl } from '../../utils/file'

const messages = ref([])
const page = reactive(createPageState())
const detailVisible = ref(false)
const currentMessage = ref(null)
const previewVisible = ref(false)
const previewUrl = ref('')
const replyForm = reactive({
  replyContent: ''
})

async function loadAll() {
  messages.value = applyPageResult(page, (await api.get('/admin/service-messages', { params: pageParams(page) })).data.data)
}

function openDetail(row) {
  // 在详情弹窗中统一浏览留言信息和处理回复，避免列表中直接 prompt 导致上下文不足。
  currentMessage.value = row
  replyForm.replyContent = row.replyContent || ''
  detailVisible.value = true
}

async function submitReply() {
  if (!currentMessage.value) return
  if (!replyForm.replyContent.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  await api.put(`/admin/service-messages/${currentMessage.value.id}/reply`, {
    replyContent: replyForm.replyContent.trim()
  })
  ElMessage.success('回复已提交')
  detailVisible.value = false
  await loadAll()
}

function openPreview(url) {
  // 管理端留言详情使用自定义预览弹窗，避免 Element Plus 内置图片预览和详情弹窗遮罩叠加后图片发灰。
  previewUrl.value = url
  previewVisible.value = Boolean(url)
}

function closePreview() {
  previewVisible.value = false
}

onMounted(loadAll)
</script>

<style scoped>
.message-detail {
  display: grid;
  gap: 18px;
}

.message-content {
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
}

.message-image {
  width: 180px;
  height: 120px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.12);
  object-fit: cover;
  cursor: zoom-in;
}

.reply-editor {
  display: grid;
  gap: 10px;
  padding: 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(239, 246, 255, 0.9), rgba(248, 250, 252, 0.95));
  border: 1px solid rgba(96, 165, 250, 0.18);
}

.reply-editor-title {
  font-weight: 700;
  color: #1e3a8a;
}

:deep(.message-detail-dialog .el-dialog__body) {
  padding-top: 10px;
}
</style>
