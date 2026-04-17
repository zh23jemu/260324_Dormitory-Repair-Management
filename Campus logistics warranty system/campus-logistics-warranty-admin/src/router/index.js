import {createRouter,createWebHistory} from "vue-router"




//制定路由规则

const routes = [
    {
        path:"/",
        name:"main",
        component:()=>import("@/views/Main.vue"), // 动态导入的方式  按需加载
        redirect:"/home",
        children:[
            {
                path:"home",
                name:"home",
                component:()=>import("@/views/Home.vue"),
            },
            {
                path:"/dormBuilding",
                name:"dormBuilding",
                component:()=>import("@/views/DormBuilding.vue"),
            },
            {
                path:"/dormRoom",
                name:"dormRoom",
                component:()=>import("@/views/DormRoom.vue"),
            },
            {
                path:"/user",
                name:"user",
                component:()=>import("@/views/User.vue"),
            },
            {
                path:"/profile",
                name:"profile",
                component:()=>import("@/views/Profile.vue"),
            },
            {
                path:"/repairType",
                name:"repairType",
                component:()=>import("@/views/RepairType.vue"),
            },
            {
                path:"/repairRecord",
                name:"repairRecord",
                component:()=>import("@/views/RepairRecord.vue"),
            },
            {
                path:"/repairRecordRating",
                name:"repairRecordRating",
                component:()=>import("@/views/RepairRecordRating.vue"),
            },
            {
                path:"/resource",
                name:"resource",
                component:()=>import("@/views/Resource.vue"),
            },
            {
                path:"/post",
                name:"post",
                component:()=>import("@/views/Post.vue"),
            },
            {
                path:"/comment",
                name:"comment",
                component:()=>import("@/views/Comment.vue"),
            },
            {
                path:"/notice",
                name:"notice",
                component:()=>import("@/views/Notice.vue"),
            },
            {
                path:"/messageManage",
                name:"messageManage",
                component:()=>import("@/views/Message.vue"),
            },
            {
                path:"/statistics",
                name:"statistics",
                component:()=>import("@/views/Statistics.vue"),
            },
            {
                path:"/dormFacility",
                name:"dormFacility",
                component:()=>import("@/views/DormFacility.vue"),
            }
        ]
    },
    {
        path:"/login",
        name:"login",
        component:()=>import("@/views/Login.vue"),
    }
    
]

//创建路由对象

const router = createRouter({
    history:createWebHistory(),
    routes
})

// 维修工可访问的路由
const workerAllowedRoutes = [
    '/profile',
    '/resource',
    '/repairType',
    '/repairRecord',
    '/repairRecordRating',
    '/statistics'
]

// 全局前置守卫
router.beforeEach((to, from, next) => {
    // 获取token和角色
    const token = localStorage.getItem('token')
    const userRole = localStorage.getItem('userRole')
    
    // 如果进入登录页，直接放行
    if (to.path === '/login') {
        next()
        return
    }
    
    // 如果没有token，跳转到登录页
    if (!token) {
        next('/login')
        return
    }
    
    // 如果是维修工，检查权限
    if (userRole === 'worker') {
        // 如果访问首页，重定向到报修记录
        if (to.path === '/' || to.path === '/home') {
            next('/repairRecord')
            return
        }
        
        // 检查是否有权限访问该路由
        if (!workerAllowedRoutes.includes(to.path)) {
            ElMessage.warning('您没有权限访问该页面')
            next('/repairRecord')
            return
        }
    }
    
    // 有token且权限检查通过，正常放行
    next()
})

//导出路由对象

export default router