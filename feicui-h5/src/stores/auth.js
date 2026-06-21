import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 商家登录态 (token + 商家信息), 持久化到 localStorage.
 */
export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('fc_token') || '')
  const merchant = ref(JSON.parse(localStorage.getItem('fc_merchant') || 'null'))
  /** 上次同步 profile 的时间戳(不持久化); 登录/页面刷新后为 0, 触发首次后台刷新 */
  const lastProfileSync = ref(0)

  const isLogin = computed(() => !!token.value)
  const isVip = computed(() => merchant.value?.tier === 'VIP')

  function setLogin(data) {
    token.value = data.token
    merchant.value = {
      id: data.merchantId,
      email: data.email,
      tier: data.tier,
      vipExpireTime: data.vipExpireTime
    }
    localStorage.setItem('fc_token', token.value)
    localStorage.setItem('fc_merchant', JSON.stringify(merchant.value))
    lastProfileSync.value = 0
  }

  function clear() {
    token.value = ''
    merchant.value = null
    localStorage.removeItem('fc_token')
    localStorage.removeItem('fc_merchant')
  }

  /** 更新商家信息(如升级 VIP 后刷新 tier/vipExpireTime) */
  function setMerchant(p) {
    merchant.value = {
      id: p.id ?? merchant.value?.id,
      email: p.email ?? merchant.value?.email,
      tier: p.tier,
      vipExpireTime: p.vipExpireTime
    }
    localStorage.setItem('fc_merchant', JSON.stringify(merchant.value))
  }

  return { token, merchant, isLogin, isVip, lastProfileSync, setLogin, setMerchant, clear }
})
