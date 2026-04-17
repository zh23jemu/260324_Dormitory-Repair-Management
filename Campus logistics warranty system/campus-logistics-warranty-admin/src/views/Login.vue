<script setup>
import { reactive } from 'vue';
import { useRouter } from 'vue-router';
import { login as adminLogin } from '@/api/user';
import { useStore } from '@/store';
import { useUserStore } from '@/store/user';
import { User, Lock, UserFilled, Tools, InfoFilled } from '@element-plus/icons-vue';

const router = useRouter();
const store = useStore();
const userStore = useUserStore();

const loginForm = reactive({
    username: 'admin',
    password: '123456',
    role: 'admin'  // 默认角色为管理员
});

const roleOptions = [
    { label: '管理员', value: 'admin' },
    { label: '维修工', value: 'worker' }
];

const login = async () => {
    try {
        // 调用管理员登录API
        const res = await adminLogin(loginForm);
        console.log(res);
        
        if (res.data && res.code === 200) {
            // 登录成功，保存token和角色
            store.setToken(res.data);
            userStore.setToken(res.data);
            userStore.setUserRole(loginForm.role);
            
            ElMessage({
                message: res.msg || '登录成功',
                type: 'success'
            });
            // 跳转到首页
            router.push({ path: '/' });
        } else {
            // 登录失败
            ElMessage({
                message: res.msg || '登录失败，请检查用户名和密码',
                type: 'error'
            });
        }
    } catch (error) {
        console.error('登录错误:', error);
        ElMessage({
            message: '登录失败，请稍后再试',
            type: 'error'
        });
    }
}
</script>


<template>
    <div class="body-login">
        <div class="login-container">
            <div class="login-header">
                <div class="logo-icon">
                    <el-icon :size="50"><Tools /></el-icon>
                </div>
                <h1>校园后勤维修系统</h1>
                <p class="subtitle">管理端 - 高效管理，智能调度</p>
                <p class="description">统一管理维修工单，优化资源配置，提升服务质量</p>
            </div>
            
            <el-form :model="loginForm" class="login-form" @keyup.enter="login">
                <el-form-item>
                    <el-input 
                        v-model="loginForm.username" 
                        placeholder="请输入用户名"
                        size="large"
                        :prefix-icon="User"
                    />
                </el-form-item>
                
                <el-form-item>
                    <el-input 
                        v-model="loginForm.password" 
                        type="password" 
                        placeholder="请输入密码"
                        size="large"
                        :prefix-icon="Lock"
                        show-password
                    />
                </el-form-item>
                
                <el-form-item>
                    <el-select 
                        v-model="loginForm.role" 
                        placeholder="请选择角色" 
                        size="large"
                        :prefix-icon="UserFilled"
                        style="width: 100%"
                    >
                        <el-option
                            v-for="item in roleOptions"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                        />
                    </el-select>
                </el-form-item>
                
                <el-form-item>
                    <el-button 
                        type="primary" 
                        @click="login" 
                        size="large"
                        style="width: 100%"
                    >
                        登录
                    </el-button>
                </el-form-item>
            </el-form>
            
            <div class="login-footer">
                <div class="footer-tips">
                    <el-icon><InfoFilled /></el-icon>
                    <span>请使用管理员或维修工账号登录系统</span>
                </div>
                <el-text type="info" size="small">© 2025 校园后勤维修系统 | 专业维修管理平台</el-text>
            </div>
        </div>
    </div>
</template>

<script>
</script>

<style scoped lang="less">
.body-login {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
    overflow: hidden;
    position: relative;
    
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