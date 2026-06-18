<template>
  <div class="page notifications">
    <van-nav-bar title="系统通知" left-arrow @click-left="$router.push('/dashboard')" :border="false">
      <template #right>
        <span class="read-all" @click="onReadAll" v-if="list.length">全部已读</span>
      </template>
    </van-nav-bar>

    <div class="page-scroll">
      <div class="list">
        <div class="notify-item" v-for="n in list" :key="n.id" :class="{ unread: !n.isRead }">
          <div class="n-time">{{ fmtTime(n.createdTime) }}</div>
          <div class="n-content">
            <span class="n-type" :class="n.type">{{ typeText(n.type) }}</span>
            {{ n.content }}
          </div>
        </div>
        <div v-if="!list.length" class="empty">暂无通知</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated } from 'vue'
import { showToast } from 'vant'
import { notifyList, markAllRead } from '@/api/notify'

const list = ref([])

function typeText(t) { return { NEW_LEAD: '新客资', VIP_EXPIRE: '到期提醒' }[t] || '通知' }
function fmtTime(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '' }

async function load() {
  try {
    const res = await notifyList({ current: 1, size: 50 })
    list.value = res.records || []
  } catch (e) {}
}

async function onReadAll() {
  try { await markAllRead(); list.value.forEach(n => n.isRead = 1); showToast('已全部已读') } catch (e) {}
}

onMounted(load)
onActivated(load)
</script>

<style scoped>
.notifications { background: var(--color-bg); }
.read-all { font-size: 13px; color: var(--color-primary); }
.list { padding: 10px 12px; }
.notify-item { display: flex; gap: 12px; background: #fff; border-radius: var(--radius-md); padding: 14px; margin-bottom: 10px; box-shadow: var(--shadow-card); }
.notify-item.unread { border-left: 3px solid var(--color-primary); }
.n-time { font-size: 12px; color: var(--color-text-placeholder); width: 70px; flex-shrink: 0; }
.n-content { flex: 1; font-size: 14px; line-height: 1.6; }
.n-type { display: inline-block; font-size: 11px; font-weight: 500; padding: 1px 8px; border-radius: var(--radius-pill); margin-right: 6px; }
.n-type.NEW_LEAD { color: var(--color-primary); background: var(--color-primary-light); }
.n-type.VIP_EXPIRE { color: var(--color-gold); background: var(--color-gold-light); }
.empty { text-align: center; color: var(--color-text-placeholder); padding: 56px 0; font-size: 13px; }
</style>
