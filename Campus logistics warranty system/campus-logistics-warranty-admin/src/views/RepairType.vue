<template>
  <div class="repair-type-manage">
    <div class="search-form">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="类型名称">
          <el-input v-model="queryParams.keyword" placeholder="请输入类型名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="handleAdd">新增类型</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="类型名称" width="200" />
      <el-table-column prop="description" label="类型描述" />
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{row}">
          {{ new Date(row.createTime).toLocaleString() }}
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
      :title="form.id ? '编辑维修类型' : '新增维修类型'"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="类型名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入类型名称" />
        </el-form-item>
        <el-form-item label="类型描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入类型描述" />
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
import { getRepairTypeList, getRepairTypeById, deleteRepairType, saveRepairType } from '@/api/repairType'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const formRef = ref(null)

const queryParams = reactive({
  keyword: ''
})

const form = ref({
  id: '',
  name: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入类型名称', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getRepairTypeList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...queryParams
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取维修类型列表失败:', error)
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
    id: '',
    name: '',
    description: ''
  }
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getRepairTypeById(row.id)
    if (res.code === 200) {
      form.value = { ...res.data }
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取维修类型详情失败:', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该维修类型吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteRepairType(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除维修类型失败:', error)
    }
  })
}

const handleSubmit = () => {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      try {
        const res = await saveRepairType(form.value)
        
        if (res.code === 200) {
          ElMessage.success(form.value.id ? '编辑成功' : '新增成功')
          dialogVisible.value = false
          getList()
        }
      } catch (error) {
        console.error('保存维修类型失败:', error)
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
</style> 