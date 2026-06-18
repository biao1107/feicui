<template>
  <div class="page product-mgmt">
    <van-nav-bar title="商品管理" left-arrow @click-left="$router.push('/dashboard')" :border="false">
      <template #right><span @click="showToast('分享开发中')">⤴</span></template>
    </van-nav-bar>

    <!-- 分类标签 -->
    <van-tabs v-model="activeTab" color="#10A47A" title-active-color="#10A47A">
      <van-tab :name="''">全部({{ counts.all }})</van-tab>
      <van-tab name="LISTED">已上架({{ counts.LISTED }})</van-tab>
      <van-tab name="DRAFT">草稿({{ counts.DRAFT }})</van-tab>
      <van-tab name="DELISTED">已下架({{ counts.DELISTED }})</van-tab>
    </van-tabs>

    <div class="page-scroll">
      <div class="list">
        <div class="prod-item" v-for="p in filtered" :key="p.id">
          <img class="prod-img" :src="p.coverImage" @error="(e)=>e.target.style.background='var(--color-primary-light)'" />
          <div class="prod-info">
            <div class="prod-title ellipsis">{{ p.title }}</div>
            <div class="prod-price">¥{{ formatPrice(p.price) }}</div>
            <div class="prod-time">{{ fmtTime(p.createdTime) }}</div>
          </div>
          <div class="prod-right">
            <span class="status-tag" :class="p.status">{{ statusText(p.status) }}</span>
            <div class="prod-actions">
              <button @click="onEdit(p.id)">编辑</button>
              <button class="del" @click="onDelete(p)">删除</button>
            </div>
          </div>
        </div>
        <div v-if="!filtered.length" class="empty">暂无商品</div>
      </div>
      <div style="height: 80px"></div>
    </div>

    <button class="add-btn" :class="{ disabled: !canAdd }" @click="onAdd">{{ addBtnText }}</button>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onActivated, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { productList, deleteProduct } from '@/api/product'
import { getProfile } from '@/api/merchant'
import { useAuthStore } from '@/stores/auth'
import { useDraftStore } from '@/stores/publishDraft'

const router = useRouter()
const auth = useAuthStore()
const draft = useDraftStore()

const activeTab = ref('')
const all = ref([])
const counts = reactive({ all: 0, LISTED: 0, DRAFT: 0, DELISTED: 0 })
const limit = ref(2)

const filtered = computed(() => activeTab.value ? all.value.filter(p => p.status === activeTab.value) : all.value)
const listedCount = computed(() => counts.LISTED)
const canAdd = computed(() => listedCount.value < limit.value)
const addBtnText = computed(() => {
  if (canAdd.value) return '＋ 发布新商品'
  return auth.isVip ? '请下架部分商品后再发布' : '升级VIP发布更多商品'
})

function statusText(s) { return { LISTED: '已上架', DRAFT: '草稿', DELISTED: '已下架' }[s] || s }
function formatPrice(p) { return p == null ? '--' : Number(p).toLocaleString('zh-CN') }
function fmtTime(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '' }

async function load() {
  try {
    const p = await getProfile()
    limit.value = p.productLimit
    const res = await productList({ current: 1, size: 200 })
    all.value = res.records || []
    counts.all = all.value.length
    counts.LISTED = all.value.filter(x => x.status === 'LISTED').length
    counts.DRAFT = all.value.filter(x => x.status === 'DRAFT').length
    counts.DELISTED = all.value.filter(x => x.status === 'DELISTED').length
  } catch (e) {}
}

function onEdit(id) {
  draft.reset()
  draft.setEdit(id)
  router.push('/publish/edit')
}

async function onDelete(p) {
  try {
    await showConfirmDialog({ title: '确认删除', message: `删除「${p.title}」？` })
    await deleteProduct(p.id)
    showToast('已删除')
    load()
  } catch (e) { /* 取消 */ }
}

function onAdd() {
  if (!canAdd.value) return showToast(auth.isVip ? '请下架部分商品后再发布' : '需升级VIP提升发布额度')
  router.push('/publish')
}

onMounted(load)
onActivated(load)
</script>

<style scoped>
.product-mgmt { background: var(--color-bg); }
.list { padding: 10px 12px; }
.prod-item { display: flex; gap: 10px; background: #fff; border-radius: var(--radius-md); padding: 10px; margin-bottom: 10px; align-items: center; box-shadow: var(--shadow-card); }
.prod-img { width: 70px; height: 70px; border-radius: var(--radius-sm); object-fit: cover; background: var(--color-primary-light); flex-shrink: 0; }
.prod-info { flex: 1; min-width: 0; }
.prod-title { font-size: 15px; font-weight: 600; }
.prod-price { font-size: 14px; color: var(--color-primary); font-weight: 600; margin-top: 4px; }
.prod-time { font-size: 11px; color: var(--color-text-placeholder); margin-top: 4px; }
.prod-right { display: flex; flex-direction: column; align-items: flex-end; gap: 8px; flex-shrink: 0; }
.status-tag { font-size: 11px; font-weight: 500; padding: 2px 10px; border-radius: var(--radius-pill); }
.status-tag.LISTED { color: var(--color-primary); background: var(--color-primary-light); }
.status-tag.DRAFT { color: #9CA3AF; background: #f0f1f3; }
.status-tag.DELISTED { color: #EF4444; background: #fef2f2; }
.prod-actions { display: flex; gap: 6px; }
.prod-actions button { border: 1px solid var(--color-border); background: #fff; border-radius: 6px; font-size: 12px; padding: 3px 10px; color: var(--color-text-secondary); }
.prod-actions button.del { color: var(--color-danger); }
.empty { text-align: center; color: var(--color-text-placeholder); padding: 40px 0; font-size: 14px; }

.add-btn { margin: 0 16px calc(16px + env(safe-area-inset-bottom)); height: 46px; border: none; border-radius: var(--radius-md); background: var(--color-primary); color: #fff; font-size: 15px; font-weight: 500; box-shadow: var(--shadow-primary); transition: transform .12s ease; }
.add-btn:active { transform: scale(.98); }
.add-btn.disabled { background: #c8d4cf; box-shadow: none; }
</style>
