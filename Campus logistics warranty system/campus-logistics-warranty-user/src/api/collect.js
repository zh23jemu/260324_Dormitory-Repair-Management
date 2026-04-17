import request from '@/utils/request'

/**
 * 获取用户收藏列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getCollectList(params) {
  return request({
    url: '/collect/list',
    method: 'get',
    params
  })
}

/**
 * 保存收藏
 * @param {Object} data - 收藏数据
 * @returns {Promise}
 */
export function saveCollect(data) {
  return request({
    url: '/collect/save',
    method: 'post',
    data
  })
}

/**
 * 删除收藏
 * @param {number} id - 收藏ID
 * @returns {Promise}
 */
export function deleteCollect(id) {
  return request({
    url: `/collect/deleteById/${id}`,
    method: 'delete'
  })
} 