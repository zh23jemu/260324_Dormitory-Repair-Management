import request from '@/utils/request'

// 获取评论列表
export function getCommentList(params) {
  return request({
    url: '/comment/list',
    method: 'get',
    params
  })
}

// 获取评论详情
export function getCommentById(id) {
  return request({
    url: `/comment/getById/${id}`,
    method: 'get'
  })
}

// 根据帖子ID获取评论
export function getCommentByPostId(postId) {
  return request({
    url: `/comment/getByPostId/${postId}`,
    method: 'get'
  })
}

// 保存或更新评论
export function saveComment(data) {
  return request({
    url: '/comment/save',
    method: 'post',
    data
  })
}

// 删除评论
export function deleteComment(id) {
  return request({
    url: `/comment/delete/${id}`,
    method: 'delete'
  })
} 