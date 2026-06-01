import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { title: '登录', public: true }
    },
    {
      path: '/',
      component: () => import('@/layout/MainLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { title: '首页' }
        },
        {
          path: 'vehicle',
          name: 'vehicle',
          component: () => import('@/views/vehicle/VehicleList.vue'),
          meta: { title: '车辆管理', roles: ['ADMIN'] }
        },
        {
          path: 'dispatch',
          name: 'dispatch',
          component: () => import('@/views/dispatch/DispatchList.vue'),
          meta: { title: '调度管理', roles: ['ADMIN'] }
        },
        {
          path: 'apply',
          name: 'apply',
          component: () => import('@/views/apply/ApplyList.vue'),
          meta: { title: '用车申请', roles: ['USER', 'ADMIN'] }
        },
        {
          path: 'approve',
          name: 'approve',
          component: () => import('@/views/approve/ApproveList.vue'),
          meta: { title: '申请审批', roles: ['ADMIN'] }
        },
        {
          path: 'driver',
          name: 'driver',
          component: () => import('@/views/driver/DriverList.vue'),
          meta: { title: '驾驶员管理', roles: ['ADMIN'] }
        },
        {
          path: 'return',
          name: 'return',
          component: () => import('@/views/return/ReturnList.vue'),
          meta: { title: '还车管理', roles: ['USER', 'ADMIN'] }
        },
        {
          path: 'location',
          name: 'location',
          component: () => import('@/views/location/LocationMap.vue'),
          meta: { title: 'GPS 实时位置', roles: ['ADMIN'] }
        },
        {
          path: 'vehicle/maintain',
          name: 'vehicleMaintain',
          component: () => import('@/views/vehicle/VehicleMaintain.vue'),
          meta: { title: '车辆维保', roles: ['ADMIN'] }
        },
        {
          path: 'vehicle/stats',
          name: 'vehicleStats',
          component: () => import('@/views/vehicle/VehicleStats.vue'),
          meta: { title: '统计报表', roles: ['ADMIN'] }
        },
        {
          path: 'system/user',
          name: 'systemUser',
          component: () => import('@/views/system/UserList.vue'),
          meta: { title: '用户管理', roles: ['ADMIN'] }
        }
      ]
    },
    {
      path: '/403',
      name: '403',
      component: () => import('@/views/ForbiddenView.vue'),
      meta: { title: '无权访问', public: true }
    },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
})

export default router
