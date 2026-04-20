<template>
  <div class="page-stack">
    <div class="section">
      <div class="section-title">个人中心</div>
      <div class="profile-avatar">
        <van-uploader :after-read="uploadAvatar" :max-count="1" :show-upload="false">
          <van-image round width="76" height="76" :src="fileUrl(form.avatar) || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'" />
        </van-uploader>
        <span>点击头像上传新头像</span>
      </div>
      <van-cell-group inset>
        <van-cell title="姓名" :value="profile.realName || ''" />
        <van-cell title="账号" :value="profile.username || ''" />
        <van-cell title="学号" :value="profile.studentNo || ''" />
        <van-cell title="学院" :value="profile.college || ''" />
        <van-cell title="专业" :value="profile.major || ''" />
        <van-cell title="班级" :value="profile.className || ''" />
        <van-cell title="宿舍" :value="`${profile.buildingName || ''} ${profile.roomNo || ''} ${profile.bedNo || ''}`" />
      </van-cell-group>
      <div style="margin-top: 14px">
        <van-field v-model="form.phone" label="手机号" placeholder="请输入手机号" />
        <van-button type="primary" block @click="saveProfile">保存信息</van-button>
      </div>
      <div style="margin-top: 12px">
        <van-button plain danger block @click="logout">退出登录</van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import api from '../../api'
import { useStudentAuth } from '../../utils/auth'
import { fileUrl } from '../../utils/file'

const router = useRouter()
const auth = useStudentAuth()
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
  await auth.loadProfile()
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
  router.replace('/login')
}

onMounted(loadProfile)
</script>
