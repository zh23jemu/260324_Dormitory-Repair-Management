import request from '@/utils/request'

// 获取报修记录列表
export function getRepairRecordList(params) {
    return request({
        url: '/repairRecord/list',
        method: 'get',
        params
    })
}

// 根据ID获取报修记录详情
export function getRepairRecordById(id) {
    return request({
        url: `/repairRecord/getById/${id}`,
        method: 'get'
    })
}

// 删除报修记录
export function deleteRepairRecord(id) {
    return request({
        url: `/repairRecord/deleteById/${id}`,
        method: 'get'
    })
}

// 保存或更新报修记录
export function saveRepairRecord(data) {
    return request({
        url: '/repairRecord/save',
        method: 'post',
        data
    })
} 