import request from '@/utils/request'

// 分页查询宿舍设施列表
export function getDormFacilityList(params) {
    return request({
        url: '/dormFacility/list',
        method: 'get',
        params
    })
}

// 查询所有宿舍设施
export function getAllDormFacilities() {
    return request({
        url: '/dormFacility/all',
        method: 'get'
    })
}

// 根据ID获取宿舍设施详情
export function getDormFacilityById(id) {
    return request({
        url: `/dormFacility/getById/${id}`,
        method: 'get'
    })
}

// 保存宿舍设施信息
export function saveDormFacility(data) {
    return request({
        url: '/dormFacility/save',
        method: 'post',
        data
    })
}

// 更新宿舍设施信息
export function updateDormFacility(data) {
    return request({
        url: '/dormFacility/update',
        method: 'put',
        data
    })
}

// 删除宿舍设施
export function deleteDormFacility(id) {
    return request({
        url: `/dormFacility/delete/${id}`,
        method: 'delete'
    })
}
