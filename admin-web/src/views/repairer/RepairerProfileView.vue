<template>
  <el-card>
    <template #header>个人信息管理</template>
    <el-form :model="form" label-width="100px" style="max-width:680px">
      <el-form-item label="头像">
        <div class="avatar-row">
          <el-avatar :size="72" :src="fileUrl(form.avatar)" />
          <el-upload :show-file-list="false" :http-request="uploadAvatar">
            <el-button>上传头像</el-button>
          </el-upload>
        </div>
      </el-form-item>
      <el-form-item label="姓名"><el-input v-model="profile.realName" disabled /></el-form-item>
      <el-form-item label="账号"><el-input v-model="profile.username" disabled /></el-form-item>
      <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="维修类型">
        <el-select v-model="form.workTypeCodes" multiple style="width:100%" placeholder="可选择多个维修类型">
          <el-option v-for="item in workTypes" :key="item.dictCode" :label="item.dictName" :value="item.dictCode" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveProfile">保存信息</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'
import { fileUrl } from '../../utils/file'

const profile = reactive({})
const workTypes = ref([])
const form = reactive({ phone: '', avatar: '', workTypeCodes: [] })

async function loadProfile() {
  const { data } = await api.get('/repairer/profile')
  Object.assign(profile, data.data)
  form.phone = profile.phone || ''
  form.avatar = profile.avatar || ''
  form.workTypeCodes = profile.workTypeCodes || []
}

async function loadWorkTypes() {
  workTypes.value = (await api.get('/repairer/work-types')).data.data
}

async function uploadAvatar(option) {
  const formData = new FormData()
  formData.append('file', option.file)
  const { data } = await api.post('/common/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
  form.avatar = data.data.filePath
  ElMessage.success('头像已上传，请保存信息')
}

async function saveProfile() {
  await api.put('/repairer/profile', form)
  ElMessage.success('个人信息已保存')
  await loadProfile()
}

onMounted(async () => {
  await Promise.all([loadProfile(), loadWorkTypes()])
})
</script>

<style scoped>
.avatar-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
</style>
