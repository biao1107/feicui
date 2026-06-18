<template>
  <div class="page dashboard">
    <van-nav-bar @click-left="go('/')" :border="false">
      <template #left><span class="back">‹</span></template>
      <template #title>商家后台<span v-if="isVip" class="vip-tag">VIP</span></template>
      <template #right>
        <span class="bell" @click="go('/notifications')">🔔<i v-if="unread > 0" class="dot"></i></span>
      </template>
    </van-nav-bar>

    <div class="page-scroll">
      <!-- 数据栏 -->
      <div class="stat-bar">
        <div class="stat">
          <div class="num">{{ data.listedCount }}/{{ data.productLimit }}</div>
          <div class="lbl">商品数量 已上架/上限</div>
        </div>
        <div class="stat">
          <div class="num">{{ data.todayLeads }}</div>
          <div class="lbl">今日客资(条)</div>
        </div>
        <div class="stat">
          <div class="num">{{ data.totalLeads }}</div>
          <div class="lbl">累计客资(条)</div>
        </div>
      </div>

      <!-- 功能入口 -->
      <div class="grid">
        <div class="grid-item" @click="go('/publish')"><span class="gi-ico bg-green">＋</span><span>发布商品</span></div>
        <div class="grid-item" @click="go('/products')"><span class="gi-ico bg-blue">🛍</span><span>商品管理</span></div>
        <div class="grid-item" @click="go('/leads')"><span class="gi-ico bg-orange">👤</span><span>客资列表</span></div>
        <div class="grid-item" @click="go('/account')"><span class="gi-ico bg-purple">👑</span><span>账户权限</span></div>
      </div>

      <!-- 最近客资 -->
      <div class="block">
        <div class="block-hd">
          <span class="block-title">最近客资</span>
          <span class="more" @click="go('/leads')">全部 ›</span>
        </div>
        <div class="lead-item" v-for="l in recent" :key="l.id" @click="go('/lead/' + l.id)">
          <div class="lead-top">
            <span class="lead-time">{{ fmtTime(l.createdTime) }}</span>
            <span class="lead-email">{{ l.buyerEmail }}</span>
          </div>
          <div class="lead-msg ellipsis">{{ l.message }}</div>
          <div class="lead-product ellipsis">商品：{{ l.productTitle }}</div>
        </div>
        <div v-if="!recent.length" class="empty">暂无客资</div>
      </div>

      <!-- banner -->
      <div class="banner">
        <div class="banner-title">极速匹配客源</div>
        <div class="banner-sub">AI智能分发，成交快人一步</div>
      </div>
      <div style="height: 64px"></div>
    </div>

    <van-tabbar v-model="active" @change="onTab" active-color="#10A47A">
      <van-tabbar-item icon="wap-home-o">首页</van-tabbar-item>
      <van-tabbar-item icon="bag-o">商品</van-tabbar-item>
      <van-tabbar-item icon="apps-o">管理后台</van-tabbar-item>
      <van-tabbar-item icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onActivated, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboard } from '@/api/merchant'
import { leadList } from '@/api/lead'
import { unreadCount } from '@/api/notify'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const isVip = computed(() => auth.isVip)

const active = ref(2)
const data = reactive({ listedCount: 0, productLimit: 2, todayLeads: 0, totalLeads: 0 })
const recent = ref([])
const unread = ref(0)

function go(path) { router.push(path) }
function onTab(i) {
  const map = ['/', '/products', '/dashboard', '/profile']
  if (map[i] !== '/dashboard') router.push(map[i])
}
function fmtTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(5, 16)
}

async function load() {
  try {
    Object.assign(data, await getDashboard())
    const res = await leadList({ current: 1, size: 3 })
    recent.value = res.records || []
    const c = await unreadCount()
    unread.value = c.count || 0
  } catch (e) { /* 已提示 */ }
}

onMounted(load)
onActivated(load)
</script>

<style scoped>
.dashboard { background: var(--color-bg); }
.back { font-size: 26px; color: var(--color-text); }
.vip-tag { background: var(--color-gold); color: #fff; font-size: 11px; padding: 2px 8px; border-radius: 6px; margin-left: 6px; vertical-align: middle; }
.bell { font-size: 18px; position: relative; }
.dot { position: absolute; top: -4px; right: -6px; width: 8px; height: 8px; background: var(--color-danger); border-radius: 50%; }

.stat-bar { display: flex; background: var(--color-primary); color: #fff; padding: 18px 0; }
.stat { flex: 1; text-align: center; border-right: 1px solid rgba(255,255,255,.25); }
.stat:last-child { border-right: none; }
.num { font-size: 22px; font-weight: 700; }
.lbl { font-size: 11px; margin-top: 4px; opacity: .9; }

.grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; padding: 14px; }
.grid-item { background: #fff; border-radius: 14px; padding: 18px 0; display: flex; flex-direction: column; align-items: center; gap: 10px; font-size: 14px; box-shadow: 0 2px 10px rgba(0,0,0,.04); transition: transform .15s; }
.grid-item:active { transform: scale(0.97); }
.gi-ico { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 22px; color: #fff; }
.bg-green { background: var(--color-primary); }
.bg-blue { background: #3B82F6; }
.bg-orange { background: #F59E0B; }
.bg-purple { background: #8B5CF6; }

.block { margin: 0 14px; background: #fff; border-radius: 14px; padding: 14px; box-shadow: 0 2px 10px rgba(0,0,0,.04); }
.block-hd { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.block-title { font-size: 15px; font-weight: 600; }
.more { font-size: 13px; color: var(--color-text-placeholder); }
.lead-item { padding: 10px 0; border-top: 1px solid var(--color-border); }
.lead-top { display: flex; justify-content: space-between; }
.lead-time { font-size: 12px; color: var(--color-text-placeholder); }
.lead-email { font-size: 12px; color: var(--color-text-secondary); }
.lead-msg { font-size: 14px; margin: 4px 0; }
.lead-product { font-size: 12px; color: var(--color-text-secondary); }
.empty { text-align: center; color: var(--color-text-placeholder); font-size: 13px; padding: 20px 0; }

.banner { margin: 14px; background: linear-gradient(135deg, #10A47A, #0D8C66); border-radius: 12px; padding: 22px 18px; color: #fff; }
.banner-title { font-size: 18px; font-weight: 700; }
.banner-sub { font-size: 12px; opacity: .9; margin-top: 4px; }
</style>
