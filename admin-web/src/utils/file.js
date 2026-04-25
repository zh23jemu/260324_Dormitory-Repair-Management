import api from '../api'

/**
 * 将后端返回的相对文件路径转换为完整可访问地址。
 * 后端图片统一通过 uploads 目录暴露，前端展示时需要拼接后端域名。
 */
export function fileUrl(path) {
  if (!path) return ''
  if (/^https?:\/\//.test(path)) return path
  try {
    return `${new URL(api.defaults.baseURL).origin}${path}`
  } catch {
    return path
  }
}

export function toPercent(value) {
  return `${Number(value || 0).toFixed(1)}%`
}
