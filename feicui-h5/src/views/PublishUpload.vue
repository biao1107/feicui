<template>
  <div class="page publish-upload">
    <van-nav-bar title="发布商品" left-arrow @click-left="$router.back()" :border="false" />

    <div class="page-scroll">
      <!-- 步骤流 -->
      <div class="steps">
        <div class="step active"><i>1</i>上传商品图片</div>
        <div class="step-line"></div>
        <div class="step"><i>2</i>AI智能生成</div>
        <div class="step-line"></div>
        <div class="step"><i>3</i>编辑商品信息</div>
        <div class="step-line"></div>
        <div class="step"><i>4</i>提交发布</div>
      </div>
      <p class="step-tip">上传清晰的翡翠图片，AI将为您自动生成商品文案</p>

      <!-- 上传图片 -->
      <div class="upload-grid">
        <div class="upload-box" v-for="(img, i) in previews" :key="i">
          <img :src="img" />
          <span class="del" @click="removeImg(i)">×</span>
        </div>
        <label class="upload-box add" v-if="previews.length < 6">
          <span>＋</span>
          <input type="file" accept="image/*" multiple hidden @change="onPick" />
        </label>
      </div>

      <!-- 额度提示 -->
      <div class="quota" v-html="quotaText"></div>
    </div>

    <button class="ai-btn" :class="{ disabled: !canGenerate }" :disabled="!canGenerate || generating" @click="onGenerate">
      {{ generating ? 'AI识别中...' : 'AI生成' }}
    </button>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { uploadImage, aiGenerate, productList } from '@/api/product'
import { getProfile } from '@/api/merchant'
import { useDraftStore } from '@/stores/publishDraft'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const draft = useDraftStore()
const auth = useAuthStore()

const files = ref([])      // 原始 File 对象
const previews = ref([])   // 本地预览 URL
const generating = ref(false)
const listedCount = ref(0)
const limit = ref(2)

const canGenerate = computed(() => files.value.length > 0 && listedCount.value < limit.value)
const quotaText = computed(() => {
  const tag = auth.isVip ? 'VIP' : '免费'
  return `${tag}商家最多发布 <b style="color:#10A47A">${limit.value}</b> 件商品，当前已发布 <b style="color:#10A47A">${listedCount.value}/${limit.value}</b> 件`
})

function onPick(e) {
  const picked = Array.from(e.target.files || [])
  picked.forEach(f => {
    files.value.push(f)
    previews.value.push(URL.createObjectURL(f))
  })
  e.target.value = ''
}

function removeImg(i) {
  files.value.splice(i, 1)
  previews.value.splice(i, 1)
}

async function loadQuota() {
  try {
    const p = await getProfile()
    limit.value = p.productLimit
    const res = await productList({ status: 'LISTED', current: 1, size: 1 })
    listedCount.value = res.total || 0
  } catch (e) {}
}

async function onGenerate() {
  if (listedCount.value >= limit.value) {
    return showToast(auth.isVip ? '请下架部分商品后再发布' : '需升级VIP提升发布额度')
  }
  generating.value = true
  try {
    // 1. 逐张上传到 OSS
    const urls = []
    for (const f of files.value) {
      const r = await uploadImage(f)
      urls.push(r.url)
    }
    // 2. AI 识图生成文案
    const fields = await aiGenerate(urls)
    // 3. 存草稿, 跳编辑页
    draft.reset()
    draft.setDraft(urls, { ...fields, aiGenerated: 1 })
    router.push('/publish/edit')
  } catch (e) {
    /* OSS/AI 失败已在拦截器提示 */
  } finally {
    generating.value = false
  }
}

loadQuota()
</script>

<style scoped>
.publish-upload { background: #fff; }
.steps { display: flex; align-items: center; padding: 20px 16px 4px; font-size: 11px; }
.step { display: flex; align-items: center; gap: 4px; color: var(--color-text-placeholder); flex-shrink: 0; }
.step.active { color: var(--color-primary); }
.step i { width: 18px; height: 18px; border-radius: 50%; background: #e5e7eb; color: #fff; display: inline-flex; align-items: center; justify-content: center; font-style: normal; font-size: 11px; }
.step.active i { background: var(--color-primary); }
.step-line { flex: 1; height: 1px; background: var(--color-border); margin: 0 4px; }
.step-tip { font-size: 13px; color: var(--color-text-placeholder); padding: 4px 16px 16px; }

.upload-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; padding: 0 16px; }
.upload-box { aspect-ratio: 1; border-radius: var(--radius-md); overflow: hidden; position: relative; background: var(--color-primary-light); }
.upload-box img { width: 100%; height: 100%; object-fit: cover; }
.upload-box.add { border: 1px dashed var(--color-text-placeholder); background: #f5f7fa; display: flex; align-items: center; justify-content: center; color: var(--color-text-placeholder); font-size: 30px; }
.del { position: absolute; top: 2px; right: 4px; color: #fff; background: rgba(0,0,0,.5); width: 18px; height: 18px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 13px; }

.quota { padding: 20px 16px; font-size: 13px; color: var(--color-text-secondary); }

.ai-btn { margin: 0 16px calc(16px + env(safe-area-inset-bottom)); height: 48px; border: none; border-radius: var(--radius-md); background: var(--color-primary); color: #fff; font-size: 16px; font-weight: 500; box-shadow: var(--shadow-primary); transition: transform .12s ease; }
.ai-btn:active { transform: scale(.98); }
.ai-btn.disabled { background: #c8d4cf; box-shadow: none; }
</style>
