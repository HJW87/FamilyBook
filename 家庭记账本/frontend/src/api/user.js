import request from './request'

/** 获取当前用户资料 */
export function getProfile() {
  return request.get('/user/profile')
}

/** 更新头像（预设：male/female/null） */
export function updateAvatar(avatar) {
  return request.put('/user/avatar', { avatar })
}

/** 上传自定义头像图片 */
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/user/avatar/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 删除自定义头像（恢复默认） */
export function deleteAvatar() {
  return request.delete('/user/avatar')
}
