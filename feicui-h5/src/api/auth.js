import request from '@/utils/request'

/** 发送邮箱验证码 */
export const sendCode = (email) => request.post('/auth/send-code', { email })

/** 邮箱验证码登录/注册 */
export const login = (email, code) => request.post('/auth/login', { email, code })
