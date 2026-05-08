/*
 * 工单状态中文映射。
 * 后端与数据库继续保留英文状态码，前端统一在展示层转成中文。
 */
export const REPAIR_ORDER_STATUS_TEXT = {
  pending_review: '待审核',
  rejected: '已驳回',
  pending_accept: '待接单',
  processing: '处理中',
  pending_rating: '待评价',
  completed: '已完成'
}

export function repairOrderStatusText(status) {
  return REPAIR_ORDER_STATUS_TEXT[status] || status || '-'
}

// 通用状态文案映射，优先覆盖各业务模块中容易直接暴露英文状态码的场景。
export const COMMON_STATUS_TEXT = {
  enabled: '启用',
  disabled: '禁用',
  draft: '草稿',
  published: '已发布',
  hidden: '隐藏',
  normal: '正常',
  fault: '故障',
  repairing: '维修中',
  pending: '待处理',
  replied: '已回复',
  unread: '未读',
  read: '已读',
  active: '启用',
  inactive: '停用'
}

export function commonStatusText(status) {
  return COMMON_STATUS_TEXT[status] || status || '-'
}

export function commonStatusTagType(status) {
  if (['disabled', 'hidden', 'fault', 'pending', 'inactive'].includes(status)) {
    return 'danger'
  }
  if (['draft', 'repairing', 'unread'].includes(status)) {
    return 'warning'
  }
  if (['published', 'enabled', 'normal', 'replied', 'read', 'active'].includes(status)) {
    return 'success'
  }
  return 'info'
}
