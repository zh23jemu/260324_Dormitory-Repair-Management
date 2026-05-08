<template>
  <div class="student-page">
    <section class="student-hero">
      <div class="student-hero__copy">
        <div class="student-hero__eyebrow">学生交流社区</div>
        <h1>发布帖子</h1>
        <p>围绕宿舍报修、维修体验、设施使用建议等主题发帖，发布后会出现在论坛交流列表中。</p>
      </div>
    </section>

    <section class="student-layout">
      <div class="student-main">
        <div class="student-card student-card--accent student-forum-composer">
          <div class="student-card__header">
            <div>
              <h2>帖子内容</h2>
              <p>建议使用清晰、具体的标题和正文，方便其他同学快速理解你的经验或建议。</p>
            </div>
            <el-button @click="router.push('/forum')">返回论坛</el-button>
          </div>
          <div class="student-form-stack student-forum-composer__stack">
            <div class="repair-form-group">
              <div class="repair-form-group__title">基础信息</div>
              <div class="repair-form-group__desc">帖子发布后默认展示为已发布状态，管理员仍可在后台进行隐藏或删除。</div>
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
                    rows="6"
                    autosize
                    placeholder="分享报修经验、宿舍设施使用建议、维修沟通建议等交流内容"
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

            <div class="student-forum-create-actions">
              <el-button @click="router.push('/forum')">取消</el-button>
              <el-button type="primary" @click="submitPost">发布帖子</el-button>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import api from '../../api'
import { fileUrl } from '../../utils/file'
import { useAuth } from '../../utils/auth'

const router = useRouter()
const auth = useAuth()
const files = ref([])
const form = reactive({ title: '', content: '', imagePath: '' })

async function afterRead(file) {
  const formData = new FormData()
  formData.append('file', file.file)
  const { data } = await api.post('/common/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
  form.imagePath = data.data.filePath
  file.url = fileUrl(form.imagePath)
}

async function submitPost() {
  if (!auth.hasToken()) {
    router.push(`/login?redirect=${encodeURIComponent('/forum/create')}&role=student`)
    return
  }
  if (!form.title.trim() || !form.content.trim()) {
    showToast('请填写标题和内容')
    return
  }
  await api.post('/student/forum-posts', {
    title: form.title.trim(),
    content: form.content.trim(),
    imagePath: form.imagePath
  })
  showToast('帖子已发布')
  router.push('/forum')
}
</script>

<style scoped>
.student-forum-create-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 640px) {
  .student-forum-create-actions {
    flex-direction: column-reverse;
  }
}
</style>
