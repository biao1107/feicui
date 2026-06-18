<template>
  <div class="page detail">
    <van-nav-bar title="商品详情" left-arrow @click-left="onBack" :border="false">
      <template #right>
        <span class="share-ico" @click="onShare">
          <svg viewBox="0 0 24 24" width="20" height="20"><path d="M18 8a3 3 0 1 0-2.83-4H15a3 3 0 0 0 .83 4H6a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8a2 2 0 0 0-2-2zM9 6a1.5 1.5 0 1 1 0-3 1.5 1.5 0 0 1 0 3zm6 0a1.5 1.5 0 1 1 0-3 1.5 1.5 0 0 1 0 3z" fill="currentColor" transform="rotate(0 12 12)"/><path d="M4 11l8 5 8-5" stroke="currentColor" stroke-width="1.6" fill="none" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </span>
      </template>
    </van-nav-bar>

    <div class="page-scroll" v-if="product">
      <!-- 图片轮播 -->
      <van-swipe class="img-swipe" :autoplay="0" indicator-color="#fff" @change="onSwipeChange">
        <van-swipe-item v-for="(img, i) in images" :key="i">
          <img class="swipe-img" :src="img" @error="onImgError" />
        </van-swipe-item>
      </van-swipe>
      <div class="img-index">{{ currentImg + 1 }}/{{ images.length }}</div>

      <div class="info-block">
        <!-- 标题 + 价格 -->
        <h1 class="title">{{ product.title }}</h1>
        <div class="price-row">
          <span class="price">¥{{ formatPrice(product.price) }}</span>
          <span class="price-tip">预估价</span>
        </div>

        <!-- 标签 -->
        <section v-if="product.tags && product.tags.length" class="section">
          <div class="section-title">标签</div>
          <div class="tags">
            <span class="tag" v-for="t in product.tags" :key="t">{{ t }}</span>
          </div>
        </section>

        <!-- 简介 -->
        <section v-if="product.brief" class="section">
          <div class="section-title">简介</div>
          <p class="section-text">{{ product.brief }}</p>
        </section>

        <!-- 详情 -->
        <section v-if="product.description" class="section">
          <div class="section-title">详情</div>
          <p class="section-text">{{ product.description }}</p>
        </section>
        <div style="height: 120px"></div>
      </div>
    </div>

    <!-- 底部联系卖家 -->
    <footer class="contact-bar" v-if="product">
      <div class="contact-title">联系卖家<span class="contact-tip">留下邮箱，卖家将通过邮件联系您</span></div>
      <div class="contact-field">
        <input v-model.trim="lead.email" class="c-input" placeholder="请输入您的联系邮箱" type="email" />
      </div>
      <div class="contact-field">
        <input v-model.trim="lead.message" class="c-input" placeholder="请输入留言，如预算、场景等" />
      </div>
      <button class="contact-btn" :disabled="submitting" @click="onSubmit">提交意向，等待卖家联系</button>
      <div class="contact-privacy">我们尊重您的隐私，仅用于卖家联系您</div>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { homeProductDetail } from '@/api/product'
import { submitLead } from '@/api/lead'

const route = useRoute()
const router = useRouter()

const product = ref(null)
const currentImg = ref(0)
const submitting = ref(false)
const lead = reactive({ email: '', message: '' })

const images = computed(() => (product.value?.images && product.value.images.length) ? product.value.images : [])

function onBack() { router.back() }
function onShare() { showToast('分享功能开发中') }
function onSwipeChange(i) { currentImg.value = i }
function onImgError(e) { e.target.style.background = 'var(--color-primary-light)' }

function formatPrice(p) { return p == null ? '--' : Number(p).toLocaleString('zh-CN') }

async function loadProduct() {
  try {
    product.value = await homeProductDetail(route.params.id)
  } catch (e) { /* 404 等已提示 */ }
}

async function onSubmit() {
  if (!lead.email) return showToast('请输入联系邮箱')
  if (!lead.message) return showToast('请输入留言')
  submitting.value = true
  try {
    await submitLead(route.params.id, lead.email, lead.message)
    showToast('提交成功，等待卖家联系您')
    lead.email = ''
    lead.message = ''
  } catch (e) { /* 已提示 */ } finally {
    submitting.value = false
  }
}

onMounted(loadProduct)
</script>

<style scoped>
.detail { background: #fff; }
.share-ico { display: flex; align-items: center; color: var(--color-text); }
.img-swipe { background: #000; height: 340px; }
.swipe-img { width: 100%; height: 340px; object-fit: contain; }
.img-index { position: absolute; right: 14px; margin-top: -32px; color: #fff; font-size: 12px; background: rgba(0,0,0,.4); padding: 2px 8px; border-radius: 10px; }

.info-block { padding: 16px; }
.title { font-size: 20px; font-weight: 700; color: var(--color-text); line-height: 1.4; }
.price-row { margin-top: 12px; display: flex; align-items: baseline; gap: 8px; }
.price { font-size: 26px; font-weight: 700; color: var(--color-danger); letter-spacing: -.5px; }
.price-tip { font-size: 12px; color: var(--color-text-placeholder); }

.section { margin-top: 22px; }
.section-title { font-size: 15px; font-weight: 600; color: var(--color-text); margin-bottom: 10px; border-left: 3px solid var(--color-primary); padding-left: 10px; line-height: 1.2; }
.section-text { font-size: 14px; color: var(--color-text-secondary); line-height: 1.7; white-space: pre-wrap; }
.tags { display: flex; flex-wrap: wrap; gap: 8px; }
.tag { font-size: 12px; color: var(--color-primary); background: var(--color-primary-light); padding: 4px 12px; border-radius: var(--radius-pill); }

.contact-bar {
  border-top: 1px solid var(--color-border); background: #fff;
  padding: 12px 16px calc(12px + env(safe-area-inset-bottom));
}
.contact-title { font-size: 15px; font-weight: 600; display: flex; align-items: baseline; gap: 8px; }
.contact-tip { font-size: 11px; color: var(--color-text-placeholder); font-weight: 400; }
.contact-field { margin-top: 8px; }
.c-input { width: 100%; height: 42px; border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 0 12px; font-size: 14px; outline: none; background: #fafbfc; transition: border-color .15s ease, background .15s ease; }
.c-input:focus { border-color: var(--color-primary); background: #fff; }
.contact-btn { width: 100%; height: 46px; margin-top: 12px; border: none; border-radius: var(--radius-md); background: var(--color-primary); color: #fff; font-size: 15px; font-weight: 500; box-shadow: var(--shadow-primary); transition: transform .12s ease, background .12s ease; }
.contact-btn:active { background: var(--color-primary-dark); transform: scale(.98); }
.contact-btn:disabled { opacity: .6; box-shadow: none; }
.contact-privacy { font-size: 11px; color: var(--color-text-placeholder); text-align: center; margin-top: 8px; }
</style>
