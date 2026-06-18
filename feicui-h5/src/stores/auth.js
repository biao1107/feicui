import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 商家登录态 (token + 商家信息), 持久化到 localStorage.
 */
export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('fc_token') || '')
  const merchant = ref(JSON.parse(localStorage.getItem('fc_merchant') || 'null'))

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

  return { token, merchant, isLogin, isVip, setLogin, setMerchant, clear }
})
