import request from '@/utils/request'

/**
 * 上传文件
 * @param {FormData} data - 文件表单数据
 * @returns {Promise}
 */
export function upload(data) {
  return request({
    url: '/file/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除文件
 * @param {String} url - 文件URL
 * @returns {Promise}
 */
export function deleteFile(url) {
  return request({
    url: '/file/delete',
    method: 'post',
    data: { url }
  })
}