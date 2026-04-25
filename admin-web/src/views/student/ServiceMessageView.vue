<template>
  <div class="page-stack">
    <div class="section">
      <div class="section-title">服务留言反馈</div>
      <van-field v-model="form.title" label="标题" placeholder="例如：希望增加维修进度提醒" />
      <van-field v-model="form.content" label="内容" type="textarea" rows="4" placeholder="请填写建议、意见或使用中遇到的问题" />
      <van-uploader v-model="files" :after-read="afterRead" :max-count="1" />
      <van-button type="primary" block @click="submitMessage">提交留言</van-button>
    </div>
    <div class="section">
      <div class="section-title">我的留言</div>
      <van-cell-group inset>
        <van-cell v-for="item in messages" :key="item.id" :title="item.title" :label="item.content" :value="item.status" />
      </van-cell-group>
      <template v-for="item in messages" :key="`extra-${item.id}`">
        <div v-if="item.imagePath" class="message-reply">
          <img :src="fileUrl(item.imagePath)" alt="留言图片" style="width: 100%; border-radius: 12px" />
        </div>
        <div v-if="item.replyContent" class="message-reply">
          <strong>管理员回复：</strong>{{ item.replyContent }}
        </div>
      </template>
      <van-empty v-if="!messages.length" description="暂无留言" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { showToast } from 'vant'
import api from '../../api'
import { fileUrl } from '../../utils/file'

const form = reactive({ title: '', content: '', imagePath: '' })
const messages = ref([])
const files = ref([])

async function loadMessages() {
  const { data } = await api.get('/student/service-messages')
  messages.value = data.data || []
}

async function submitMessage() {
  if (!form.title || !form.content) {
    showToast('请填写留言标题和内容')
    return
  }
  try {
    const { data } = await api.post('/student/service-messages', form)
    if (data.code !== 200) {
      showToast(data.message || '留言提交失败')
      return
    }
    showToast('留言已提交')
    form.title = ''
    form.content = ''
    form.imagePath = ''
    files.value = []
    await loadMessages()
  } catch (error) {
    showToast(error.response?.data?.message || '留言提交失败，请稍后重试')
  }
}

async function afterRead(file) {
  try {
    const formData = new FormData()
    formData.append('file', file.file)
    const { data } = await api.post('/common/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    form.imagePath = data.data.filePath
    file.url = fileUrl(form.imagePath)
    showToast('图片已上传')
  } catch (error) {
    showToast('图片上传失败')
  }
}

onMounted(loadMessages)
</script>
