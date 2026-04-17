<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from '@/store'
import { getRepairTypeList } from '@/api/repairType'
import { saveRepairRecord } from '@/api/repairRecord'
import { upload } from '@/api/file'
import { getAllDormBuildings } from '@/api/dormBuilding'
import { getAllDormRooms } from '@/api/dormRoom'

const router = useRouter()
const route = useRoute()
const userStore = useStore()

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 报修表单
const form = reactive({
  name: '', // 报修标题
  description: '', // 问题描述
  location: '', // 维修地点
  phone: '', // 联系方式
  typeId: '', // 维修类型ID
  img: '', // 图片地址
  status: '待处理', // 状态默认为待处理
  isCharge: 0, // 默认免费
  price: null // 预估价格，付费时填写
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入报修标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入问题描述', trigger: 'blur' }],
  location: [{ required: true, message: '请输入维修地点', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  typeId: [{ required: true, message: '请选择报修类型', trigger: 'change' }],
  price: [
    { 
      validator: (rule, value, callback) => {
        if (form.isCharge === 1 && (!value || value <= 0)) {
          callback(new Error('付费维修需要填写预估价格'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 获取报修类型列表
const repairTypes = ref([])

// 宿舍楼列表
const buildingList = ref([])
// 房间列表（根据选择的楼栋过滤）
const roomList = ref([])
const filteredRoomList = ref([])

// 地点选择
const locationSelect = reactive({
  buildingId: '',
  floor: '',
  roomNumber: ''
})

// 楼层列表（根据选择的楼栋动态生成）
const floorList = ref([])

// 图片上传相关
const imageList = ref([])
const uploadLimit = 1 // 限制只能上传一张图片

// 表单引用
const formRef = ref(null)

// 当付费状态变化时，重置价格
watch(() => form.isCharge, (newVal) => {
  if (newVal === 0) {
    form.price = null
  }
})

// 监听楼栋选择变化
watch(() => locationSelect.buildingId, (newVal) => {
  locationSelect.floor = ''
  locationSelect.roomNumber = ''
  floorList.value = []
  filteredRoomList.value = []
  
  if (newVal) {
    // 根据选择的楼栋获取楼层列表
    const rooms = roomList.value.filter(room => room.buildingId === newVal)
    const floors = [...new Set(rooms.map(room => room.floor))].sort((a, b) => a - b)
    floorList.value = floors
  }
  
  updateLocation()
})

// 监听楼层选择变化
watch(() => locationSelect.floor, (newVal) => {
  locationSelect.roomNumber = ''
  filteredRoomList.value = []
  
  if (newVal && locationSelect.buildingId) {
    // 根据选择的楼栋和楼层过滤房间
    filteredRoomList.value = roomList.value.filter(
      room => room.buildingId === locationSelect.buildingId && room.floor === newVal
    )
  }
  
  updateLocation()
})

// 监听房间号选择变化
watch(() => locationSelect.roomNumber, () => {
  updateLocation()
})

// 更新地点字符串
const updateLocation = () => {
  const parts = []
  
  if (locationSelect.buildingId) {
    const building = buildingList.value.find(b => b.buildingId === locationSelect.buildingId)
    if (building) {
      parts.push(building.buildingName)
    }
  }
  
  if (locationSelect.floor) {
    parts.push(`${locationSelect.floor}层`)
  }
  
  if (locationSelect.roomNumber) {
    parts.push(`${locationSelect.roomNumber}室`)
  }
  
  form.location = parts.join('')
}

// 初始化数据
onMounted(async () => {
  loading.value = true
  try {
    // 获取报修类型列表
    const typeRes = await getRepairTypeList()
    if (typeRes.code === 200) {
      repairTypes.value = typeRes.data.records || []
      
      // 如果存在query参数中的typeId，设置表单的typeId
      const { typeId } = route.query
      if (typeId && repairTypes.value.some(type => type.id === Number(typeId))) {
        form.typeId = Number(typeId)
      }
    }
    
    // 获取宿舍楼列表
    const buildingRes = await getAllDormBuildings()
    if (buildingRes.code === 200) {
      buildingList.value = buildingRes.data || []
    }
    
    // 获取房间列表
    const roomRes = await getAllDormRooms()
    if (roomRes.code === 200) {
      roomList.value = roomRes.data || []
    }
  } catch (error) {
    console.error('初始化数据失败', error)
    ElMessage.error('初始化数据失败')
  } finally {
    loading.value = false
  }
})

// 处理图片上传成功
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    form.img = response.data
    imageList.value = [{ url: response.data }]
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 处理图片上传错误
const handleUploadError = () => {
  ElMessage.error('上传失败，请重试')
}

// 处理图片上传之前
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  
  if (imageList.value.length >= uploadLimit) {
    ElMessage.warning(`最多上传 ${uploadLimit} 张图片`)
    return false
  }
  
  return true
}

// 自定义上传方法
const customUpload = async (options) => {
  const { file, onSuccess, onError } = options
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const res = await upload(formData)
    onSuccess(res)
  } catch (error) {
    console.error('上传错误', error)
    onError()
  }
}

// 移除图片
const handleRemove = () => {
  form.img = ''
  imageList.value = []
}

// 提交表单
const submitForm = async () => {
  if (formRef.value) {
    await formRef.value.validate(async (valid) => {
      if (valid) {
        submitting.value = true
        try {
          // 构建报修记录数据
          const recordData = {
            ...form,
            userId: userStore.userInfo.id
          }
          
          const res = await saveRepairRecord(recordData)
          
          if (res.code === 200) {
            ElMessage.success('报修提交成功')
            router.push('/repair/list')
          } else {
            ElMessage.error(res.message || '提交失败')
          }
        } catch (error) {
          console.error('提交报修失败', error)
          ElMessage.error('提交失败，请检查网络连接')
        } finally {
          submitting.value = false
        }
      }
    })
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}
</script>

<template>
  <div class="repair-create-container">
    <div class="page-header">
      <el-page-header @back="goBack">
        <template #content>
          <span class="page-title">创建报修单</span>
        </template>
      </el-page-header>
    </div>
    
    <el-card class="form-card" v-loading="loading">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        :disabled="submitting"
      >
        <el-form-item label="报修类型" prop="typeId">
          <el-select v-model="form.typeId" placeholder="请选择报修类型" style="width: 100%">
            <el-option
              v-for="item in repairTypes"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="报修标题" prop="name">
          <el-input v-model="form.name" placeholder="简要描述您的问题" />
        </el-form-item>
        
        <el-form-item label="问题描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            placeholder="请详细描述您遇到的问题"
          />
        </el-form-item>
        
        <el-form-item label="维修地点" prop="location">
          <div class="location-select">
            <el-select v-model="locationSelect.buildingId" placeholder="请选择楼栋" style="width: 180px;">
              <el-option
                v-for="building in buildingList"
                :key="building.buildingId"
                :label="building.buildingName"
                :value="building.buildingId"
              />
            </el-select>
            
            <el-select 
              v-model="locationSelect.floor" 
              placeholder="请选择楼层" 
              :disabled="!locationSelect.buildingId"
              style="width: 120px; margin-left: 10px;"
            >
              <el-option
                v-for="floor in floorList"
                :key="floor"
                :label="`${floor}层`"
                :value="floor"
              />
            </el-select>
            
            <el-select 
              v-model="locationSelect.roomNumber" 
              placeholder="请选择房间" 
              :disabled="!locationSelect.floor"
              style="width: 150px; margin-left: 10px;"
            >
              <el-option
                v-for="room in filteredRoomList"
                :key="room.roomId"
                :label="`${room.roomNumber}室`"
                :value="room.roomNumber"
              />
            </el-select>
          </div>
          <div class="location-preview" v-if="form.location">
            <el-text type="info">地点：{{ form.location }}</el-text>
          </div>
        </el-form-item>
        
        <el-form-item label="联系方式" prop="phone">
          <el-input 
            v-model="form.phone" 
            placeholder="请输入您的手机号" 
            maxlength="11"
          />
        </el-form-item>
        
        <el-form-item label="付费情况">
          <el-radio-group v-model="form.isCharge">
            <el-radio :label="0">免费维修</el-radio>
            <el-radio :label="1">付费维修</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item 
          v-if="form.isCharge === 1" 
          label="预估价格(元)" 
          prop="price"
        >
          <el-input-number 
            v-model="form.price" 
            :min="1" 
            :precision="0" 
            style="width: 200px"
            placeholder="请输入预估价格"
          />
          <div class="price-tip">
            <el-text type="info">请输入您预估的维修价格，最终价格由维修人员确定</el-text>
          </div>
        </el-form-item>
        
        <el-form-item label="上传图片">
          <el-upload
            list-type="picture-card"
            :http-request="customUpload"
            :file-list="imageList"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-remove="handleRemove"
            :limit="uploadLimit"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">
            <el-text type="info">请上传问题相关的图片（仅限 1 张，大小不超过 5MB）</el-text>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="submitting"
            @click="submitForm"
            style="width: 120px"
          >
            提交报修
          </el-button>
          <el-button @click="goBack" style="margin-left: 10px">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.repair-create-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
}

.form-card {
  max-width: 800px;
  margin: 0 auto;
}

.upload-tip, .price-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.location-select {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.location-preview {
  margin-top: 8px;
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style> 