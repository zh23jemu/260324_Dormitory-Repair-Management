<template>
  <div class="page-stack">
    <div class="section">
      <div class="section-title">提交报修</div>
      <van-field v-model="form.title" label="标题" placeholder="例如：卫生间水龙头漏水" />
      <van-field v-model="form.description" label="问题描述" type="textarea" rows="4" placeholder="请尽量写清故障现象、出现时间和影响范围" />
      <van-field label="期望时间">
        <template #input>
          <input v-model="form.expectTimeInput" class="datetime-input" type="datetime-local" />
        </template>
      </van-field>
      <van-field label="报修类型">
        <template #input>
          <van-radio-group v-model="form.repairTypeId" direction="horizontal" class="type-group">
            <van-radio v-for="item in repairTypes" :key="item.id" :name="item.id">{{ item.typeName }}</van-radio>
          </van-radio-group>
        </template>
      </van-field>
      <van-field label="关联设施">
        <template #input>
          <van-radio-group v-model="form.facilityId" direction="vertical" class="facility-group">
            <van-radio :name="null">不关联设施</van-radio>
            <van-radio v-for="item in facilities" :key="item.id" :name="item.id">{{ item.facilityName }} / {{ item.facilityType }}</van-radio>
          </van-radio-group>
        </template>
      </van-field>
      <div style="margin: 10px 0 14px">
        <van-uploader v-model="uploaderFiles" :after-read="afterRead" :preview-full-image="false" multiple :max-count="3" @click-preview="previewUploadImage" />
      </div>
      <van-button type="primary" block @click="submitRepair">提交工单</van-button>
    </div>

    <van-image-preview
      v-model:show="previewVisible"
      :images="previewImages"
      :start-position="previewIndex"
      :close-on-click-image="true"
      :close-on-click-overlay="true"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { showNotify, showToast } from 'vant'
import api from '../../api'

const repairTypes = ref([])
const facilities = ref([])
const uploaderFiles = ref([])
const previewVisible = ref(false)
const previewImages = ref([])
const previewIndex = ref(0)
const form = reactive({ repairTypeId: null, title: '', description: '', expectTimeInput: '', facilityId: null, imagePaths: [] })

function toApiDateTime(value) {
  return value ? `${value.replace('T', ' ')}:00` : ''
}

async function loadBase() {
  repairTypes.value = (await api.get('/student/repair-types')).data.data
  facilities.value = (await api.get('/student/facilities')).data.data
  if (!form.repairTypeId && repairTypes.value.length) {
    form.repairTypeId = repairTypes.value[0].id
  }
}

async function afterRead(file) {
  const files = Array.isArray(file) ? file : [file]
  for (const item of files) {
    const formData = new FormData()
    formData.append('file', item.file)
    const { data } = await api.post('/common/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    form.imagePaths.push(data.data.filePath)
  }
  showToast('图片上传完成')
}

function previewUploadImage(file) {
  previewImages.value = uploaderFiles.value.map((item) => item.url || item.content || '').filter(Boolean)
  const target = file.url || file.content || ''
  previewIndex.value = Math.max(0, previewImages.value.indexOf(target))
  previewVisible.value = previewImages.value.length > 0
}

async function submitRepair() {
  await api.post('/student/repair-orders', {
    repairTypeId: form.repairTypeId,
    title: form.title,
    description: form.description,
    expectTime: toApiDateTime(form.expectTimeInput),
    facilityId: form.facilityId,
    imagePaths: form.imagePaths
  })
  showNotify({ type: 'success', message: '报修提交成功' })
  Object.assign(form, { repairTypeId: repairTypes.value[0]?.id || null, title: '', description: '', expectTimeInput: '', facilityId: null, imagePaths: [] })
  uploaderFiles.value = []
}

onMounted(loadBase)
</script>
