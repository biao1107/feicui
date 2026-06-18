<template>
  <div class="page publish-edit">
    <van-nav-bar title="发布商品" left-arrow @click-left="onBack" :border="false" />

    <div class="page-scroll">
      <!-- 图片预览 -->
      <div class="img-wrap" v-if="images.length">
        <van-swipe class="img-swipe" @change="(i)=>curImg=i" indicator-color="#fff">
          <van-swipe-item v-for="(img, i) in images" :key="i">
            <img class="swipe-img" :src="img" @error="(e)=>e.target.style.background='var(--color-primary-light)'" />
          </van-swipe-item>
        </van-swipe>
        <div class="img-index">{{ curImg + 1 }}/{{ images.length }}</div>
        <button v-if="isEdit" class="replace-img" @click="replaceImg">替换图片</button>
      </div>

      <!-- 编辑字段 -->
      <div class="form">
        <div class="f-row"><label>标题</label><input v-model="fields.title" placeholder="商品标题" /></div>
        <div class="f-row"><label>简介</label><input v-model="fields.brief" placeholder="一句话简介" /></div>
        <div class="f-row textarea"><label>详情</label><textarea v-model="fields.description" rows="4" placeholder="商品详情"></textarea></div>
        <div class="f-row"><label>售价(¥)</label><input v-model="fields.price" type="number" placeholder="预估售价" /></div>

        <div class="f-row tags-row"><label>标签</label></div>
        <div class="tags">
          <span class="tag" v-for="(t, i) in fields.tags" :key="i">{{ t }}<i @click="fields.tags.splice(i, 1)">×</i></span>
          <label class="tag-add">＋ 添加标签
            <input v-model="tagInput" @keyup.enter="addTag" />
          </label>
        </div>
      </div>
      <div style="height: 96px"></div>
    </div>

    <div class="bottom">
      <template v-if="isEdit">
        <button class="btn-outline" @click="toggleStatus">{{ statusBtnText }}</button>
        <button class="btn-primary" @click="onSave">保存修改</button>
      </template>
      <template v-else>
        <button class="btn-primary block" :disabled="saving" @click="onPublish">{{ saving ? '发布中...' : '确认发布' }}</button>
      </template>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useDraftStore } from '@/stores/publishDraft'
import { createDraft, updateProduct, publishProduct, productDetail, changeStatus, uploadImage } from '@/api/product'

const router = useRouter()
const draft = useDraftStore()

const fields = reactive({ title: '', brief: '', description: '', price: null, tags: [], aiGenerated: 0 })
const images = ref([])
const editId = ref(draft.editId)
const isEdit = computed(() => !!editId.value)
const currentStatus = ref('LISTED')
const curImg = ref(0)
const tagInput = ref('')
const saving = ref(false)

const statusBtnText = computed(() => currentStatus.value === 'LISTED' ? '下架商品' : '上架商品')

function onBack() { router.back() }

function addTag() {
  const t = tagInput.value.trim()
  if (t && !fields.tags.includes(t)) fields.tags.push(t)
  tagInput.value = ''
}

async function loadForEdit() {
  if (isEdit.value) {
    try {
      const p = await productDetail(editId.value)
      Object.assign(fields, { title: p.title, brief: p.brief, description: p.description, price: p.price, tags: p.tags || [], aiGenerated: p.aiGenerated })
      images.value = p.images || []
      currentStatus.value = p.status
    } catch (e) {}
  } else if (draft.images.length) {
    // 来自上传页的草稿
    Object.assign(fields, draft.fields)
    images.value = [...draft.images]
  } else {
    showToast('请先上传图片')
    router.replace('/publish')
  }
}

async function onSave() {
  try {
    await updateProduct(editId.value, { ...fields, images: images.value })
    showToast('保存成功')
    router.replace('/products')
  } catch (e) {}
}

async function onPublish() {
  if (!fields.title) return showToast('请填写标题')
  saving.value = true
  try {
    const r = await createDraft({ ...fields, images: images.value })
    await publishProduct(r.id)
    showToast('发布成功')
    draft.reset()
    router.replace('/products')
  } catch (e) { /* 额度满等已提示 */ } finally {
    saving.value = false
  }
}

async function toggleStatus() {
  const target = currentStatus.value === 'LISTED' ? 'DELISTED' : 'LISTED'
  try {
    await changeStatus(editId.value, target)
    currentStatus.value = target
    showToast(target === 'LISTED' ? '已上架' : '已下架')
  } catch (e) {}
}

async function replaceImg() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = async () => {
    const f = input.files?.[0]
    if (!f) return
    try {
      const r = await uploadImage(f)
      images.value[curImg.value] = r.url
      showToast('已替换')
    } catch (e) {}
  }
  input.click()
}

onMounted(loadForEdit)
</script>

<style scoped>
.publish-edit { background: #fff; }
.img-wrap { position: relative; }
.img-swipe { background: #000; height: 320px; }
.swipe-img { width: 100%; height: 320px; object-fit: contain; }
.img-index { position: absolute; right: 14px; bottom: 14px; color: #fff; font-size: 12px; background: rgba(0,0,0,.4); padding: 2px 8px; border-radius: 10px; }
.replace-img { position: absolute; left: 14px; bottom: 14px; background: rgba(0,0,0,.4); color: #fff; border: none; border-radius: 6px; padding: 4px 12px; font-size: 12px; }

.form { padding: 16px; }
.f-row { display: flex; align-items: center; padding: 10px 0; border-bottom: 1px solid var(--color-border); }
.f-row label { width: 72px; color: var(--color-text-secondary); font-size: 14px; flex-shrink: 0; }
.f-row input { flex: 1; border: none; outline: none; font-size: 15px; background: transparent; }
.f-row.textarea { align-items: flex-start; }
.f-row.textarea textarea { flex: 1; border: none; outline: none; font-size: 15px; resize: none; background: transparent; font-family: inherit; }
.tags-row { border-bottom: none; }

.tags { display: flex; flex-wrap: wrap; gap: 8px; padding-bottom: 12px; }
.tag { background: var(--color-primary-light); color: var(--color-primary); font-size: 13px; padding: 4px 8px 4px 12px; border-radius: 14px; display: inline-flex; align-items: center; gap: 4px; }
.tag i { font-style: normal; cursor: pointer; opacity: .6; }
.tag-add { border: 1px dashed var(--color-text-placeholder); color: var(--color-text-placeholder); font-size: 13px; padding: 4px 12px; border-radius: 14px; }
.tag-add input { width: 60px; border: none; outline: none; background: transparent; font-size: 13px; }

.bottom { display: flex; gap: 10px; padding: 12px 16px calc(12px + env(safe-area-inset-bottom)); border-top: 1px solid var(--color-border); background: #fff; }
.btn-primary { flex: 1; height: 46px; border: none; border-radius: 10px; background: var(--color-primary); color: #fff; font-size: 15px; }
.btn-primary.block { flex: 1; }
.btn-primary:active { background: var(--color-primary-dark); }
.btn-outline { flex: 1; height: 46px; border: 1px solid var(--color-primary); background: #fff; color: var(--color-primary); border-radius: 10px; font-size: 15px; }
</style>
