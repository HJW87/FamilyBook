import request from './request'

/** 获取类别列表 */
export function getCategories(type) {
  return request.get('/categories', { params: { type } })
}

/** 添加类别 */
export function addCategory(data) {
  return request.post('/categories', data)
}

/** 修改类别 */
export function updateCategory(id, data) {
  return request.put(`/categories/${id}`, data)
}

/** 删除类别 */
export function deleteCategory(id) {
  return request.delete(`/categories/${id}`)
}
