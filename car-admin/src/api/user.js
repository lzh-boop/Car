import request from '@/utils/request'

export const getUserList    = (params) => request.get('/api/system/user/list', { params })
export const getUserDetail  = (id) => request.get(`/api/system/user/${id}`)
export const addUser        = (data) => request.post('/api/system/user', data)
export const updateUser     = (data) => request.put('/api/system/user', data)
export const deleteUser     = (id) => request.delete(`/api/system/user/${id}`)
export const resetPassword  = (id, newPassword) =>
  request.put(`/api/system/user/resetPassword/${id}`, null, { params: { newPassword } })
export const changeStatus   = (id, status) =>
  request.put(`/api/system/user/changeStatus/${id}`, null, { params: { status } })
