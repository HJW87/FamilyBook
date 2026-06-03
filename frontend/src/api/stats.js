import request from './request'

/** 收支汇总 */
export function getSummary(params) {
  return request.get('/stats/summary', { params })
}

/** 按类别统计 */
export function getByCategory(params) {
  return request.get('/stats/by-category', { params })
}

/** 按成员统计 */
export function getByMember(params) {
  return request.get('/stats/by-member', { params })
}

/** 月度趋势 */
export function getMonthlyTrend(params) {
  return request.get('/stats/monthly-trend', { params })
}
