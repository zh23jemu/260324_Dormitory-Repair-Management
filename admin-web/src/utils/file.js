import api from '../api'

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
