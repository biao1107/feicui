import request from '@/utils/request'

/** 首页 AI 找货匹配 */
export const aiMatch = (message) => request.post('/home/ai/match', { message })
