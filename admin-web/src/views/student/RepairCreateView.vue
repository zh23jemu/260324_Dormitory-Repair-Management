<template>
  <div class="repair-create-page">
    <section class="repair-create-hero">
      <div class="repair-create-hero__copy">
        <div class="repair-create-hero__eyebrow">学生报修服务</div>
        <h1>在线提交宿舍报修</h1>
        <p>围绕宿舍水电、门窗、网络和设施故障快速发起工单。填写越清楚，维修人员越容易快速判断和处理。</p>
        <div class="repair-create-hero__tips">
          <span>1. 先选择报修类型</span>
          <span>2. 尽量填写准确时间</span>
          <span>3. 上传现场图片更利于处理</span>
        </div>
      </div>
      <div class="repair-create-hero__panel">
        <div class="repair-create-hero__metric">
          <span>当前可选类型</span>
          <strong>{{ repairTypes.length }}</strong>
          <small>覆盖水电、家具、门窗、网络等常见场景</small>
        </div>
        <div class="repair-create-hero__metric">
          <span>设施关联</span>
          <strong>{{ facilities.length }}</strong>
          <small>可将工单直接关联到宿舍设施台账</small>
        </div>
      </div>
    </section>

    <section class="repair-create-layout">
      <div class="repair-create-main">
        <div class="repair-create-card">
          <div class="repair-create-card__header">
            <div>
              <h2>报修信息填写</h2>
              <p>请根据实际情况填写故障内容、期望上门时间和现场图片。</p>
            </div>
          </div>

          <div class="repair-create-form">
            <div class="repair-form-group">
              <div class="repair-form-group__title">基础信息</div>
              <div class="repair-form-grid">
                <div class="repair-form-field repair-form-field--full">
                  <label>报修标题</label>
                  <van-field v-model="form.title" placeholder="例如：卫生间水龙头漏水、宿舍门锁损坏" />
                </div>
                <div class="repair-form-field repair-form-field--full">
                  <label>问题描述</label>
                  <van-field
                    v-model="form.description"
                    type="textarea"
                    rows="5"
                    autosize
                    placeholder="请尽量写清故障现象、出现时间、持续时长、影响范围，以及是否存在漏水、断电等紧急情况"
                  />
                </div>
                <div class="repair-form-field">
                  <label>期望上门时间</label>
                  <div class="repair-datetime-box">
                    <input v-model="form.expectTimeInput" class="datetime-input" type="datetime-local" />
                  </div>
                </div>
                <div class="repair-form-field">
                  <label>当前已上传图片</label>
                  <div class="repair-create-counter">{{ uploaderFiles.length }} / 3 张</div>
                </div>
              </div>
            </div>

            <div class="repair-form-group">
              <div class="repair-form-group__title">报修类型</div>
              <van-radio-group v-model="form.repairTypeId" direction="horizontal" class="repair-chip-group">
                <van-radio v-for="item in repairTypes" :key="item.id" :name="item.id" class="repair-chip-radio">
                  {{ item.typeName }}
                </van-radio>
              </van-radio-group>
            </div>

            <div class="repair-form-group">
              <div class="repair-form-group__title">设施关联</div>
              <div class="repair-form-group__desc">如果本次报修对应到宿舍内的具体设施，建议一并关联，便于后续维修记录沉淀。</div>
              <van-radio-group v-model="form.facilityId" direction="vertical" class="repair-facility-list">
                <van-radio :name="null" class="repair-facility-item">不关联设施</van-radio>
                <van-radio v-for="item in facilities" :key="item.id" :name="item.id" class="repair-facility-item">
                  {{ item.facilityName }} / {{ item.facilityType }}
                </van-radio>
              </van-radio-group>
            </div>

            <div class="repair-form-group">
              <div class="repair-form-group__title">现场图片</div>
              <div class="repair-form-group__desc">支持最多 3 张图片，建议上传故障位置、影响范围和设备近景。</div>
              <div class="repair-upload-box">
                <van-uploader
                  v-model="uploaderFiles"
                  :after-read="afterRead"
                  :preview-full-image="false"
                  multiple
                  :max-count="3"
                  @click-preview="previewUploadImage"
                />
              </div>
            </div>
          </div>

          <div class="repair-create-actions">
            <van-button type="primary" block @click="submitRepair">提交工单</van-button>
          </div>
        </div>
      </div>

      <aside class="repair-create-side">
        <div class="repair-create-side-card">
          <div class="repair-create-side-card__title">填写建议</div>
          <div class="repair-create-side-list">
            <div class="repair-create-side-item">
              <strong>描述清楚故障现象</strong>
              <span>例如是否漏水、异响、无法通电、门锁无法闭合等。</span>
            </div>
            <div class="repair-create-side-item">
              <strong>注明方便上门时间</strong>
              <span>填写期望时间后，维修人员可结合工单安排尽快联系处理。</span>
            </div>
            <div class="repair-create-side-item">
              <strong>优先上传现场照片</strong>
              <span>图片有助于提前判断材料、工具和维修方式。</span>
            </div>
          </div>
        </div>

        <div class="repair-create-side-card repair-create-side-card--highlight">
          <div class="repair-create-side-card__title">当前填写摘要</div>
          <div class="repair-create-summary">
            <div class="repair-create-summary__row">
              <span>报修类型</span>
              <strong>{{ selectedRepairTypeName }}</strong>
            </div>
            <div class="repair-create-summary__row">
              <span>关联设施</span>
              <strong>{{ selectedFacilityName }}</strong>
            </div>
            <div class="repair-create-summary__row">
              <span>期望时间</span>
              <strong>{{ form.expectTimeInput || '暂未选择' }}</strong>
            </div>
          </div>
        </div>
      </aside>
    </section>

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
import { computed, onMounted, reactive, ref } from 'vue'
import { showNotify, showToast } from 'vant'
import api from '../../api'

const repairTypes = ref([])
const facilities = ref([])
const uploaderFiles = ref([])
const previewVisible = ref(false)
const previewImages = ref([])
const previewIndex = ref(0)
const form = reactive({ repairTypeId: null, title: '', description: '', expectTimeInput: '', facilityId: null, imagePaths: [] })

const selectedRepairTypeName = computed(() => {
  return repairTypes.value.find((item) => item.id === form.repairTypeId)?.typeName || '暂未选择'
})

const selectedFacilityName = computed(() => {
  if (form.facilityId === null || form.facilityId === undefined) {
    return '不关联设施'
  }
  const facility = facilities.value.find((item) => item.id === form.facilityId)
  return facility ? `${facility.facilityName} / ${facility.facilityType}` : '不关联设施'
})

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
