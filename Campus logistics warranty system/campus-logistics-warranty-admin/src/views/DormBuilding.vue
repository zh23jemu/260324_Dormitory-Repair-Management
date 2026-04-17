<template>
  <div class="dorm-building-manage">
    <div class="search-form">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="请输入楼栋编号或名称" clearable />
        </el-form-item>
        <el-form-item label="楼栋类型">
          <el-select v-model="queryParams.buildingType" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="男生宿舍" :value="1" />
            <el-option label="女生宿舍" :value="2" />
            <el-option label="混合" :value="3" />
            <el-option label="教职工" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="停用" :value="0" />
            <el-option label="启用" :value="1" />
            <el-option label="维修中" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="handleAdd">新增宿舍楼</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="buildingId" label="楼栋编号" width="100" />
      <el-table-column prop="buildingName" label="楼栋名称" width="130" />
      <el-table-column prop="buildingType" label="楼栋类型" width="110">
        <template #default="{row}">
          <el-tag :type="getBuildingTypeTag(row.buildingType)">
            {{ getBuildingTypeText(row.buildingType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalFloors" label="总楼层数" width="90" />
      <el-table-column prop="managerName" label="宿管姓名" width="100" />
      <el-table-column prop="managerPhone" label="宿管电话" width="120" />
      <el-table-column prop="img" label="楼栋图片" width="100">
        <template #default="{row}">
          <el-image v-if="row.img" :src="row.img" :preview-src-list="[row.img]" 
            style="width: 50px; height: 50px; border-radius: 4px;" fit="cover" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="getStatusTag(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{row}">
          {{ row.createdAt ? new Date(row.createdAt).toLocaleString() : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="180">
        <template #default="{row}">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="form.buildingId ? '编辑宿舍楼' : '新增宿舍楼'"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="楼栋编号" prop="buildingId">
          <el-input v-model="form.buildingId" placeholder="请输入楼栋编号，如 A1" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="楼栋名称" prop="buildingName">
          <el-input v-model="form.buildingName" placeholder="请输入楼栋名称" />
        </el-form-item>
        <el-form-item label="楼栋类型" prop="buildingType">
          <el-select v-model="form.buildingType" placeholder="请选择楼栋类型" style="width: 100%">
            <el-option label="男生宿舍" :value="1" />
            <el-option label="女生宿舍" :value="2" />
            <el-option label="混合" :value="3" />
            <el-option label="教职工" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="总楼层数" prop="totalFloors">
          <el-input-number v-model="form.totalFloors" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="宿管姓名" prop="managerName">
          <el-input v-model="form.managerName" placeholder="请输入宿管姓名" />
        </el-form-item>
        <el-form-item label="宿管电话" prop="managerPhone">
          <el-input v-model="form.managerPhone" placeholder="请输入宿管电话" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="停用" :value="0" />
            <el-option label="启用" :value="1" />
            <el-option label="维修中" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼栋图片" prop="img">
          <div class="image-upload-wrapper">
            <el-upload
              class="image-uploader"
              action=""
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleImageChange"
            >
              <div v-if="form.img" class="image-preview">
                <img :src="form.img" class="preview-image" />
                <div class="image-edit-overlay">
                  <el-icon><EditPen /></el-icon>
                </div>
              </div>
              <div v-else class="image-empty">
                <el-icon class="image-icon"><Plus /></el-icon>
                <div class="image-text">点击上传图片</div>
              </div>
            </el-upload>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getDormBuildingList, getDormBuildingById, deleteDormBuilding, saveDormBuilding, updateDormBuilding } from '@/api/dormBuilding'
import { uploadFile } from '@/api/file'
import { Plus, EditPen } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const formRef = ref(null)
const imageFile = ref(null)
const isEdit = ref(false)

const queryParams = reactive({
  keyword: '',
  buildingType: '',
  status: ''
})

const form = ref({
  buildingId: '',
  buildingName: '',
  buildingType: 1,
  totalFloors: 6,
  managerName: '',
  managerPhone: '',
  status: 1,
  img: ''
})

const rules = {
  buildingId: [{ required: true, message: '请输入楼栋编号', trigger: 'blur' }],
  buildingName: [{ required: true, message: '请输入楼栋名称', trigger: 'blur' }],
  buildingType: [{ required: true, message: '请选择楼栋类型', trigger: 'change' }],
  totalFloors: [{ required: true, message: '请输入总楼层数', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const getBuildingTypeText = (type) => {
  const map = { 1: '男生宿舍', 2: '女生宿舍', 3: '混合', 4: '教职工' }
  return map[type] || '-'
}

const getBuildingTypeTag = (type) => {
  const map = { 1: 'primary', 2: 'danger', 3: 'warning', 4: 'success' }
  return map[type] || ''
}

const getStatusText = (status) => {
  const map = { 0: '停用', 1: '启用', 2: '维修中' }
  return map[status] || '-'
}

const getStatusTag = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[status] || ''
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getDormBuildingList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...queryParams
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取宿舍楼列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pageNum.value = 1
  getList()
}

const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.buildingType = ''
  queryParams.status = ''
  handleQuery()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  getList()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  getList()
}

const resetForm = () => {
  form.value = {
    buildingId: '',
    buildingName: '',
    buildingType: 1,
    totalFloors: 6,
    managerName: '',
    managerPhone: '',
    status: 1,
    img: ''
  }
  imageFile.value = null
}

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getDormBuildingById(row.buildingId)
    if (res.code === 200) {
      form.value = { ...res.data }
      isEdit.value = true
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取宿舍楼详情失败:', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该宿舍楼吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteDormBuilding(row.buildingId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除宿舍楼失败:', error)
    }
  })
}

const handleImageChange = (file) => {
  imageFile.value = file.raw
  // 预览
  const reader = new FileReader()
  reader.onload = (e) => {
    form.value.img = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const handleSubmit = () => {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      try {
        // 上传图片
        if (imageFile.value) {
          const uploadRes = await uploadFile(imageFile.value)
          if (uploadRes.code === 200) {
            form.value.img = uploadRes.data
          } else {
            ElMessage.error('图片上传失败')
            return
          }
        }
        
        const res = isEdit.value ? await updateDormBuilding(form.value) : await saveDormBuilding(form.value)
        
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
          dialogVisible.value = false
          getList()
        }
      } catch (error) {
        console.error('保存宿舍楼失败:', error)
      }
    }
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.image-upload-wrapper {
  width: 150px;
  height: 150px;
}

.image-uploader {
  width: 100%;
  height: 100%;
}

.image-empty {
  width: 150px;
  height: 150px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fafafa;
}

.image-empty:hover {
  border-color: #409EFF;
  background-color: #f0f7ff;
}

.image-icon {
  font-size: 28px;
  color: #8c939d;
  margin-bottom: 8px;
}

.image-text {
  font-size: 14px;
  color: #8c939d;
}

.image-preview {
  width: 150px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  border: 1px solid #e0e0e0;
  cursor: pointer;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-edit-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: #fff;
  font-size: 28px;
}

.image-preview:hover .image-edit-overlay {
  opacity: 1;
}
</style>
