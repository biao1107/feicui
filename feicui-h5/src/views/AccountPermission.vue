<template>
  <div class="page account">
    <van-nav-bar title="账户权限" left-arrow @click-left="$router.push('/profile')" :border="false">
      <template #right><span v-if="auth.isVip" class="vip-tag">VIP</span></template>
    </van-nav-bar>

    <div class="page-scroll">
      <!-- 会员卡 -->
      <div class="member-card" :class="{ vip: auth.isVip }">
        <div class="m-title">{{ auth.isVip ? 'VIP会员' : '普通会员' }}</div>
        <div class="m-expire">有效期至 {{ auth.isVip ? fmtDate(auth.merchant?.vipExpireTime) : '永久' }}</div>
      </div>

      <!-- 当前权限 -->
      <div class="table">
        <div class="table-title">当前权限</div>
        <div class="table-row"><span>商品发布上限</span><b>{{ limit }}件</b></div>
        <div class="table-row"><span>今日已发布</span><b>{{ listed }}件</b></div>
        <div class="table-row"><span>客资查看权限</span><b>{{ auth.isVip ? '无限查看全部' : '无查看权限' }}</b></div>
        <div class="table-row"><span>优先展示权重</span><b>{{ auth.isVip ? '高' : '低' }}</b></div>
      </div>

      <!-- 升级/续费 -->
      <div class="table">
        <div class="table-title">{{ auth.isVip ? '续费' : '升级' }}</div>
        <div class="table-row"><span>VIP会员（12个月）</span><b class="price">￥2999</b></div>
        <div class="table-row"><span>VIP会员（6个月）</span><b class="price">￥1688</b></div>
      </div>
      <div class="demo-tip">演示期：点击下方按钮可直接开通，无需支付</div>
      <div style="height: 90px"></div>
    </div>

    <button class="cta-btn" @click="onUpgrade">{{ auth.isVip ? '续费 VIP（12个月）' : '一键开通 VIP（12个月）' }}</button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showConfirmDialog, showToast } from 'vant'
import { getProfile, getDashboard, upgradeVip } from '@/api/merchant'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const limit = ref(2)
const listed = ref(0)

function fmtDate(d) { return d ? String(d).slice(0, 10) : '--' }

async function load() {
  try {
    const p = await getProfile()
    limit.value = p.productLimit
    const d = await getDashboard()
    listed.value = d.listedCount || 0
  } catch (e) {}
}

async function onUpgrade() {
  try {
    await showConfirmDialog({
      title: auth.isVip ? '续费 VIP' : '升级 VIP',
      message: auth.isVip ? '确认续费 12 个月 VIP？' : '演示期：确认直接开通 12 个月 VIP？',
      showCancelButton: true
    })
    const p = await upgradeVip(12)
    auth.setMerchant(p)
    showToast('已开通 VIP')
    load()
  } catch (e) { /* 取消 */ }
}

onMounted(load)
</script>

<style scoped>
.account { background: var(--color-bg); }
.vip-tag { background: var(--color-gold); color: #fff; font-size: 11px; padding: 2px 10px; border-radius: 8px; }

.member-card { margin: 12px; border-radius: var(--radius-lg); padding: 24px 20px; background: linear-gradient(135deg, #e8edf2, #f5f7fa); color: var(--color-text-secondary); }
.member-card.vip { background: linear-gradient(135deg, #E8B547, #F5D27A); color: #5a3d00; }
.m-title { font-size: 20px; font-weight: 700; }
.m-expire { font-size: 13px; margin-top: 8px; }

.table { background: #fff; margin: 0 12px 12px; border-radius: var(--radius-md); padding: 16px; box-shadow: var(--shadow-card); }
.table-title { font-size: 15px; font-weight: 600; margin-bottom: 8px; }
.table-row { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid var(--color-border); font-size: 14px; }
.table-row:last-child { border-bottom: none; }
.table-row b { color: var(--color-text); font-weight: 500; }
.table-row b.price { color: var(--color-gold); font-weight: 700; }

.demo-tip { margin: 0 12px 12px; font-size: 12px; color: var(--color-text-placeholder); }
.cta-btn { margin: 0 16px calc(16px + env(safe-area-inset-bottom)); height: 48px; border: none; border-radius: var(--radius-md); background: var(--color-primary); color: #fff; font-size: 16px; font-weight: 500; box-shadow: var(--shadow-primary); transition: transform .12s ease, background .12s ease; }
.cta-btn:active { background: var(--color-primary-dark); transform: scale(.98); }
</style>
