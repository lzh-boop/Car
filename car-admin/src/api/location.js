import request from '@/utils/request'

export const getBeidouList     = (params)    => request.get('/api/location/beidou/list', { params })
export const getLatestLocation = (vehicleId) => request.get(`/api/location/beidou/latest/${vehicleId}`)

/**
 * 经纬度解析为详细地址（后端调用高德Web服务逆地理编码）
 * @param {number} longitude  经度
 * @param {number} latitude   纬度
 * @param {number} [id]       定位记录ID（传入则同步更新数据库中的address字段）
 * @returns {Promise<string>} 详细地址（省市区街道门牌号）
 */
export const resolveAddress = (longitude, latitude, id = null) =>
  request.get('/api/location/beidou/resolve-address', {
    params: { longitude, latitude, ...(id ? { id } : {}) }
  })

export const getTrackHistory  = (params)    => request.get('/api/location/track/history', { params })
export const getTrackPlayback = (params)    => request.get('/api/location/track/playback', { params })
export const getTodayTrack    = (vehicleId) => request.get(`/api/location/track/today/${vehicleId}`)
