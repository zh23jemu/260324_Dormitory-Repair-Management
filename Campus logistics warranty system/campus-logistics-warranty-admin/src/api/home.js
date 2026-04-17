import request from '@/utils/request'

// 获取首页统计数据
export function getCountData() {
    return request({
        url: '/home/getCountData',
        method: 'get'
    })
}

// 获取维修类型占比数据
export function getProportion() {
    return request({
        url: '/home/getProportion',
        method: 'get'
    })
} 