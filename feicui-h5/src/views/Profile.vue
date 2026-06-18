<template>
  <div class="page profile">
    <van-nav-bar title="个人中心" left-arrow @click-left="$router.push('/dashboard')" :border="false" />

    <div class="page-scroll">
      <!-- 卖家信息 -->
      <div class="seller-card" @click="go('/account')">
        <div class="avatar-wrap"><span class="avatar">👤</span></div>
        <div class="seller-info">
          <div class="seller-email">{{ auth.merchant?.email }}</div>
          <div class="seller-expire">有效期至 {{ expireText }}</div>
        </div>
        <span class="arrow">›</span>
      </div>

      <!-- 账户信息 -->
      <div class="cell-group">
        <div class="cell-hd" @click="toggle.accInfo = !toggle.accInfo">
          <span class="cell-ico">👤</span><span>账户信息</span>
          <span class="cell-arrow" :class="{ open: toggle.accInfo }">›</span>
        </div>
        <div v-if="toggle.accInfo" class="cell-body">
          VIP起止日 {{ vipRangeText }}
        </div>
      </div>

      <!-- 修改邮箱 -->
      <div class="cell-group">
        <div class="cell-hd" @click="toggle.email = !toggle.email">
          <span class="cell-ico">✉</span><span>修改邮箱</span>
          <span class="cell-arrow" :class="{ open: toggle.email }">›</span>
        </div>
        <div v-if="toggle.email" class="cell-body">
          <div class="email-current">当前邮箱 <b>{{ auth.merchant?.email }}</b></div>
          <div class="email-row">
            <input v-model.trim="newEmail" class="email-input" placeholder="修改邮箱" />
            <button class="text-btn" :disabled="emailCounting>0" @click="sendChangeCode">{{ emailCounting>0 ? emailCounting+'s' : '发送验证码' }}</button>
          </div>
          <div class="email-row">
            <input v-model.trim="newCode" class="email-input" placeholder="验证码" />
            <button class="text-btn" @click="saveEmail">保存</button>
          </div>
        </div>
      </div>

      <!-- 通知设置 -->
      <div class="cell-group">
        <div class="cell-hd" @click="toggle.notify = !toggle.notify">
          <span class="cell-ico">🔔</span><span>通知设置</span>
          <span class="cell-arrow" :class="{ open: toggle.notify }">›</span>
        </div>
        <div v-if="toggle.notify" class="cell-body">
          <label class="check-row">
            <input type="checkbox" checked disabled /> 有人感兴趣时网页内通知我
            <span class="check-tip">(默认开启)</span>
          </label>
          <label class="check-row">
            <input type="checkbox" v-model="emailNotify" /> 有人感兴趣时邮件通知我
          </label>
        </div>
      </div>

      <!-- 其它 -->
      <div class="cell-group">
        <div class="cell-hd"><span class="cell-ico">❓</span><span>帮助中心</span></div>
      </div>
      <div class="cell-group">
        <div class="cell-hd"><span class="cell-ico">ℹ</span><span>关于我们</span></div>
      </div>

      <button class="logout-btn" @click="onLogout">退出登录</button>
      <div style="height: 40px"></div>
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
import { ref, reactive, computed, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useAuthStore } from '@/stores/auth'
import { updateEmail, updateNotifySettings } from '@/api/merchant'
import { sendCode } from '@/api/auth'

const router = useRouter()
const auth = useAuthStore()

const active = ref(3)
const toggle = reactive({ accInfo: false, email: false, notify: false })
const newEmail = ref('')
const newCode = ref('')
const emailCounting = ref(0)
const emailNotify = ref((auth.merchant?.emailNotify ?? 1) === 1)
let timer = null

function go(p) { router.push(p) }
function onTab(i) { const map = ['/','/products','/dashboard','/profile']; if (map[i] !== '/profile') router.push(map[i]) }

const expireText = computed(() => auth.isVip ? fmtDate(auth.merchant?.vipExpireTime) : '永久')
const vipRangeText = computed(() => auth.isVip ? fmtDate(auth.merchant?.vipExpireTime) : '免费会员(永久)')
function fmtDate(d) { return d ? String(d).slice(0, 10) : '--' }

async function sendChangeCode() {
  if (!newEmail.value) return showToast('请输入新邮箱')
  try { await sendCode(newEmail.value); showToast('验证码已发送(见后端控制台)'); startCount() } catch(e){}
}
function startCount() { emailCounting.value = 60; timer = setInterval(()=>{ emailCounting.value--; if(emailCounting.value<=0) clearInterval(timer) }, 1000) }

async function saveEmail() {
  if (!newEmail.value) return showToast('请输入新邮箱')
  try {
    await updateEmail(newEmail.value, newCode.value || '0')
    auth.merchant = { ...auth.merchant, email: newEmail.value }
    localStorage.setItem('fc_merchant', JSON.stringify(auth.merchant))
    showToast('修改成功')
    newEmail.value = ''; newCode.value = ''
  } catch(e){}
}

watch(emailNotify, async (v) => {
  try { await updateNotifySettings(1, v ? 1 : 0) } catch(e){}
})

async function onLogout() {
  auth.clear()
  showToast('已退出')
  router.push('/')
}

onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.profile { background: var(--color-bg); }
.seller-card { display: flex; align-items: center; gap: 14px; background: #fff; margin: 12px; padding: 16px; border-radius: var(--radius-lg); box-shadow: var(--shadow-card); transition: transform .12s ease; }
.seller-card:active { transform: scale(.99); }
.avatar-wrap { width: 52px; height: 52px; border-radius: 50%; background: var(--color-primary-light); display: flex; align-items: center; justify-content: center; }
.avatar { font-size: 24px; }
.seller-info { flex: 1; }
.seller-email { font-size: 16px; font-weight: 600; }
.seller-expire { font-size: 12px; color: var(--color-text-placeholder); margin-top: 4px; }
.arrow { font-size: 20px; color: var(--color-text-placeholder); }

.cell-group { margin: 0 12px 10px; background: #fff; border-radius: var(--radius-md); overflow: hidden; }
.cell-hd { display: flex; align-items: center; padding: 16px; font-size: 15px; cursor: pointer; }
.cell-ico { margin-right: 10px; font-size: 18px; }
.cell-arrow { margin-left: auto; color: var(--color-text-placeholder); transition: transform .2s; }
.cell-arrow.open { transform: rotate(90deg); }
.cell-body { padding: 4px 16px 16px; font-size: 14px; color: var(--color-text-secondary); }
.email-current { margin-bottom: 10px; }
.email-row { display: flex; gap: 10px; margin-top: 8px; align-items: center; }
.email-input { flex: 1; height: 38px; border: 1px solid var(--color-border); border-radius: var(--radius-sm); padding: 0 10px; font-size: 14px; outline: none; background: #fafbfc; transition: border-color .15s ease, background .15s ease; }
.email-input:focus { border-color: var(--color-primary); background: #fff; }
.text-btn { background: none; border: none; color: var(--color-primary); font-size: 14px; white-space: nowrap; }
.text-btn:disabled { color: var(--color-text-placeholder); }
.check-row { display: flex; align-items: center; gap: 8px; padding: 8px 0; font-size: 14px; }
.check-tip { font-size: 11px; color: var(--color-text-placeholder); }

.logout-btn { width: calc(100% - 24px); margin: 16px 12px 0; height: 46px; border: 1px solid var(--color-border); background: #fff; border-radius: var(--radius-md); font-size: 15px; color: var(--color-danger); transition: transform .12s ease, background .12s ease; }
.logout-btn:active { transform: scale(.98); background: #fef2f2; }
</style>
