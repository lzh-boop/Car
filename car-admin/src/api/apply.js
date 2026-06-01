import request from '@/utils/request'

// 普通用户：查询自己的申请列表（用车申请页面）
export const getApplyList      = (params) => request.get('/api/apply/list', { params })
// 管理员专属：查询所有申请列表（审批管理页面，后端要求 ADMIN 角色）
export const getAdminApplyList = (params) => request.get('/api/apply/admin/list', { params })
export const getApplyDetail    = (id) => request.get(`/api/apply/detail/${id}`)
export const createApply       = (data) => request.post('/api/apply/create', data)
export const updateApply       = (data) => request.put('/api/apply/update', data)
export const cancelApply       = (id) => request.put(`/api/apply/cancel/${id}`)
export const deleteApply       = (id) => request.delete(`/api/apply/${id}`)
export const approveApply      = (data) => request.post('/api/apply/approve', data)
