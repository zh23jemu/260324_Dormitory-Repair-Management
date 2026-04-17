import request from '@/utils/request'

// 获取留言列表
export function getMessageList(params) {
  return request({
    url: '/message/list',
    method: 'get',
    params
  })
}

// 根据ID获取留言
export function getMessageById(id) {
  return request({
    url: `/message/getById/${id}`,
    method: 'get'
  })
}

// 删除留言
export function deleteMessage(id) {
  return request({
    url: `/message/deleteById/${id}`,
    method: 'get'
  })
}

// 保存或更新留言
export function saveMessage(data) {
  return request({
    url: '/message/save',
    method: 'post',
    data
  })
} 