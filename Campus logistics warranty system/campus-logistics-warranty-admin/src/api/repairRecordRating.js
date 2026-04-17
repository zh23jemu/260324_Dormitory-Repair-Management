import request from '@/utils/request'

/**
 * 保存维修记录评分
 * @param {Object} data - 评分数据
 * @returns {Promise}
 */
export function saveRepairRecordRating(data) {
  return request({
    url: '/repairRecordRating/save',
    method: 'post',
    data
  })
}

/**
 * 获取维修记录评分列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getRepairRecordRatingList(params) {
  return request({
    url: '/repairRecordRating/list',
    method: 'get',
    params
  })
}

/**
 * 删除维修记录评分
 * @param {number} id - 评分ID
 * @returns {Promise}
 */
export function deleteRepairRecordRating(id) {
  return request({
    url: `/repairRecordRating/deleteById/${id}`,
    method: 'delete'
  })
} 