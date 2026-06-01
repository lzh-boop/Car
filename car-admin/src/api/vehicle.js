import request from '@/utils/request'

// ─── 车辆基础信息 ─────────────────────────────────────────
export const getVehicleList   = (params, config) => request.get('/api/vehicle/list', { params, ...config })
export const getVehicleDetail = (vehicleNo) => request.get(`/api/vehicle/${vehicleNo}`)
export const addVehicle       = (data) => request.post('/api/vehicle', data)
export const updateVehicle    = (data) => request.put('/api/vehicle', data)
export const deleteVehicle    = (vehicleNo) => request.delete(`/api/vehicle/${vehicleNo}`)

// ─── 车辆维护记录（保养/维修/年检/保险 统一接口，maintenanceType区分）─
// maintenanceType: 1-保养  2-维修  3-年检  4-保险
export const getMaintenanceList   = (params) => request.get('/api/vehicle/maintenance/list', { params })
export const addMaintenance       = (data)   => request.post('/api/vehicle/maintenance', data)
export const updateMaintenance    = (data)   => request.put('/api/vehicle/maintenance', data)
export const deleteMaintenance    = (id)     => request.delete(`/api/vehicle/maintenance/${id}`)

// ─── 统计报表 ─────────────────────────────────────────────
export const getVehicleStats = (params) => request.get('/api/vehicle/stats', { params })
