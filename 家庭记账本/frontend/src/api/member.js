import request from './request'

/** 获取家庭成员列表（现在由 FamilyController 提供） */
export function getMembers() {
  return request.get('/family/members')
}

/** 添加家庭成员标签 */
export function addMember(name) {
  return request.post('/family/members', { name })
}

/** 删除家庭成员标签 */
export function deleteMember(id) {
  return request.delete(`/family/members/${id}`)
}
