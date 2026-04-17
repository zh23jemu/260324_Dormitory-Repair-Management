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

