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

