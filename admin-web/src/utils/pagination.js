export const DEFAULT_PAGE_SIZE = 10

export function createPageState(pageSize = DEFAULT_PAGE_SIZE) {
  return {
    pageNum: 1,
    pageSize,
    total: 0
  }
}

export function pageParams(page) {
  return {
    pageNum: page.pageNum,
    pageSize: page.pageSize
  }
}

export function readPageData(payload) {
  // 后端分页接口统一返回 records/total/pageNum/pageSize；
  // 这里兼容历史数组接口，便于部分下拉选项接口继续保持全量返回。
  if (Array.isArray(payload)) {
    return {
      records: payload,
      total: payload.length,
      pageNum: 1,
      pageSize: payload.length || DEFAULT_PAGE_SIZE
    }
  }
  return {
    records: payload?.records || [],
    total: Number(payload?.total || 0),
    pageNum: Number(payload?.pageNum || 1),
    pageSize: Number(payload?.pageSize || DEFAULT_PAGE_SIZE)
  }
}

export function applyPageResult(page, payload) {
  const result = readPageData(payload)
  page.total = result.total
  page.pageNum = result.pageNum
  page.pageSize = result.pageSize
  return result.records
}

export function paginateClient(list, page) {
  const total = Array.isArray(list) ? list.length : 0
  const pageSize = page?.pageSize || DEFAULT_PAGE_SIZE
  const pageNum = page?.pageNum || 1
  const start = (pageNum - 1) * pageSize
  return {
    records: total ? list.slice(start, start + pageSize) : [],
    total
  }
}
