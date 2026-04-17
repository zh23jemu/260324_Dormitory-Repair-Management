import request from '@/utils/request'

// 用户登录
export function login(data) {
    return request({
        url: '/admin/login',
        method: 'post',
        data
    })
}