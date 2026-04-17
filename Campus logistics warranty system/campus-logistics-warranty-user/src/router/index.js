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
                meta: { requiresAuth: false }
            },
            {
                path:"/announcement",
                name:"announcement",
                component:()=>import("@/views/Announcement.vue"),
                meta: { requiresAuth: false }
            },
            {
                path:"/message",
                name:"message",
                component:()=>import("@/views/Message.vue"),
                meta: { requiresAuth: true }
            },
            {
                path:"/forum",
                name:"forum",
                component:()=>import("@/views/Forum.vue"),
                meta: { requiresAuth: true }
            },
            {
                path:"/post/detail/:id",
                name:"postDetail",
                component:()=>import("@/views/PostDetail.vue"),
                props: true,
                meta: { requiresAuth: true }
            },
            {
                path:"/profile",
                name:"profile",
                component:()=>import("@/views/Profile.vue"),
                meta: { requiresAuth: true }
            },
            {
                path:"/repair/create",
                name:"repairCreate",
                component:()=>import("@/views/repair/Create.vue"),
                meta: { requiresAuth: true }
            },
            {
                path:"/repair/list",
                name:"repairList",
                component:()=>import("@/views/repair/List.vue"),
                meta: { requiresAuth: true }
            },
            {
                path:"/repair/detail/:id",
                name:"repairDetail",
                component:()=>import("@/views/repair/Detail.vue"),
                props: true,
                meta: { requiresAuth: true }
            },
            {
                path:"/worker/list",
                name:"workerList",
                component:()=>import("@/views/worker/List.vue"),
                meta: { requiresAuth: true }
            },
            {
                path:"/worker/detail/:id",
                name:"workerDetail",
                component:()=>import("@/views/worker/Detail.vue"),
                props: true,
                meta: { requiresAuth: true }
            },
            {
                path:"/resource/list",
                name:"resourceList",
                component:()=>import("@/views/resource/List.vue"),
                meta: { requiresAuth: false }
            },
            {
                path:"/resource/detail/:id",
                name:"resourceDetail",
                component:()=>import("@/views/resource/Detail.vue"),
                props: true,
                meta: { requiresAuth: false }
            }
        ]
    },
    {
        path:"/login",
        name:"login",
        component:()=>import("@/views/Login.vue"),
        meta: { requiresAuth: false }
    }
    
]

//创建路由对象

const router = createRouter({
    history:createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    const requiresAuth = to.meta.requiresAuth !== false // 默认需要鉴权
    
    if (requiresAuth && !token) {
        next({ 
            path: '/login', 
            query: { redirect: to.fullPath } // 将要访问的页面路径作为参数传递
        })
    } else {
        next()
    }
})

//导出路由对象

export default router