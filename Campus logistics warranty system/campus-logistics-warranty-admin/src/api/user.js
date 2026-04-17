import request from '@/utils/request'

// 用户登录
export function login(data) {
    return request({
        url: '/user/login',
        method: 'post',
        data
    })
}

// 用户注册
export function register(data) {
    return request({
        url: '/user/register',
        method: 'post',
        data
    })
}

// 删除用户
export function deleteUser(id) {
    return request({
        url: `/user/delete/${id}`,
        method: 'delete'
    })
}
export function getUserById(id) {
    return request({
        url: `/user/getById/${id}`,
        method: 'get'
    })
}

// 获取当前登录用户信息
export function getUserByToken() {
    return request({
        url: '/user/getByToken',
        method: 'get'
    })
}

// 获取用户列表
export function getUserList(params) {
    return request({
        url: '/user/list',
        method: 'get',
        params
    })
}

// 更新用户信息
export function updateStatus(params) {
    return request({
        url: '/user/updateStatus',
        method: 'get',
        params: {
            id: params.id,
            status: params.status
        }
    })
}

// 更新用户密码
export function updatePassword(data) {
    return request({
        url: '/user/updatePassword',
        method: 'put',
        data
    })
}