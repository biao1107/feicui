import request from '@/utils/request'

export const getProfile = () => request.get('/merchant/profile')
export const getDashboard = () => request.get('/merchant/dashboard')
export const updateEmail = (email, code) => request.put('/merchant/email', { email, code })
export const updateNotifySettings = (webNotify, emailNotify) =>
  request.put('/merchant/notification-settings', { webNotify, emailNotify })

/** 升级/续费 VIP(模拟开通) */
export const upgradeVip = (months = 12) =>
  request.post('/merchant/upgrade', null, { params: { months } })

export const logoutApi = () => request.post('/merchant/logout')
