import request from '@/utils/request'

// 获取维修类型列表
export function getRepairTypeList(params) {
    return request({
        url: '/repairType/list',
        method: 'get',
        params
    })
}

// 根据ID获取维修类型详情
export function getRepairTypeById(id) {
    return request({
        url: `/repairType/getById/${id}`,
        method: 'get'
    })
}

// 删除维修类型
export function deleteRepairType(id) {
    return request({
        url: `/repairType/deleteById/${id}`,
        method: 'get'
    })
}

// 保存或更新维修类型
export function saveRepairType(data) {
    return request({
        url: '/repairType/save',
        method: 'post',
        data
    })
} 