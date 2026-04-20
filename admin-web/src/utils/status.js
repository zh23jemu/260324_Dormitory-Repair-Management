/*
 * 工单状态中文映射。
 * 注意：后端和数据库仍使用英文编码保证流程判断稳定，前端只在展示层转换为中文。
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
