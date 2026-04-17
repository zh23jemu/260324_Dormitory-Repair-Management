<template>
    <div class="top">
        <div class="left">
            <el-button size="small" @click="handleCollapse">
                <component class="icons" is="menu"/>
            </el-button>
            <el-breadcrumb separator="/" class="bread">
                <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="right">
            <el-dropdown @command="handleCommand">
                <span class="el-dropdown-link">
                    <img class="user" :src="userInfo ? userInfo.avatar : ''" alt="">
                    <span v-if="userInfo" class="username">{{ userInfo.username }}</span>
                </span>
                <template #dropdown>
                    <el-dropdown-menu>
                        <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                        <el-dropdown-item command="logout">退出</el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </div>
    </div>

   
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useStore } from '@/store';
import { useUserStore } from '@/store/user';
import { getUserByToken } from '@/api/user';
import { useRouter } from 'vue-router';

const store = useStore();
const userStore = useUserStore();
const router = useRouter();
const userInfo = ref(null);

const handleCollapse = () => {
    store.state.isCollapse = !store.state.isCollapse
}

// 获取用户信息
const fetchUserInfo = async () => {
    try {
        const res = await getUserByToken();
        if (res.code === 200) {
            console.log(res.data);
            
            userInfo.value = res.data;
        }
    } catch (error) {
        console.error('获取用户信息失败:', error);
    }
}

// 处理下拉菜单命令
const handleCommand = (command) => {
    if (command === 'profile') {
        // 跳转到个人中心页面
        router.push('/profile');
    } else if (command === 'logout') {
        ElMessageBox.confirm('确定要退出登录吗?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }).then(() => {
            // 清除token和角色信息
            store.removeToken();
            userStore.logout();
            // 提示用户
            ElMessage.success('退出登录成功');
            // 跳转到登录页
            router.push('/login');
        }).catch(() => {
            // 用户取消操作
        });
    }
}

// 组件挂载时获取用户信息
onMounted(() => {
    fetchUserInfo();
});
</script>


<style lang="less" scoped>
.top{
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    height: 100%;
    background-color: #333;
}
.icons{
    width: 20px;
    height: 20px;
}

.left{
    display: flex;
    align-items: center;
    .el-button{
        margin-right: 20px;
    }
}

.right{
    display: flex;
    align-items: center;
    
    .el-dropdown-link {
        display: flex;
        align-items: center;
        cursor: pointer;
    }
    
    .user{
        width: 40px;
        height: 40px;
        border-radius: 50%;
        margin-right: 8px;
    }
    
    .username {
        color: #fff;
        margin-left: 5px;
    }
}

:deep(.bread span){
    color: #fff !important;
    cursor: pointer !important;
}

.profile-info {
    padding: 10px;
}

:deep(.el-descriptions) {
    margin-bottom: 20px;
}

:deep(.el-descriptions__label) {
    width: 80px;
    font-weight: bold;
}
</style>