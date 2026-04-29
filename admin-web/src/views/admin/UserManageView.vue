<template>
  <el-card>
    <template #header>
      <div class="toolbar" style="margin-bottom:0">
        <span>用户管理</span>
        <el-button type="primary" @click="openCreateDialog">新增用户</el-button>
      </div>
    </template>
    <el-table :data="users" style="margin-top:12px">
      <el-table-column prop="username" label="账号" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column label="角色">
        <template #default="{ row }">{{ roleText(row.role) }}</template>
      </el-table-column>
      <el-table-column prop="workTypeName" label="工种" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="passwordQuestion" label="找回问题" min-width="160" show-overflow-tooltip />
      <el-table-column prop="passwordAnswer" label="找回答案" min-width="120" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ statusText(row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" type="primary" plain @click="editUser(row)">编辑</el-button>
            <el-button size="small" type="warning" plain @click="resetPassword(row)">重置密码</el-button>
            <el-button size="small" type="danger" plain @click="removeUser(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑用户' : '新增用户'"
      width="720px"
      append-to-body
      destroy-on-close
      class="user-manage-dialog"
    >
      <el-form :model="form" label-width="110px" class="user-dialog-form">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入账号" :disabled="Boolean(form.id)" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width:100%">
            <el-option label="学生" value="student" />
            <el-option label="宿管" value="dorm_admin" />
            <el-option label="维修" value="repairer" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.role === 'repairer'" label="维修工种">
          <el-select v-model="form.workTypeCodes" multiple collapse-tags collapse-tags-tooltip style="width:100%" placeholder="可选择多个维修工种">
            <el-option v-for="item in workTypes" :key="item.dictCode" :label="item.dictName" :value="item.dictCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="启用" value="enabled" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.role !== 'admin'" label="找回问题">
          <el-input v-model="form.passwordQuestion" placeholder="请输入找回密码问题" />
        </el-form-item>
        <el-form-item v-if="form.role !== 'admin'" label="找回答案">
          <el-input v-model="form.passwordAnswer" :placeholder="form.id ? '新答案，留空表示不修改' : '请输入找回密码答案'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="saveUser">{{ form.id ? '保存修改' : '新增用户' }}</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const users = ref([])
const workTypes = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, username: '', realName: '', role: 'repairer', workTypeCodes: [], phone: '', status: 'enabled', passwordQuestion: '', passwordAnswer: '' })

async function loadAll() {
  users.value = (await api.get('/admin/users')).data.data
  workTypes.value = (await api.get('/admin/dicts')).data.data.filter((item) => item.dictType === 'repair_work_type')
}

function resetForm() {
  form.id = null
  form.username = ''
  form.realName = ''
  form.role = 'repairer'
  form.workTypeCodes = []
  form.phone = ''
  form.status = 'enabled'
  form.passwordQuestion = ''
  form.passwordAnswer = ''
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function closeDialog() {
  dialogVisible.value = false
  resetForm()
}

function editUser(row) {
  // 弹窗表单复用为编辑表单，便于管理员一次性修改用户的基础资料、角色、状态和维修工种。
  form.id = row.id
  form.username = row.username || ''
  form.realName = row.realName || ''
  form.role = row.role || 'repairer'
  form.workTypeCodes = row.workTypeCode ? String(row.workTypeCode).split(',').filter(Boolean) : []
  form.phone = row.phone || ''
  form.status = row.status || 'enabled'
  form.passwordQuestion = row.passwordQuestion || ''
  form.passwordAnswer = ''
  dialogVisible.value = true
}

function buildPayload() {
  return {
    username: form.username.trim(),
    realName: form.realName.trim(),
    phone: form.phone,
    role: form.role,
    workTypeCode: form.role === 'repairer' ? form.workTypeCodes.join(',') : '',
    passwordQuestion: form.role !== 'admin' ? form.passwordQuestion.trim() : '',
    passwordAnswer: form.role !== 'admin' ? form.passwordAnswer.trim() : '',
  }
}

async function saveUser() {
  if (!form.username.trim()) return ElMessage.warning('请输入账号')
  if (!form.realName.trim()) return ElMessage.warning('请输入姓名')
  if (form.role === 'repairer' && form.workTypeCodes.length === 0) return ElMessage.warning('请选择维修工种')
  if (form.role !== 'admin' && !form.passwordQuestion.trim()) return ElMessage.warning('请设置找回密码问题')
  if (!form.id && form.role !== 'admin' && !form.passwordAnswer.trim()) return ElMessage.warning('请设置找回密码答案')

  const payload = buildPayload()
  if (form.id) {
    await api.put(`/admin/users/${form.id}`, payload)
    await api.put(`/admin/users/${form.id}/status`, { status: form.status })
    ElMessage.success('用户信息已更新')
  } else {
    await api.post('/admin/users', { ...payload, password: '123456' })
    ElMessage.success('用户已创建，默认密码 123456')
  }
  closeDialog()
  await loadAll()
}

async function removeUser(row) {
  await ElMessageBox.confirm(`确认删除用户“${row.username}”吗？如果该用户已有工单、评价、公告、留言等业务数据，系统会阻止删除。`, '提示', { type: 'warning' })
  await api.delete(`/admin/users/${row.id}`)
  ElMessage.success('用户已删除')
  if (form.id === row.id) resetForm()
  await loadAll()
}

async function resetPassword(row) {
  const { value } = await ElMessageBox.prompt(`请输入用户“${row.username}”的新密码`, '重置密码', {
    confirmButtonText: '确认重置',
    cancelButtonText: '取消',
    inputType: 'password',
    inputPattern: /\S+/,
    inputErrorMessage: '新密码不能为空',
  })
  await api.put(`/auth/reset-password/${row.id}`, { newPassword: value })
  ElMessage.success('密码已重置')
}

function roleText(role) {
  return { admin: '系统管理员', dorm_admin: '宿舍管理员', repairer: '维修人员', student: '学生' }[role] || role || '-'
}

function statusText(status) {
  return { enabled: '启用', disabled: '禁用' }[status] || status || '-'
}

onMounted(loadAll)
</script>
