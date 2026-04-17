import request from '@/utils/request'

/**
 * 获取资源列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getResourceList(params) {
  return request({
    url: '/resource/list',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取资源详情
 * @param {number} id - 资源ID
 * @returns {Promise}
 */
export function getResourceById(id) {
  return request({
    url: `/resource/getById/${id}`,
    method: 'get'
  })
}

/**
 * 删除资源
 * @param {number} id - 资源ID
 * @returns {Promise}
 */
export function deleteResource(id) {
  return request({
    url: `/resource/deleteById/${id}`,
    method: 'delete'
  })
}

/**
 * 保存或更新资源
 * @param {Object} data - 资源数据
 * @returns {Promise}
 */
export function saveResource(data) {
  return request({
    url: '/resource/save',
    method: 'post',
    data
  })
} 