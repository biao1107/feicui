import request from '@/utils/request'

export const getProfile = () => request.get('/merchant/profile')
export const getDashboard = () => request.get('/merchant/dashboard')
export const updateEmail = (email, code) => request.put('/merchant/email', { email, code })
export const updateNotifySettings = (webNotify, emailNotify) =>
  request.put('/merchant/notification-settings', { webNotify, emailNotify })
export const logoutApi = () => request.post('/merchant/logout')
