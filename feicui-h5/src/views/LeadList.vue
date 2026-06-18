<template>
  <div class="page lead-list">
    <van-nav-bar title="客资列表" left-arrow @click-left="$router.push('/dashboard')" :border="false" />

    <van-tabs v-model="activeTab" color="#10A47A" title-active-color="#10A47A">
      <van-tab :name="''">全部</van-tab>
      <van-tab name="PENDING">待联系</van-tab>
      <van-tab name="CONTACTED">已联系</van-tab>
    </van-tabs>

    <div class="page-scroll">
      <div class="list">
        <div class="lead-item" v-for="l in list" :key="l.id" @click="$router.push('/lead/' + l.id)">
          <div class="left">
            <div class="time">{{ fmtTime(l.createdTime) }}</div>
            <div class="status" :class="l.status">{{ statusText(l.status) }}</div>
          </div>
          <div class="mid">
            <div class="product ellipsis">{{ l.productTitle }}</div>
            <div class="msg ellipsis">{{ l.message }}</div>
          </div>
          <div class="right">{{ l.buyerEmail }}</div>
        </div>
        <div v-if="!list.length" class="empty">暂无客资</div>
      </div>
      <div style="height: 80px"></div>
    </div>

    <button v-if="!auth.isVip" class="upgrade-btn" @click="$router.push('/account')">升级VIP，查看全部联系方式</button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onActivated } from 'vue'
import { leadList } from '@/api/lead'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const activeTab = ref('')
const all = ref([])

const list = computed(() => activeTab.value ? all.value.filter(l => l.status === activeTab.value) : all.value)

function statusText(s) { return { PENDING: '待联系', CONTACTED: '已联系' }[s] || s }
function fmtTime(t) { return t ? String(t).replace('T', ' ').slice(5, 16) : '' }

async function load() {
  try {
    const res = await leadList({ current: 1, size: 200 })
    all.value = res.records || []
  } catch (e) {}
}

onMounted(load)
onActivated(load)
</script>

<style scoped>
.lead-list { background: var(--color-bg); }
.list { padding: 10px 12px; }
.lead-item { display: flex; gap: 8px; background: #fff; border-radius: var(--radius-md); padding: 12px; margin-bottom: 10px; align-items: stretch; box-shadow: var(--shadow-card); transition: transform .12s ease; }
.lead-item:active { transform: scale(.99); }
.left { width: 70px; display: flex; flex-direction: column; gap: 6px; flex-shrink: 0; }
.time { font-size: 11px; color: var(--color-text-placeholder); }
.status { font-size: 11px; font-weight: 500; padding: 1px 8px; border-radius: var(--radius-pill); width: fit-content; }
.status.PENDING { color: var(--color-gold); background: var(--color-gold-light); }
.status.CONTACTED { color: var(--color-primary); background: var(--color-primary-light); }
.mid { flex: 1; min-width: 0; }
.product { font-size: 14px; font-weight: 600; }
.msg { font-size: 13px; color: var(--color-text-secondary); margin-top: 6px; }
.right { font-size: 11px; color: var(--color-text-secondary); align-self: flex-end; white-space: nowrap; }
.empty { text-align: center; color: var(--color-text-placeholder); padding: 56px 0; font-size: 13px; }
.upgrade-btn { margin: 0 16px calc(16px + env(safe-area-inset-bottom)); height: 46px; border: none; border-radius: var(--radius-md); background: linear-gradient(135deg, var(--color-gold), #d99a2b); color: #fff; font-size: 15px; font-weight: 500; box-shadow: 0 6px 16px rgba(232,181,71,.3); transition: transform .12s ease; }
.upgrade-btn:active { transform: scale(.98); }
</style>
