<template>
  <div class="page-stack">
    <div class="section">
      <div class="section-title">公开论坛</div>
      <van-field v-model="form.title" label="标题" placeholder="请输入帖子标题" />
      <van-field v-model="form.content" label="内容" type="textarea" rows="4" placeholder="分享报修经验、宿舍设施使用建议等" />
      <van-uploader v-model="files" :after-read="afterRead" :max-count="1" />
      <van-button type="primary" block @click="submitPost">发布帖子</van-button>
    </div>

    <div class="section">
      <div class="section-title">帖子列表</div>
      <div v-if="posts.length" class="forum-list">
        <div v-for="item in posts" :key="item.id" class="forum-card">
          <div class="forum-author">{{ item.studentName || '同学' }} · {{ item.createdAt }}</div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.content }}</p>
          <img v-if="item.imagePath" :src="fileUrl(item.imagePath)" alt="帖子图片" />
        </div>
      </div>
      <van-empty v-else description="暂无帖子" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import api from '../../api'
import { fileUrl } from '../../utils/file'
import { useAuth } from '../../utils/auth'

const router = useRouter()
const auth = useAuth()
const posts = ref([])
const files = ref([])
const form = reactive({ title: '', content: '', imagePath: '' })

async function loadPosts() {
  posts.value = (await api.get('/portal/forum-posts')).data.data
}

async function afterRead(file) {
  const formData = new FormData()
  formData.append('file', file.file)
  const { data } = await api.post('/common/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
  form.imagePath = data.data.filePath
  file.url = fileUrl(form.imagePath)
}

async function submitPost() {
  if (!auth.hasToken()) {
    router.push(`/login?redirect=${encodeURIComponent('/forum')}&role=student`)
    return
  }
  if (!form.title || !form.content) {
    showToast('请填写标题和内容')
    return
  }
  await api.post('/student/forum-posts', form)
  showToast('帖子已发布')
  form.title = ''
  form.content = ''
  form.imagePath = ''
  files.value = []
  await loadPosts()
}

onMounted(loadPosts)
</script>
