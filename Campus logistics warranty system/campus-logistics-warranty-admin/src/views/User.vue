<template>
  <div class="user-manage">
    <div class="search-form">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="姓名">
          <el-input v-model="queryParams.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 150px;">
            <el-option label="正常" :value="0" />
            <el-option label="拉黑" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="handleAdd">新增用户</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column prop="sex" label="性别" width="80" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{row}">
          <el-tag :type="row.role === 'admin' ? 'danger' : row.role === 'worker' ? 'warning' : 'success'">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-switch
            v-model="row.status"
            :active-value="0"
            :inactive-value="1"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="avatar" label="头像" width="100">
        <template #default="{row}">
          <el-avatar :src="row.avatar || '/src/assets/images/user-default.png'" />
        </template>
      </el-table-column>
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
      :title="form.id ? '编辑用户' : '新增用户'"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="form.sex">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色">
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="user" />
            <el-option label="工作人员" value="worker" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">正常</el-radio>
            <el-radio :label="1">拉黑</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标签" prop="tags" v-if="form.role === 'worker'">
          <el-input 
            v-model="form.tags" 
            placeholder="请输入标签，多个标签用英文逗号分隔，如：水电维修,网络维护"
            type="textarea"
            :rows="2"
          />
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            多个标签请用英文逗号(,)分隔
          </div>
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <div class="avatar-upload-wrapper">
            <el-upload
              class="avatar-uploader"
              action=""
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleAvatarChange"
            >
              <div v-if="form.avatar" class="avatar-preview">
                <img :src="form.avatar" class="avatar-image" />
                <div class="avatar-edit-overlay">
                  <el-icon><EditPen /></el-icon>
                </div>
              </div>
              <div v-else class="avatar-empty">
                <el-icon class="avatar-icon"><Plus /></el-icon>
                <div class="avatar-text">点击上传头像</div>
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
import { getUserList, register, deleteUser, getUserById, updateStatus } from '@/api/user'
import { uploadFile } from '@/api/file'
import { Plus, EditPen } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const formRef = ref(null)
const avatarFile = ref(null)

const queryParams = reactive({
  name: '',
  status: ''
})

const form = ref({
  id: '',
  username: '',
  name: '',
  password: '',
  phone: '',
  sex: '男',
  email: '',
  role: 'user',
  status: 0,
  avatar: '',
  tags: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...queryParams
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pageNum.value = 1
  getList()
}

const resetQuery = () => {
  queryParams.name = ''
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
    id: '',
    username: '',
    name: '',
    password: '',
    phone: '',
    sex: '男',
    email: '',
    role: 'user',
    status: 0,
    avatar: '',
    tags: ''
  }
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getUserById(row.id)
    if (res.code === 200) {
      form.value = { ...res.data }
      delete form.value.password
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该用户吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteUser(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error('删除用户失败:', error)
    }
  })
}

const handleStatusChange = async (row) => {
  try {
    const res = await updateStatus({
      id: row.id,
      status: row.status
    })
    if (res.code === 200) {
      ElMessage.success(`已${row.status === 0 ? '启用' : '禁用'}该用户`)
    } else {
      // 如果失败，恢复原状态
      row.status = row.status === 0 ? 1 : 0
    }
  } catch (error) {
    console.error('更新用户状态失败:', error)
    // 如果失败，恢复原状态
    row.status = row.status === 0 ? 1 : 0
  }
}

const handleAvatarChange = (file) => {
  avatarFile.value = file.raw
  // 预览
  const reader = new FileReader()
  reader.onload = (e) => {
    form.value.avatar = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const handleSubmit = () => {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      try {
        // 上传头像
        if (avatarFile.value) {
          const uploadRes = await uploadFile(avatarFile.value)
          if (uploadRes.code === 200) {
            form.value.avatar = uploadRes.data
          } else {
            ElMessage.error('头像上传失败')
            return
          }
        }
        
        // 无论新增还是编辑都使用register方法
        const res = await register(form.value)
        
        if (res.code === 200) {
          ElMessage.success(form.value.id ? '编辑成功' : '新增成功')
          dialogVisible.value = false
          getList()
        }
      } catch (error) {
        console.error('保存用户失败:', error)
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

.avatar-upload-wrapper {
  width: 150px;
  height: 150px;
}

.avatar-uploader {
  width: 100%;
  height: 100%;
}

.avatar-empty {
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

.avatar-empty:hover {
  border-color: #409EFF;
  background-color: #f0f7ff;
}

.avatar-icon {
  font-size: 28px;
  color: #8c939d;
  margin-bottom: 8px;
}

.avatar-text {
  font-size: 14px;
  color: #8c939d;
}

.avatar-preview {
  width: 150px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  border: 1px solid #e0e0e0;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-edit-overlay {
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

.avatar-preview:hover .avatar-edit-overlay {
  opacity: 1;
}

/* 移除之前的头像样式 */
.avatar-container, 
.avatar-placeholder, 
.avatar-hover-mask,
.avatar-uploader-icon, 
.upload-tip,
.avatar {
  display: none;
}
</style>