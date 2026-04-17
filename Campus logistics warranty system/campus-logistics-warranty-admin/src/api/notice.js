import request from '@/utils/request'

// 获取公告列表
export function getNoticeList(params) {
  return request({
    url: '/notice/list',
    method: 'get',
    params
  })
}

// 根据ID获取公告
export function getNoticeById(id) {
  return request({
    url: `/notice/getById/${id}`,
    method: 'get'
  })
}

// 删除公告
export function deleteNotice(id) {
  return request({
    url: `/notice/deleteById/${id}`,
    method: 'get'
  })
}

// 保存或更新公告
export function saveNotice(data) {
  return request({
    url: '/notice/save',
    method: 'post',
    data
  })
} 