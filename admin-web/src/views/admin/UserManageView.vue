<template>
  <el-card>
    <template #header>用户管理</template>
    <el-form :model="form" inline>
      <el-form-item><el-input v-model="form.username" placeholder="账号" /></el-form-item>
      <el-form-item><el-input v-model="form.realName" placeholder="姓名" /></el-form-item>
      <el-form-item>
        <el-select v-model="form.role" style="width:140px">
          <el-option label="宿管" value="dorm_admin" />
          <el-option label="维修" value="repairer" />
          <el-option label="管理员" value="admin" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.role === 'repairer'">
        <el-select v-model="form.workTypeCode" style="width:160px" placeholder="维修工种">
          <el-option v-for="item in workTypes" :key="item.dictCode" :label="item.dictName" :value="item.dictCode" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" @click="createUser">新增用户</el-button></el-form-item>
    </el-form>
    <el-table :data="users" style="margin-top:12px">
      <el-table-column prop="username" label="账号" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="role" label="角色" />
      <el-table-column prop="workTypeName" label="工种" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="status" label="状态" width="100" />
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'

const users = ref([])
const workTypes = ref([])
const form = reactive({ username: '', realName: '', role: 'repairer', workTypeCode: '', phone: '13800000000' })

async function loadAll() {
  users.value = (await api.get('/admin/users')).data.data
  workTypes.value = (await api.get('/admin/dicts')).data.data.filter((item) => item.dictType === 'repair_work_type')
}

async function createUser() {
  await api.post('/admin/users', { ...form, password: '123456' })
  ElMessage.success('用户已创建，默认密码 123456')
  form.username = ''
  form.realName = ''
  form.workTypeCode = ''
  await loadAll()
}

onMounted(loadAll)
</script>
