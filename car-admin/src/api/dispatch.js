import request from '@/utils/request'

export const getDispatchList   = (params, config) => request.get('/api/dispatch/list', { params, ...config })
export const getDispatchDetail = (id) => request.get(`/api/dispatch/${id}`)
export const createDispatch    = (data) => request.post('/api/dispatch/create', data)
export const updateDispatch    = (data) => request.put('/api/dispatch/update', data)
export const startDispatch     = (id) => request.put(`/api/dispatch/start/${id}`)
export const completeDispatch  = (id) => request.put(`/api/dispatch/complete/${id}`)
export const cancelDispatch    = (id) => request.put(`/api/dispatch/cancel/${id}`)
export const deleteDispatch    = (id) => request.delete(`/api/dispatch/${id}`)
