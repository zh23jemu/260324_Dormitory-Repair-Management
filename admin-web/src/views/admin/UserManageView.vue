<template>
  <el-card>
    <template #header>用户管理</template>
    <el-form :model="form" inline>
      <el-form-item><el-input v-model="form.username" placeholder="账号" :disabled="Boolean(form.id)" /></el-form-item>
      <el-form-item><el-input v-model="form.realName" placeholder="姓名" /></el-form-item>
      <el-form-item><el-input v-model="form.phone" placeholder="手机号" /></el-form-item>
      <el-form-item>
        <el-select v-model="form.role" style="width:140px">
          <el-option label="学生" value="student" />
          <el-option label="宿管" value="dorm_admin" />
          <el-option label="维修" value="repairer" />
          <el-option label="管理员" value="admin" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.role === 'repairer'">
        <el-select v-model="form.workTypeCodes" multiple collapse-tags collapse-tags-tooltip style="width:220px" placeholder="维修工种">
          <el-option v-for="item in workTypes" :key="item.dictCode" :label="item.dictName" :value="item.dictCode" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-select v-model="form.status" style="width:120px">
          <el-option label="启用" value="enabled" />
          <el-option label="禁用" value="disabled" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveUser">{{ form.id ? '保存修改' : '新增用户' }}</el-button>
        <el-button v-if="form.id" @click="resetForm">取消编辑</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="users" style="margin-top:12px">
      <el-table-column prop="username" label="账号" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column label="角色">
        <template #default="{ row }">{{ roleText(row.role) }}</template>
      </el-table-column>
      <el-table-column prop="workTypeName" label="工种" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ statusText(row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" type="primary" plain @click="editUser(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="removeUser(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const users = ref([])
const workTypes = ref([])
const form = reactive({ id: null, username: '', realName: '', role: 'repairer', workTypeCodes: [], phone: '', status: 'enabled' })

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
}

function editUser(row) {
  // 顶部表单复用为编辑表单，便于管理员一次性修改用户的基础资料、角色、状态和维修工种。
  form.id = row.id
  form.username = row.username || ''
  form.realName = row.realName || ''
  form.role = row.role || 'repairer'
  form.workTypeCodes = row.workTypeCode ? String(row.workTypeCode).split(',').filter(Boolean) : []
  form.phone = row.phone || ''
  form.status = row.status || 'enabled'
}

function buildPayload() {
  return {
    username: form.username.trim(),
    realName: form.realName.trim(),
    phone: form.phone,
    role: form.role,
    workTypeCode: form.role === 'repairer' ? form.workTypeCodes.join(',') : '',
  }
}

async function saveUser() {
  if (!form.username.trim()) return ElMessage.warning('请输入账号')
  if (!form.realName.trim()) return ElMessage.warning('请输入姓名')
  if (form.role === 'repairer' && form.workTypeCodes.length === 0) return ElMessage.warning('请选择维修工种')

  const payload = buildPayload()
  if (form.id) {
    await api.put(`/admin/users/${form.id}`, payload)
    await api.put(`/admin/users/${form.id}/status`, { status: form.status })
    ElMessage.success('用户信息已更新')
  } else {
    await api.post('/admin/users', { ...payload, password: '123456' })
    ElMessage.success('用户已创建，默认密码 123456')
  }
  resetForm()
  await loadAll()
}

async function removeUser(row) {
  await ElMessageBox.confirm(`确认删除用户“${row.username}”吗？如果该用户已有工单、评价、公告、留言等业务数据，系统会阻止删除。`, '提示', { type: 'warning' })
  await api.delete(`/admin/users/${row.id}`)
  ElMessage.success('用户已删除')
  if (form.id === row.id) resetForm()
  await loadAll()
}

function roleText(role) {
  return { admin: '系统管理员', dorm_admin: '宿舍管理员', repairer: '维修人员', student: '学生' }[role] || role || '-'
}

function statusText(status) {
  return { enabled: '启用', disabled: '禁用' }[status] || status || '-'
}

onMounted(loadAll)
</script>
