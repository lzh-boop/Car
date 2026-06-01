import request from '@/utils/request'

export const getReturnList  = (params) => request.get('/api/return/list', { params })
export const getReturnDetail = (id) => request.get(`/api/return/${id}`)
export const doReturn       = (data) => request.put('/api/return/do', data)
export const deleteReturn   = (id) => request.delete(`/api/return/${id}`)
