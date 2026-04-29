<template>
  <div class="student-page">
    <section class="student-hero">
      <div class="student-hero__copy">
        <div class="student-hero__eyebrow">学生个人中心</div>
        <h1>个人信息管理</h1>
        <p>查看账号、住宿信息与联系方式，并可上传头像、更新手机号。</p>
      </div>
    </section>

    <section class="student-layout">
      <div class="student-main">
        <div class="student-card">
          <div class="profile-avatar">
            <van-uploader :after-read="uploadAvatar" :max-count="1" :show-upload="false">
              <van-image round width="96" height="96" :src="fileUrl(form.avatar) || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'" />
            </van-uploader>
            <span>点击头像上传新头像</span>
          </div>
          <div class="student-info-grid">
            <div class="student-info-item"><span>姓名</span><strong>{{ profile.realName || '' }}</strong></div>
            <div class="student-info-item"><span>账号</span><strong>{{ profile.username || '' }}</strong></div>
            <div class="student-info-item"><span>学号</span><strong>{{ profile.studentNo || '' }}</strong></div>
            <div class="student-info-item"><span>学院</span><strong>{{ profile.college || '' }}</strong></div>
            <div class="student-info-item"><span>专业</span><strong>{{ profile.major || '' }}</strong></div>
            <div class="student-info-item"><span>班级</span><strong>{{ profile.className || '' }}</strong></div>
            <div class="student-info-item student-info-item--full"><span>宿舍</span><strong>{{ `${profile.buildingName || ''} ${profile.roomNo || ''} ${profile.bedNo || ''}` }}</strong></div>
          </div>
          <div style="margin-top: 14px">
            <van-field v-model="form.phone" label="手机号" placeholder="请输入手机号" />
            <van-button type="primary" block @click="saveProfile">保存信息</van-button>
          </div>
          <div style="margin-top: 12px">
            <van-button plain type="primary" block @click="router.push('/student/security')">设置找回密码问题</van-button>
          </div>
          <div style="margin-top: 12px">
            <van-button plain danger block @click="logout">退出登录</van-button>
          </div>
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
import { useAuth } from '../../utils/auth'
import { fileUrl } from '../../utils/file'

const router = useRouter()
const auth = useAuth()
const profile = ref({})
const form = reactive({ phone: '', avatar: '' })

async function loadProfile() {
  profile.value = (await api.get('/student/profile')).data.data
  form.phone = profile.value.phone || ''
  form.avatar = profile.value.avatar || ''
}

async function saveProfile() {
  await api.put('/student/profile', { phone: form.phone, avatar: form.avatar, buildingId: profile.value.buildingId, roomId: profile.value.roomId, bedNo: profile.value.bedNo })
  showToast('个人信息已更新')
  await loadProfile()
  await auth.loadMe()
}

async function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file.file)
  const { data } = await api.post('/common/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
  form.avatar = data.data.filePath
  showToast('头像已上传，请点击保存信息')
}

function logout() {
  auth.logout()
  router.replace('/home')
}

onMounted(loadProfile)
</script>
