<template>
  <div class="student-page">
    <section class="student-hero">
      <div class="student-hero__copy">
        <div class="student-hero__eyebrow">学生交流社区</div>
        <h1>公开论坛</h1>
        <p>分享报修经验、宿舍设施使用建议和服务反馈，登录后可发布帖子。</p>
      </div>
    </section>

    <section class="student-layout">
      <div class="student-main">
        <div class="student-card student-card--accent student-forum-composer">
          <div class="student-card__header">
            <div>
              <h2>发布帖子</h2>
              <p>围绕宿舍报修、维修体验、设施使用建议等主题进行公开交流。</p>
            </div>
          </div>
          <div class="student-form-stack student-forum-composer__stack">
            <div class="repair-form-group">
              <div class="repair-form-group__title">帖子内容</div>
              <div class="repair-form-group__desc">建议使用清晰、具体的标题和正文，方便其他同学快速理解你的经验或建议。</div>
              <div class="repair-form-grid student-forum-form-grid">
                <div class="repair-form-field repair-form-field--full">
                  <label>帖子标题</label>
                  <van-field v-model="form.title" placeholder="请输入帖子标题，例如：建议报修时补充故障照片" class="student-form-field" />
                </div>
                <div class="repair-form-field repair-form-field--full">
                  <label>正文内容</label>
                  <van-field
                    v-model="form.content"
                    type="textarea"
                    rows="5"
                    autosize
                    placeholder="分享报修经验、宿舍设施使用建议、维修沟通建议等公开交流内容"
                    class="student-form-field student-form-field--textarea"
                  />
                </div>
              </div>
            </div>

            <div class="repair-form-group">
              <div class="repair-form-group__title">图片附件</div>
              <div class="repair-form-group__desc">可上传 1 张配图，用于补充说明问题场景、示意内容或经验截图。</div>
              <div class="student-upload-panel student-upload-panel--spacious">
                <van-uploader v-model="files" :after-read="afterRead" :max-count="1" />
              </div>
            </div>
            <van-button type="primary" block class="student-primary-button" @click="submitPost">发布帖子</van-button>
          </div>
        </div>

        <div class="student-card">
          <div class="student-card__header">
            <div>
              <h2>帖子列表</h2>
              <p>最新帖子按时间展示，便于快速浏览公开交流内容。</p>
            </div>
          </div>
          <div v-if="posts.length" class="student-forum-grid">
            <article v-for="item in posts" :key="item.id" class="student-forum-card">
              <div class="student-forum-card__author-row">
                <el-avatar :src="fileUrl(item.avatar) || defaultAvatar" :size="46" />
                <div class="student-forum-card__author-info">
                  <strong>{{ item.username || item.studentName || '同学' }}</strong>
                  <span>发帖时间：{{ item.createdAt }}</span>
                </div>
              </div>
              <h3>{{ item.title }}</h3>
              <p>{{ item.content }}</p>
              <img v-if="item.imagePath" :src="fileUrl(item.imagePath)" alt="帖子图片" />
            </article>
          </div>
          <van-empty v-else description="暂无帖子" />
        </div>
      </div>
    </section>
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
const defaultAvatar = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'

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
