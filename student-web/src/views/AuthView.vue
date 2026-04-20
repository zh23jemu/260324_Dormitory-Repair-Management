<template>
  <div class="auth-shell">
    <div class="auth-hero">
      <div class="auth-title">高校宿舍报修管理系统</div>
      <div class="auth-subtitle">移动端学生入口，支持报修、查单、评价、查看维修工与知识库</div>
    </div>
    <div class="section">
      <van-tabs v-model:active="activeTab" animated>
        <van-tab title="登录">
          <van-field v-model="loginForm.username" label="账号" placeholder="student01" />
          <van-field v-model="loginForm.password" label="密码" type="password" placeholder="123456" />
          <van-button type="primary" block @click="submitLogin">登录系统</van-button>
        </van-tab>
        <van-tab title="注册">
          <van-field v-model="registerForm.username" label="账号" />
          <van-field v-model="registerForm.password" label="密码" type="password" />
          <van-field v-model="registerForm.realName" label="姓名" />
          <van-field v-model="registerForm.phone" label="手机号" />
          <van-field v-model="registerForm.studentNo" label="学号" />
          <van-field v-model="registerForm.college" label="学院" />
          <van-field v-model="registerForm.major" label="专业" />
          <van-field v-model="registerForm.className" label="班级" />
          <van-button type="success" block @click="submitRegister">注册账号</van-button>
        </van-tab>
        <van-tab title="找回密码">
          <van-field v-model="forgotForm.username" label="账号" />
          <van-field v-model="forgotForm.studentNo" label="学号" />
          <van-field v-model="forgotForm.phone" label="手机号" />
          <van-field v-model="forgotForm.newPassword" label="新密码" type="password" />
          <van-button type="warning" block @click="submitForgot">重置密码</van-button>
        </van-tab>
      </van-tabs>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { useStudentAuth } from '../utils/auth'

const router = useRouter()
const route = useRoute()
const auth = useStudentAuth()
const activeTab = ref(0)
const loginForm = reactive({ username: 'student01', password: '123456' })
const registerForm = reactive({ username: '', password: '', realName: '', phone: '', studentNo: '', college: '', major: '', className: '' })
const forgotForm = reactive({ username: '', studentNo: '', phone: '', newPassword: '' })

async function submitLogin() {
  await auth.login(loginForm)
  router.replace(String(route.query.redirect || '/home'))
}

async function submitRegister() {
  await auth.register(registerForm)
  showToast('注册成功，请登录')
  activeTab.value = 0
}

async function submitForgot() {
  await auth.forgotPassword(forgotForm)
  showToast('密码已重置，请使用新密码登录')
  activeTab.value = 0
}
</script>
