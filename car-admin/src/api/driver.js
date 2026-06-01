import request from '@/utils/request'

export const getDriverList   = (params) => request.get('/api/vehicle/driver/list', { params })
export const getDriverDetail = (id) => request.get(`/api/vehicle/driver/${id}`)
export const addDriver       = (data) => request.post('/api/vehicle/driver', data)
export const updateDriver    = (data) => request.put('/api/vehicle/driver', data)
export const deleteDriver    = (id) => request.delete(`/api/vehicle/driver/${id}`)
