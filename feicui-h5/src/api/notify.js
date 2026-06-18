import request from '@/utils/request'

export const notifyList = (params) => request.get('/merchant/notifications', { params })
export const unreadCount = () => request.get('/merchant/notifications/unread-count')
export const markRead = (id) => request.put(`/merchant/notifications/${id}/read`)
export const markAllRead = () => request.put('/merchant/notifications/read-all')
