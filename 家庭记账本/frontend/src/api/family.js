import request from './request'

/** 创建家庭 */
export const createFamily = (name) => request.post('/family/create', { name })

/** 加入家庭（需提供邀请码和身份标签名） */
export const joinFamily = (inviteCode, labelName) => request.post('/family/join', { inviteCode, labelName })

/** 获取家庭成员列表 */
export const getMembers = () => request.get('/family/members')

/** 添加身份标签 */
export const addMember = (name) => request.post('/family/members', { name })

/** 删除身份标签 */
export const deleteMember = (id) => request.delete(`/family/members/${id}`)

/** 邀请用户（通过展示ID） */
export const inviteUser = (memberId, displayId) => request.post('/family/invite', { memberId, displayId })

/** 踢出用户 */
export const kickUser = (memberId) => request.post(`/family/kick/${memberId}`)

/** 修改自己的身份标签名称 */
export const updateMemberName = (memberId, name) => request.put(`/family/members/${memberId}/name`, { name })

/** 获取家庭信息 */
export const getFamilyInfo = () => request.get('/family/info')
