<template>
  <div class="student-page">
    <section class="student-hero">
      <div class="student-hero__copy">
        <div class="student-hero__eyebrow">服务反馈通道</div>
        <h1>服务留言反馈</h1>
        <p>向管理员提交建议、问题和使用反馈，并查看后续回复结果。</p>
      </div>
    </section>

    <section class="student-layout">
      <div class="student-main">
        <div class="student-card student-card--accent">
          <div class="student-card__header">
            <div>
              <h2>提交留言</h2>
              <p>可附带图片说明问题或建议，管理员查看后会在系统内回复。</p>
            </div>
          </div>
          <div class="student-form-stack">
            <van-field v-model="form.title" label="标题" placeholder="例如：希望增加维修进度提醒" />
            <van-field v-model="form.content" label="内容" type="textarea" rows="4" placeholder="请填写建议、意见或使用中遇到的问题" />
            <div class="student-upload-panel">
              <van-uploader v-model="files" :after-read="afterRead" :max-count="1" />
            </div>
            <van-button type="primary" block @click="submitMessage">提交留言</van-button>
          </div>
        </div>
        <div class="student-card">
          <div class="student-card__header">
            <div>
              <h2>我的留言</h2>
              <p>查看已提交的留言内容、图片附件和管理员回复结果。</p>
            </div>
          </div>
          <div v-if="messages.length" class="student-message-grid">
            <article v-for="item in messages" :key="item.id" class="student-message-card">
              <div class="student-message-card__head">
                <strong>{{ item.title }}</strong>
                <span class="student-status-pill">{{ item.status }}</span>
              </div>
              <p>{{ item.content }}</p>
              <img v-if="item.imagePath" :src="fileUrl(item.imagePath)" alt="留言图片" class="student-message-card__image" />
              <div v-if="item.replyContent" class="student-message-card__reply">
                <strong>管理员回复</strong>
                <p>{{ item.replyContent }}</p>
              </div>
            </article>
          </div>
          <van-empty v-else description="暂无留言" />
        </div>
      </div>
    </section>
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
