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
