import request from '@/utils/request'

export const login = (data) => request.post('/api/auth/login', data)
export const logout = () => request.post('/api/auth/logout')
export const getUserInfo = () => request.get('/api/auth/info')
export const updatePassword = (data) => request.put('/api/auth/password', data)
