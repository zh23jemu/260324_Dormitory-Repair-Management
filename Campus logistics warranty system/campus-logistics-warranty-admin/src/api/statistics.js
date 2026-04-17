import request from '@/utils/request'

// 获取维修类型统计信息
export function getRepairTypeStatistics() {
    return request({
        url: '/statistics/repairTypeStatistics',
        method: 'get'
    })
}

// 获取维修地点统计信息
export function getRepairLocationStatistics() {
    return request({
        url: '/statistics/repairLocationStatistics',
        method: 'get'
    })
}

// 根据时间范围获取维修类型统计信息
export function getRepairTypeStatisticsByTime(params) {
    return request({
        url: '/statistics/repairTypeStatisticsByTime',
        method: 'get',
        params
    })
}

// 根据时间范围获取维修地点统计信息
export function getRepairLocationStatisticsByTime(params) {
    return request({
        url: '/statistics/repairLocationStatisticsByTime',
        method: 'get',
        params
    })
}

// 获取总报修记录数量
export function getTotalRepairCount() {
    return request({
        url: '/statistics/totalRepairCount',
        method: 'get'
    })
}

// 获取报修状态统计信息
export function getRepairStatusStatistics() {
    return request({
        url: '/statistics/repairStatusStatistics',
        method: 'get'
    })
}
