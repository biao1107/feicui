<template>
  <div class="page login">
    <!-- 返回 -->
    <van-nav-bar left-arrow @click-left="onBack" :border="false" />

    <div class="login-body">
      <!-- Logo -->
      <div class="logo-circle">
        <svg viewBox="0 0 24 24" width="38" height="38"><path d="M12 4l7 6-7 10-7-10z" fill="#fff"/><path d="M5 10h14M12 4v16" stroke="#10A47A" stroke-width="1.2" stroke-linecap="round"/></svg>
      </div>
      <h1 class="title">商家入驻</h1>
      <p class="subtitle">加入高翠网 · 获取精准买家线索</p>

      <!-- 表单 -->
      <div class="form">
        <div class="field">
          <span class="field-icon">✉</span>
          <input v-model.trim="email" class="field-input" placeholder="请输入您的邮箱地址" type="email" />
        </div>
        <div class="field">
          <span class="field-icon">🛡</span>
          <input v-model.trim="code" class="field-input code-input" placeholder="请输入验证码" inputmode="numeric" />
          <button class="code-btn" :disabled="counting > 0" @click="onGetCode">
            {{ counting > 0 ? counting + 's' : '获取验证码' }}
          </button>
        </div>

        <button class="submit-btn" :disabled="loading" @click="onLogin">登录/注册</button>
      </div>

      <!-- 特性 -->
      <div class="features">
        <div class="feat"><span class="feat-ico">✉</span><span>仅需邮箱</span></div>
        <div class="feat"><span class="feat-ico">🔒</span><span>验证码登录</span></div>
        <div class="feat"><span class="feat-ico">⚡</span><span>快速入驻</span></div>
        <div class="feat"><span class="feat-ico">✓</span><span>免费试用</span></div>
      </div>
    </div>

    <p class="agreement">登录即表示同意《平台服务协议》与《隐私政策》</p>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { sendCode, login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const email = ref('')
const code = ref('')
const counting = ref(0)
const loading = ref(false)
let timer = null

function onBack() {
  router.push('/')
}

async function onGetCode() {
  if (!email.value) return showToast('请输入邮箱')
  try {
    await sendCode(email.value)
    showToast({ message: '验证码已发送(开发期见后端控制台)', duration: 2500 })
    counting.value = 60
    timer = setInterval(() => {
      counting.value--
      if (counting.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) { /* 拦截器已提示 */ }
}

async function onLogin() {
  if (!email.value) return showToast('请输入邮箱')
  if (!code.value) return showToast('请输入验证码')
  loading.value = true
  try {
    const data = await login(email.value, code.value)
    auth.setLogin(data)
    showToast('登录成功')
    // 优先跳 redirect, 否则进后台
    router.replace(route.query.redirect || '/dashboard')
  } catch (e) { /* 拦截器已提示 */ } finally {
    loading.value = false
  }
}

onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.login { background: #fff; }
.login-body { flex: 1; overflow-y: auto; padding: 8px 28px 0; display: flex; flex-direction: column; align-items: center; }
.logo-circle {
  width: 72px; height: 72px; border-radius: 50%; background: linear-gradient(135deg, #10A47A, #0D8C66);
  display: flex; align-items: center; justify-content: center; margin-top: 12px;
  box-shadow: 0 6px 18px rgba(16, 164, 122, 0.3);
}
.title { font-size: 26px; color: var(--color-text); margin-top: 18px; font-weight: 500; }
.subtitle { font-size: 13px; color: var(--color-text-placeholder); margin-top: 6px; }

.form { width: 100%; margin-top: 36px; display: flex; flex-direction: column; gap: 16px; }
.field {
  display: flex; align-items: center; border: 1px solid var(--color-border);
  border-radius: 10px; padding: 0 12px; height: 48px; background: #fafbfc;
}
.field:focus-within { border-color: var(--color-primary); }
.field-icon { font-size: 18px; color: var(--color-text-placeholder); margin-right: 8px; }
.field-input { flex: 1; border: none; outline: none; background: transparent; font-size: 15px; height: 100%; }
.code-input { max-width: 55%; }
.code-btn {
  background: none; border: none; color: var(--color-primary); font-size: 14px;
  white-space: nowrap; padding-left: 8px; border-left: 1px solid var(--color-border);
}
.code-btn:disabled { color: var(--color-text-placeholder); }

.submit-btn {
  height: 48px; border: none; border-radius: 10px;
  background: var(--color-primary); color: #fff; font-size: 16px; font-weight: 500;
  margin-top: 8px; box-shadow: 0 4px 14px rgba(16, 164, 122, 0.28);
}
.submit-btn:active { background: var(--color-primary-dark); }
.submit-btn:disabled { opacity: .7; }

.features { display: flex; justify-content: space-between; width: 100%; margin-top: 36px; }
.feat { display: flex; flex-direction: column; align-items: center; gap: 6px; flex: 1; }
.feat-ico {
  width: 36px; height: 36px; border-radius: 50%; background: #f0f1f3; color: #8a929c;
  display: flex; align-items: center; justify-content: center; font-size: 16px;
}
.feat span:last-child { font-size: 11px; color: var(--color-text-secondary); }

.agreement { text-align: center; font-size: 11px; color: var(--color-text-placeholder); padding: 16px; }
</style>
