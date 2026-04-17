import request from '@/utils/request'

/**
 * 文件上传
 * @param {File} file - 要上传的文件
 * @returns {Promise} 返回带有文件URL的Promise
 */
export function uploadFile(file) {
    // 创建 FormData 对象
    const formData = new FormData()
    formData.append('file', file)

    return request({
        url: '/file/upload',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}