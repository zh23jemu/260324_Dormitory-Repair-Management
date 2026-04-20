<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <template #header>
        <div>
          <div class="login-title">高校宿舍报修管理系统</div>
          <div class="login-subtitle">宿管、维修人员、系统管理员统一入口</div>
        </div>
      </template>
      <el-form :model="form" label-position="top" @submit.prevent>
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="admin / dorm01 / repair01" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="123456" />
        </el-form-item>
        <el-button type="primary" class="full-width" @click="submit">登录系统</el-button>
      </el-form>
      <div class="login-hints">
        <span>管理员：admin / 123456</span>
        <span>宿管：dorm01 / 123456</span>
        <span>维修：repair01 / 123456</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAdminAuth } from '../utils/auth'

const router = useRouter()
const route = useRoute()
const auth = useAdminAuth()
const form = reactive({ username: 'admin', password: '123456' })

async function submit() {
  await auth.login(form)
  router.replace(String(route.query.redirect || '/dashboard'))
}
</script>
