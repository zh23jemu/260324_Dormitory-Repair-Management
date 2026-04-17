import request from '@/utils/request'

// 查询所有宿舍楼
export function getAllDormBuildings() {
    return request({
        url: '/dormBuilding/all',
        method: 'get'
    })
}
