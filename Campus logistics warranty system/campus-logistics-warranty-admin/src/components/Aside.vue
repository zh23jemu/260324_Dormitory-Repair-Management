<template>
    <el-aside :width="width">
        <el-menu 
        background-color="#545c64"
        text-color="#fff"
        :collapse="isCollapse"
        :collapse-transition="false"
        :default-active="activeMenu"
        >
            <h3 v-show="!isCollapse">学校后勤维修系统</h3>
            <h3 v-show="isCollapse">后台</h3>
            <el-menu-item v-for="item in noChildren" :index="item.path" :key="item.path" @click="handleMenu(item)">
                <component class="icons" :is="item.icon"></component>
                <span>{{ item.label }}</span>
            </el-menu-item>
            <el-sub-menu v-for="item in hasChildren" :index="item.path" :key="item.path">
                <template #title>
                    <component class="icons" :is="item.icon"></component>
                    <span>{{ item.label }}</span>
                </template>
                <el-menu-item-group>
                    <el-menu-item 
                    v-for="subItem in item.children" 
                    :index="subItem.path" 
                    :key="subItem.path"
                    @click="handleMenu(subItem)">
                    <component class="icons" :is="subItem.icon">
                    </component>
                    <span>{{ subItem.label }}</span>
                    </el-menu-item>
                </el-menu-item-group>
            </el-sub-menu>
        </el-menu>
    </el-aside>
</template>
<script setup>
import { ref, computed } from 'vue'
import {useStore} from '@/store/index.js'
import { useUserStore } from '@/store/user.js'
import { useRouter,useRoute } from 'vue-router';
const router = useRouter()
const route = useRoute()
const store = useStore()
const userStore = useUserStore()

const activeMenu = computed(()=>{
    return route.path
})
const handleMenu = (item) => {
 router.push(item.path)
 store.addTags(item)
}

// 原始菜单列表
const allMenuList = ref([
    {
        path: '/home',
        name: 'home',
        label: '首页',
        icon: 'house',
        url: 'Home'
    },
    {
        path: '/system',
        name: 'system',
        label: '系统管理',
        icon: 'setting',
        children: [
            {
                path: '/user',
                name: 'user',
                label: '用户管理',
                icon: 'user',
                url: 'User'
            },
            {
                path: '/profile',
                name: 'profile',
                label: '个人中心',
                icon: 'user',
                url: 'Profile'
            },
            {
                path: '/resource',
                name: 'resource',
                label: '资源管理',
                icon: 'folder',
                url: 'Resource'
            }
        ]
    },
    {
        path: '/repair',
        name: 'repair',
        label: '维修管理',
        icon: 'tools',
        children: [
            {
                path: '/repairType',
                name: 'repairType',
                label: '维修类型',
                icon: 'menu',
                url: 'RepairType'
            },
            {
                path: '/repairRecord',
                name: 'repairRecord',
                label: '报修记录',
                icon: 'document',
                url: 'RepairRecord'
            },
            {
                path: '/repairRecordRating',
                name: 'repairRecordRating',
                label: '维修评价',
                icon: 'star',
                url: 'RepairRecordRating'
            },
            {
                path: '/dormBuilding',
                name: 'dormBuilding',
                label: '宿舍楼管理',
                icon: 'office-building',
                url: 'DormBuilding'
            },
            {
                path: '/dormRoom',
                name: 'dormRoom',
                label: '房间管理',
                icon: 'house',
                url: 'DormRoom'
            },
            {
                path: '/statistics',
                name: 'statistics',
                label: '数据统计',
                icon: 'data-analysis',
                url: 'Statistics'
            },
            {
                path: '/dormFacility',
                name: 'dormFacility',
                label: '宿舍物品管理',
                icon: 'box',
                url: 'DormFacility'
            }
        ]
    },
    {
        path: '/community',
        name: 'community',
        label: '社区管理',
        icon: 'chat-dot-round',
        children: [
            {
                path: '/post',
                name: 'post',
                label: '帖子管理',
                icon: 'document',
                url: 'Post'
            },
            {
                path: '/comment',
                name: 'comment',
                label: '评论管理',
                icon: 'chat-line-round',
                url: 'Comment'
            }
        ]
    },
    {
        path: '/message',
        name: 'message',
        label: '消息管理',
        icon: 'bell',
        children: [
            {
                path: '/notice',
                name: 'notice',
                label: '公告管理',
                icon: 'notification',
                url: 'Notice'
            },
            {
                path: '/messageManage',
                name: 'messageManage',
                label: '留言管理',
                icon: 'message',
                url: 'Message'
            }
        ]
    }
])

// 根据角色过滤菜单
const list = computed(() => {
    const role = userStore.userRole
    
    // 如果是管理员，显示所有菜单
    if (role === 'admin') {
        return allMenuList.value
    }
    
    // 如果是维修工，只显示特定菜单
    if (role === 'worker') {
        return allMenuList.value.filter(item => {
            // 首页不显示
            if (item.path === '/home') {
                return false
            }
            
            // 系统管理：只保留个人中心和资源管理
            if (item.path === '/system') {
                return {
                    ...item,
                    children: item.children.filter(child => 
                        child.path === '/profile' || child.path === '/resource'
                    )
                }
            }
            
            // 维修管理：保留维修类型、报修记录、维修评价、数据统计
            if (item.path === '/repair') {
                return {
                    ...item,
                    children: item.children.filter(child => 
                        child.path === '/repairType' || 
                        child.path === '/repairRecord' || 
                        child.path === '/repairRecordRating' ||
                        child.path === '/statistics'
                    )
                }
            }
            
            // 其他菜单不显示
            return false
        }).map(item => {
            // 处理有子菜单的项
            if (item.children) {
                return {
                    ...item,
                    children: item.children.filter(child => {
                        if (item.path === '/system') {
                            return child.path === '/profile' || child.path === '/resource'
                        }
                        if (item.path === '/repair') {
                            return child.path === '/repairType' || 
                                   child.path === '/repairRecord' || 
                                   child.path === '/repairRecordRating' ||
                                   child.path === '/statistics'
                        }
                        return true
                    })
                }
            }
            return item
        })
    }
    
    // 默认返回所有菜单
    return allMenuList.value
})

const noChildren = computed(() => {
    return list.value.filter(item => {
        return !item.children
    })
})
const hasChildren = computed(() => {
    return list.value.filter(item => {
        return item.children && item.children.length > 0
    })
})

const isCollapse = computed(()=>{
    return store.state.isCollapse
})  
const width = computed(()=>{
   return store.state.isCollapse ? "64px" : "180px"
})



</script>
<style lang="less" scoped>
.icons{
    width: 18px;
    height: 18px;
    margin-right: 5px;
}
.el-menu{
    border-right: none;
    h3{
        line-height: 48px;
        color: #fff;
        text-align: center;
        
    }
}
.el-aside{
    height: 100%;
    background-color: #545c64;
}


</style>