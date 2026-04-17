import request from '@/utils/request'

// 分页查询宿舍楼列表
export function getDormBuildingList(params) {
    return request({
        url: '/dormBuilding/list',
        method: 'get',
        params
    })
}

// 查询所有宿舍楼
export function getAllDormBuildings() {
    return request({
        url: '/dormBuilding/all',
        method: 'get'
    })
}

// 根据ID获取宿舍楼详情
export function getDormBuildingById(id) {
    return request({
        url: `/dormBuilding/getById/${id}`,
        method: 'get'
    })
}

// 保存宿舍楼信息
export function saveDormBuilding(data) {
    return request({
        url: '/dormBuilding/save',
        method: 'post',
        data
    })
}

// 更新宿舍楼信息
export function updateDormBuilding(data) {
    return request({
        url: '/dormBuilding/update',
        method: 'put',
        data
    })
}

// 删除宿舍楼
export function deleteDormBuilding(id) {
    return request({
        url: `/dormBuilding/delete/${id}`,
        method: 'delete'
    })
}
