import request from '@/utils/request'

// 获取帖子列表
export function getPostList(params) {
  return request({
    url: '/post/list',
    method: 'get',
    params
  })
}

// 获取帖子详情
export function getPostById(id) {
  return request({
    url: `/post/getById/${id}`,
    method: 'get'
  })
}

// 保存或更新帖子
export function savePost(data) {
  return request({
    url: '/post/save',
    method: 'post',
    data
  })
}

// 删除帖子
export function deletePost(id) {
  return request({
    url: `/post/deleteById/${id}`,
    method: 'delete'
  })
} 