<template>
  <div class="page lead-detail">
    <van-nav-bar title="客资详情" left-arrow @click-left="$router.back()" :border="false" />

    <div class="page-scroll" v-if="lead">
      <div class="card">
        <div class="field"><span class="lbl">留言时间</span><span class="val">{{ fmtTime(lead.createdTime) }}</span></div>
        <div class="field"><span class="lbl">用户需求</span><span class="val">{{ lead.message }}</span></div>
        <div class="field"><span class="lbl">关联商品</span><span class="val">{{ lead.productTitle }}</span></div>
        <div class="field"><span class="lbl">用户邮箱</span><span class="val">{{ lead.buyerEmail }}</span></div>
        <div class="field last"><span class="lbl">商家账号</span><span class="val">{{ lead.merchantEmail }}</span></div>
      </div>
      <div style="height: 90px"></div>
    </div>

    <div class="bottom" v-if="lead">
      <button v-if="lead.emailFullVisible" class="btn-outline" @click="copyEmail">复制邮箱</button>
      <button v-if="lead.emailFullVisible" class="btn-primary" @click="onMark">{{ lead.status === 'CONTACTED' ? '已联系' : '标记已联系' }}</button>
      <button v-if="!lead.emailFullVisible" class="btn-gold block" @click="$router.push('/account')">升级VIP看完整邮箱</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import { leadDetail, markContacted } from '@/api/lead'

const route = useRoute()

const lead = ref(null)

function fmtTime(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '' }

async function load() {
  try { lead.value = await leadDetail(route.params.id) } catch (e) {}
}

async function copyEmail() {
  try { await navigator.clipboard.writeText(lead.value.buyerEmail); showToast('已复制邮箱') }
  catch (e) { showToast('复制失败') }
}

async function onMark() {
  if (lead.value.status === 'CONTACTED') return
  try { await markContacted(route.params.id); lead.value.status = 'CONTACTED'; showToast('已标记') } catch (e) {}
}

onMounted(load)
</script>

<style scoped>
.lead-detail { background: var(--color-bg); }
.card { background: #fff; margin: 12px; border-radius: var(--radius-lg); padding: 4px 16px; box-shadow: var(--shadow-card); }
.field { display: flex; padding: 16px 0; border-bottom: 1px solid var(--color-border); gap: 16px; }
.field.last { border-bottom: none; }
.lbl { width: 72px; color: var(--color-text-placeholder); font-size: 14px; flex-shrink: 0; }
.val { flex: 1; font-size: 14px; color: var(--color-text); word-break: break-all; }

.bottom { display: flex; gap: 10px; padding: 12px 16px calc(12px + env(safe-area-inset-bottom)); background: #fff; border-top: 1px solid var(--color-border); }
.btn-primary { flex: 1; height: 46px; border: none; border-radius: var(--radius-md); background: var(--color-primary); color: #fff; font-size: 15px; font-weight: 500; box-shadow: var(--shadow-primary); transition: transform .12s ease, background .12s ease; }
.btn-primary:active { background: var(--color-primary-dark); transform: scale(.98); }
.btn-outline { flex: 1; height: 46px; border: 1px solid var(--color-primary); background: #fff; color: var(--color-primary); border-radius: var(--radius-md); font-size: 15px; transition: transform .12s ease, background .12s ease; }
.btn-outline:active { transform: scale(.98); background: var(--color-primary-light); }
.btn-gold { height: 46px; border: none; border-radius: var(--radius-md); background: linear-gradient(135deg, var(--color-gold), #d99a2b); color: #fff; font-size: 15px; font-weight: 500; box-shadow: 0 6px 16px rgba(232,181,71,.3); transition: transform .12s ease; }
.btn-gold:active { transform: scale(.98); }
.btn-gold.block { flex: 1; }
</style>
