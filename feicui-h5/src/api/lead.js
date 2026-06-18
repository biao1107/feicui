import request from '@/utils/request'

/** 游客提交客资 */
export const submitLead = (productId, buyerEmail, message) =>
  request.post(`/home/products/${productId}/lead`, { buyerEmail, message })

/** 商家客资列表 */
export const leadList = (params) => request.get('/merchant/leads', { params })

/** 首页最近客资预览(邮箱完整) */
export const leadRecent = (limit = 3) => request.get('/merchant/leads/recent', { params: { limit } })

/** 客资详情 */
export const leadDetail = (id) => request.get(`/merchant/leads/${id}`)

/** 标记已联系 */
export const markContacted = (id) => request.put(`/merchant/leads/${id}/contacted`)
