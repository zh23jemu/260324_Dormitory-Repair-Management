<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from '@/store'
import { login, register } from '@/api/user'
import { User, Lock, Phone, Message, UserFilled, InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useStore()

// 控制登录/注册表单的显示
const activeForm = ref('login')

// 登录表单
const loginForm = reactive({
  username: 'test',
  password: '123456'
})

// 注册表单
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: '',
  gender: '',
  role: 'user'
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ]
}

// 表单引用
const loginFormRef = ref(null)
const registerFormRef = ref(null)

// 加载状态
const loading = ref(false)

// 确认密码验证
function validateConfirmPassword(rule, value, callback) {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 提交登录
async function submitLogin() {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm)
        if (res.code === 200) {
          ElMessage.success('登录成功')
          userStore.setToken(res.data)
          
          // 获取用户信息
          await userStore.getUserInfo()
          // 检查是否有重定向路径
          const redirectPath = route.query.redirect || '/home'
          router.push(redirectPath)
        }else
        {
          ElMessage.error(res.msg||"登陆失败")
        }
      } catch (error) {
        console.error('登录失败', error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 提交注册
async function submitRegister() {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await register(registerForm)
        if (res.code === 200) {
          ElMessage.success('注册成功，请登录')
          activeForm.value = 'login'
          // 预填充登录表单
          loginForm.username = registerForm.username
          loginForm.password = registerForm.password
        }
      } catch (error) {
        console.error('注册失败', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<template>
  <div class="body-login">
    <div class="login-container">
      <div class="login-header">
        <div class="logo-icon">
          <el-icon :size="50"><UserFilled /></el-icon>
        </div>
        <h1>校园后勤维修系统</h1>
        <p class="subtitle">用户端 - 便捷报修，快速响应</p>
        <p class="description">随时随地提交维修申请，实时跟踪维修进度</p>
      </div>
      
      <div class="login-tabs">
        <div class="tab-buttons">
          <button 
            :class="['tab-btn', { active: activeForm === 'login' }]"
            @click="activeForm = 'login'"
          >
            登录
          </button>
          <button 
            :class="['tab-btn', { active: activeForm === 'register' }]"
            @click="activeForm = 'register'"
          >
            注册
          </button>
        </div>
        
        <!-- 登录表单 -->
        <div v-show="activeForm === 'login'" class="login-form">
          <el-form 
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="submitLogin"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="submitLogin"
                style="width: 100%"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 注册表单 -->
        <div v-show="activeForm === 'register'" class="login-form">
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
          >
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请确认密码"
                size="large"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            
            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="请输入邮箱"
                size="large"
                :prefix-icon="Message"
              />
            </el-form-item>
            
            <el-form-item prop="phone">
              <el-input
                v-model="registerForm.phone"
                placeholder="请输入手机号"
                size="large"
                :prefix-icon="Phone"
              />
            </el-form-item>
            
            <el-form-item prop="gender">
              <el-select
                v-model="registerForm.gender"
                placeholder="请选择性别"
                size="large"
                style="width: 100%"
              >
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="submitRegister"
                style="width: 100%"
              >
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <div class="login-footer">
        <div class="footer-tips">
          <el-icon><InfoFilled /></el-icon>
          <span>首次使用？点击注册创建账号，开始使用维修服务</span>
        </div>
        <el-text type="info" size="small">© 2025 校园后勤维修系统 | 让校园生活更美好</el-text>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.body-login {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
  
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(64, 158, 255, 0.05) 1px, transparent 1px);
    background-size: 50px 50px;
    animation: moveBackground 20s linear infinite;
  }
}

@keyframes moveBackground {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(50px, 50px);
  }
}

.login-container {
  width: 420px;
  background-color: #fff;
  border-radius: 20px;
  padding: 0;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  position: relative;
  z-index: 1;
  animation: slideIn 0.5s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  padding: 40px 30px 30px;
  text-align: center;
  color: #fff;
  
  .logo-icon {
    margin-bottom: 15px;
    
    .el-icon {
      color: #fff;
      filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
    }
  }
  
  h1 {
    margin: 0 0 8px 0;
    font-size: 26px;
    font-weight: 600;
    color: #fff;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  .subtitle {
    margin: 0 0 8px 0;
    font-size: 15px;
    opacity: 0.95;
    font-weight: 400;
  }
  
  .description {
    margin: 0;
    font-size: 13px;
    opacity: 0.85;
    font-weight: 300;
  }
}

.login-tabs {
  padding: 0;
}

.tab-buttons {
  display: flex;
  border-bottom: 1px solid #e4e7ed;
  
  .tab-btn {
    flex: 1;
    padding: 16px;
    background: none;
    border: none;
    font-size: 15px;
    color: #606266;
    cursor: pointer;
    transition: all 0.3s;
    position: relative;
    
    &:hover {
      color: #409eff;
    }
    
    &.active {
      color: #409eff;
      font-weight: 500;
      
      &::after {
        content: '';
        position: absolute;
        bottom: -1px;
        left: 0;
        right: 0;
        height: 2px;
        background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
      }
    }
  }
}

.login-form {
  padding: 35px 40px 20px;
  
  .el-form-item {
    margin-bottom: 24px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  :deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #e4e7ed inset;
    transition: all 0.3s;
    
    &:hover {
      box-shadow: 0 0 0 1px #c0c4cc inset;
    }
    
    &.is-focus {
      box-shadow: 0 0 0 1px #409eff inset;
    }
  }
  
  :deep(.el-button--primary) {
    background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
    border: none;
    height: 44px;
    font-size: 16px;
    font-weight: 500;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 16px rgba(64, 158, 255, 0.4);
    }
    
    &:active {
      transform: translateY(0);
    }
  }
}

.login-footer {
  padding: 20px;
  text-align: center;
  background-color: #f9fafb;
  border-top: 1px solid #e4e7ed;
  
  .footer-tips {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    margin-bottom: 12px;
    color: #909399;
    font-size: 13px;
    
    .el-icon {
      font-size: 14px;
    }
  }
}

:deep(.el-form-item__content) {
  justify-content: center;
}
</style> 