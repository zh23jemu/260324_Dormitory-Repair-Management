import request from '@/utils/request'

// 查询所有宿舍房间
export function getAllDormRooms() {
    return request({
        url: '/dormRoom/all',
        method: 'get'
    })
}
