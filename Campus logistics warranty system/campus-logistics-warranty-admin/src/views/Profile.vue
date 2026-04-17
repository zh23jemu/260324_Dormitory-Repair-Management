<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>
      <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="80px">
        <el-form-item label="头像" class="avatar-uploader-container">
          <div class="avatar-wrapper">
            <el-avatar :size="100" :src="userForm.avatar || defaultAvatar" @error="handleAvatarError">
              <img src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png"/>
            </el-avatar>
            <div class="avatar-upload-btn">
              <el-upload
                class="avatar-uploader"
                action="#"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleAvatarChange">
                <el-button size="small" type="primary">更换头像</el-button>
              </el-upload>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="userForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="userForm.sex">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email"></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-input v-model="userForm.role" disabled></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitUserForm">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>修改密码</span>
        </div>
      </template>
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="passwordForm.password" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitPasswordForm">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserByToken, register, updatePassword } from '@/api/user'
import { uploadFile } from '@/api/file'


// 默认头像
const defaultAvatar = ref('https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png')

// 用户信息表单
const userFormRef = ref(null)
const userForm = reactive({
  id: '',
  username: '',
  name: '',
  phone: '',
  sex: '男',
  email: '',
  role: 'admin',
  avatar: ''
})

// 密码表单
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  password: '',
  confirmPassword: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入电话号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 密码表单验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 处理头像上传
const handleAvatarChange = async (file) => {
  if (file) {
    try {
      // 显示上传中提示
      const loadingMsg = ElMessage({
        message: '头像上传中...',
        type: 'info',
        duration: 0
      })
      
      // 上传文件
      const res = await uploadFile(file.raw)
      loadingMsg.close()
      
      if (res.code === 200) {
        // 更新头像URL
        userForm.avatar = res.data.replace(/\"/g, '')
        ElMessage.success('头像上传成功')
      } else {
        ElMessage.error(res.msg || '头像上传失败')
      }
    } catch (error) {
      console.error('头像上传失败:', error)
      ElMessage.error('头像上传失败')
    }
  }
}

// 处理头像加载错误
const handleAvatarError = () => {
  userForm.avatar = defaultAvatar.value
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const res = await getUserByToken()
    if (res.code === 200) {
      const { id, username, name, phone, sex, email, role, avatar } = res.data
      Object.assign(userForm, { id, username, name, phone, sex, email, role, avatar })
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 提交用户信息表单
const submitUserForm = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await register(userForm)
        if (res.code === 200) {
          ElMessage.success('个人信息修改成功')
          // 重新获取最新用户信息
          fetchUserInfo()
        } else {
          ElMessage.error(res.msg || '修改失败')
        }
      } catch (error) {
        console.error('修改个人信息失败:', error)
        ElMessage.error('修改个人信息失败')
      }
    }
  })
}

// 提交密码表单
const submitPasswordForm = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await updatePassword({
          id: userForm.id,
          oldPassword: passwordForm.oldPassword,
          password: passwordForm.password
        })
        if (res.code === 200) {
          ElMessage.success('密码修改成功')
          // 清空表单
          passwordForm.oldPassword = ''
          passwordForm.password = ''
          passwordForm.confirmPassword = ''
          passwordFormRef.value.resetFields()
        } else {
          ElMessage.error(res.msg || '修改密码失败')
        }
      } catch (error) {
        console.error('修改密码失败:', error)
        ElMessage.error('修改密码失败')
      }
    }
  })
}

// 组件挂载时获取用户信息
onMounted(() => {
  fetchUserInfo()
})
</script>

<style lang="less" scoped>
.profile-container {
  padding: 20px;
  
  .profile-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: bold;
    }
  }
  
  .avatar-uploader-container {
    .avatar-wrapper {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .el-avatar {
        margin-bottom: 10px;
        border: 1px solid #eee;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      }
      
      .avatar-upload-btn {
        margin-top: 10px;
      }
    }
  }
}
</style>