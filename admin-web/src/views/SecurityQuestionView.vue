<template>
  <div class="security-page">
    <el-card class="security-card">
      <template #header>
        <div>
          <div class="security-title">找回密码安全设置</div>
          <div class="security-subtitle">设置一个只有本人知道的问题和答案，忘记密码时可用于自助重置。</div>
        </div>
      </template>

      <el-alert
        type="info"
        :closable="false"
        title="安全答案不会在页面回显；每次保存都会用新答案覆盖旧答案。"
        style="margin-bottom: 18px"
      />

      <el-form :model="form" label-width="110px" class="security-form">
        <el-form-item label="当前状态">
          <el-tag :type="hasAnswer ? 'success' : 'warning'">{{ hasAnswer ? '已设置答案' : '尚未设置答案' }}</el-tag>
        </el-form-item>
        <el-form-item label="找回问题">
          <el-input v-model="form.question" placeholder="例如：你的学号后四位是什么？" />
        </el-form-item>
        <el-form-item label="找回答案">
          <el-input v-model="form.answer" placeholder="请输入新答案，保存后请牢记" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveSecurityQuestion">保存安全设置</el-button>
          <el-button @click="goBack">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuth } from '../utils/auth'

const router = useRouter()
const auth = useAuth()
const hasAnswer = ref(false)
const form = reactive({ question: '', answer: '' })

async function loadSecurityQuestion() {
  const data = await auth.securityQuestion()
  form.question = data.question || ''
  form.answer = ''
  hasAnswer.value = Boolean(data.hasAnswer)
}

async function saveSecurityQuestion() {
  if (!form.question.trim()) return ElMessage.warning('请输入找回密码问题')
  if (!form.answer.trim()) return ElMessage.warning('请输入找回密码答案')
  await auth.updateSecurityQuestion({ question: form.question.trim(), answer: form.answer.trim() })
  ElMessage.success('找回密码安全设置已保存')
  await loadSecurityQuestion()
}

function goBack() {
  router.push(auth.getRoleHomePath())
}

onMounted(loadSecurityQuestion)
</script>

<style scoped>
.security-page {
  max-width: 760px;
}

.security-card {
  border-radius: 16px;
}

.security-title {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
}

.security-subtitle {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

.security-form {
  max-width: 620px;
}
</style>
