import request from './request'

/** 分页查询记录 */
export function getRecords(params) {
  return request.get('/records', { params })
}

/** 获取单条记录 */
export function getRecordById(id) {
  return request.get(`/records/${id}`)
}

/** 添加记录 */
export function addRecord(data) {
  return request.post('/records', data)
}

/** 修改记录 */
export function updateRecord(id, data) {
  return request.put(`/records/${id}`, data)
}

/** 删除记录 */
export function deleteRecord(id) {
  return request.delete(`/records/${id}`)
}
