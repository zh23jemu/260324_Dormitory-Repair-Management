import request from '@/utils/request'

// 分页查询宿舍房间列表
export function getDormRoomList(params) {
    return request({
        url: '/dormRoom/list',
        method: 'get',
        params
    })
}

// 查询所有宿舍房间
export function getAllDormRooms() {
    return request({
        url: '/dormRoom/all',
        method: 'get'
    })
}

// 根据ID获取宿舍房间详情
export function getDormRoomById(id) {
    return request({
        url: `/dormRoom/getById/${id}`,
        method: 'get'
    })
}

// 保存宿舍房间信息
export function saveDormRoom(data) {
    return request({
        url: '/dormRoom/save',
        method: 'post',
        data
    })
}

// 更新宿舍房间信息
export function updateDormRoom(data) {
    return request({
        url: '/dormRoom/update',
        method: 'put',
        data
    })
}

// 删除宿舍房间
export function deleteDormRoom(id) {
    return request({
        url: `/dormRoom/delete/${id}`,
        method: 'delete'
    })
}
