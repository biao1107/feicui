<template>
  <div class="page home">
    <!-- 顶部: LOGO + 商家入驻 -->
    <header class="home-header">
      <div class="logo">
        <span class="logo-icon">
          <svg viewBox="0 0 24 24" width="20" height="20"><path d="M12 3l7 6-7 12-7-12z" fill="#fff"/><path d="M5 9h14M12 3v18" stroke="#10A47A" stroke-width="1.1" stroke-linecap="round"/></svg>
        </span>
        <span class="logo-text">AI翡翠匹配</span>
      </div>
      <button class="entry-btn" @click="goEntry">商家入驻</button>
    </header>

    <!-- 聊天区 -->
    <main class="chat-area" ref="chatAreaRef">
      <!-- 空状态: 欢迎语 + 提示词 -->
      <template v-if="chatStore.messages.length === 0">
        <div class="welcome">
          <div class="avatar bot-avatar">
            <svg viewBox="0 0 24 24" width="26" height="26"><rect x="5" y="7" width="14" height="11" rx="3" fill="#fff"/><circle cx="9.5" cy="12" r="1.3" fill="#10A47A"/><circle cx="14.5" cy="12" r="1.3" fill="#10A47A"/><path d="M10 15.5c.7.5 1.3.7 2 .7s1.3-.2 2-.7" stroke="#10A47A" stroke-width="1.2" fill="none" stroke-linecap="round"/><path d="M12 4v3" stroke="#fff" stroke-width="1.4" stroke-linecap="round"/><circle cx="12" cy="3.5" r="1.1" fill="#fff"/></svg>
          </div>
          <div class="welcome-bubble">
            <p>您好！</p>
            <p>请说出您的翡翠需求（预算、品类、尺寸、品相），我将为您精准匹配货源~</p>
          </div>
        </div>
        <div class="chips">
          <button v-for="c in PROMPTS" :key="c" class="chip" @click="sendMessage(c)">{{ c }}</button>
        </div>
      </template>

      <!-- 有消息: 渲染对话 -->
      <template v-else>
        <div v-for="(m, i) in chatStore.messages" :key="i" :class="['msg-row', m.role]">
          <!-- AI 消息 -->
          <template v-if="m.role === 'ai'">
            <div class="avatar bot-avatar small">
              <svg viewBox="0 0 24 24" width="22" height="22"><rect x="5" y="7" width="14" height="11" rx="3" fill="#fff"/><circle cx="9.5" cy="12" r="1.3" fill="#10A47A"/><circle cx="14.5" cy="12" r="1.3" fill="#10A47A"/></svg>
            </div>
            <div class="ai-content">
              <div v-if="m.text" class="ai-bubble">{{ m.text }}</div>
              <!-- 翡翠卡片 -->
              <template v-if="m.cards && m.cards.length">
                <div class="cards-title">为您找到以下优质货源</div>
                <div class="card" v-for="card in m.cards" :key="card.id" @click="openProduct(card.id)">
                  <img class="card-img" :src="card.coverImage" alt="" @error="onImgError" />
                  <div class="card-info">
                    <div class="card-title ellipsis">{{ card.title }}</div>
                    <div class="card-tags">
                      <span class="tag" v-for="t in (card.tags || [])" :key="t">{{ t }}</span>
                    </div>
                    <div class="card-price-row">
                      <span class="card-price">¥{{ formatPrice(card.price) }}</span>
                      <span class="card-source">商家货源</span>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </template>

          <!-- 用户消息 -->
          <template v-else>
            <div class="user-bubble">{{ m.text }}</div>
            <div class="avatar user-avatar small">
              <svg viewBox="0 0 24 24" width="22" height="22"><circle cx="12" cy="9" r="3.5" fill="#fff"/><path d="M5 20c1-4 4-5.5 7-5.5s6 1.5 7 5.5" fill="#fff"/></svg>
            </div>
          </template>
        </div>

        <!-- 加载中 -->
        <div v-if="loading" class="msg-row ai">
          <div class="avatar bot-avatar small"><svg viewBox="0 0 24 24" width="22" height="22"><rect x="5" y="7" width="14" height="11" rx="3" fill="#fff"/><circle cx="9.5" cy="12" r="1.3" fill="#10A47A"/><circle cx="14.5" cy="12" r="1.3" fill="#10A47A"/></svg></div>
          <div class="ai-bubble typing">正在匹配优质翡翠货源<span class="dots"><i></i><i></i><i></i></span></div>
        </div>
      </template>
    </main>

    <!-- 底部输入区 -->
    <footer class="input-area">
      <div class="input-row">
        <textarea
          ref="taRef"
          v-model="input"
          class="input-box"
          :placeholder="'请输入您的翡翠需求...\n支持中文英文等多语言'"
          rows="2"
          @keydown.enter.exact.prevent="onEnter"
          @input="autoResize"
        ></textarea>
        <button class="send-btn" :class="{ active: input.trim() && !loading }" :disabled="!input.trim() || loading" @click="send">
          <svg class="send-ico" viewBox="0 0 24 24" width="15" height="15"><path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z" fill="currentColor"/></svg>
          AI匹配
        </button>
      </div>
      <div class="input-tip">AI智能匹配，仅供参考，不做鉴定与交易</div>
    </footer>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
import { aiMatch } from '@/api/ai'

const router = useRouter()
const chatStore = useChatStore()
const auth = useAuthStore()

const input = ref('')
const loading = ref(false)
const chatAreaRef = ref(null)
const taRef = ref(null)

// PRD 固定的三条提示词
const PROMPTS = ['10万预算 帝王绿手镯', '冰种平安扣 预算2万 无纹无裂', '冰种翡翠吊坠 送礼自用均可']

function goEntry() {
  router.push(auth.isLogin ? '/dashboard' : '/login')
}

function onEnter() {
  // PRD: 回车=换行, 不发送
  input.value += '\n'
  autoResize()
}

function autoResize() {
  const ta = taRef.value
  if (!ta) return
  ta.style.height = 'auto'
  // 限制最大 6 行高度
  ta.style.height = Math.min(ta.scrollHeight, 120) + 'px'
}

async function send() {
  const text = input.value.trim()
  if (!text || loading.value) return
  input.value = ''
  nextTick(() => { if (taRef.value) taRef.value.style.height = 'auto' })
  await sendMessage(text)
}

async function sendMessage(text) {
  chatStore.push({ role: 'user', text })
  loading.value = true
  await scrollBottom()
  try {
    const res = await aiMatch(text)
    chatStore.push({ role: 'ai', text: res.reply, cards: res.products || [] })
  } catch (e) {
    chatStore.push({ role: 'ai', text: '抱歉，匹配服务暂时不可用，请稍后再试。' })
  } finally {
    loading.value = false
    await scrollBottom()
  }
}

function openProduct(id) {
  router.push(`/product/${id}`)
}

function onImgError(e) {
  e.target.style.display = 'none'
}

function formatPrice(p) {
  if (p == null) return '--'
  return Number(p).toLocaleString('zh-CN')
}

async function scrollBottom() {
  await nextTick()
  if (chatAreaRef.value) {
    chatAreaRef.value.scrollTop = chatAreaRef.value.scrollHeight
  }
}

onMounted(() => scrollBottom())
</script>

<style scoped>
.home { background: var(--color-bg); }

/* 顶部 */
.home-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 16px; background: #fff;
  border-bottom: 1px solid var(--color-border);
}
.logo { display: flex; align-items: center; gap: 8px; }
.logo-icon {
  width: 30px; height: 30px; border-radius: 8px; display: flex;
  align-items: center; justify-content: center;
  background: linear-gradient(135deg, #10A47A, #0D8C66);
  box-shadow: 0 2px 6px rgba(16, 164, 122, 0.3);
}
.logo-text { font-size: 17px; font-weight: 700; color: var(--color-text); letter-spacing: 0.5px; }
.entry-btn {
  background: var(--color-primary); color: #fff;
  border: none; border-radius: 999px;
  padding: 6px 16px; font-size: 13px; font-weight: 500;
}

/* 聊天区 */
.chat-area { flex: 1; overflow-y: auto; padding: 16px 12px; -webkit-overflow-scrolling: touch; }

/* 欢迎语 */
.welcome { display: flex; gap: 8px; align-items: flex-start; margin-bottom: 16px; }
.welcome-bubble {
  background: #fff; border-radius: 0 12px 12px 12px;
  padding: 12px 14px; max-width: 80%; font-size: 14px; line-height: 1.6;
}
.welcome-bubble p { margin: 0; }

.chips { display: flex; flex-direction: column; gap: 10px; padding-left: 40px; }
.chip {
  align-self: flex-start;
  background: #fff; border: 1px solid var(--color-primary);
  color: var(--color-primary); border-radius: 999px;
  padding: 8px 16px; font-size: 13px;
}

/* 消息行 */
.msg-row { display: flex; gap: 8px; margin-bottom: 16px; align-items: flex-start; }
.msg-row.user { justify-content: flex-end; }
.avatar { flex-shrink: 0; border-radius: 50%; overflow: hidden; display: flex; align-items: center; justify-content: center; }
.bot-avatar { background: var(--color-primary); width: 40px; height: 40px; }
.bot-avatar.small { width: 32px; height: 32px; }
.user-avatar { background: var(--color-primary); }
.user-avatar.small { width: 32px; height: 32px; }

.ai-content { max-width: 78%; display: flex; flex-direction: column; gap: 8px; }
.ai-bubble {
  background: #fff; border-radius: 0 12px 12px 12px;
  padding: 10px 14px; font-size: 14px; line-height: 1.6; white-space: pre-wrap; word-break: break-word;
  align-self: flex-start;
}
.user-bubble {
  background: var(--color-primary); color: #fff;
  border-radius: 12px 2px 12px 12px; /* 右上角圆角较小 */
  padding: 10px 14px; font-size: 14px; line-height: 1.6; max-width: 78%;
  white-space: pre-wrap; word-break: break-word;
}

/* 翡翠卡片 */
.cards-title { font-size: 14px; font-weight: 600; color: var(--color-text); margin-top: 4px; }
.card {
  display: flex; gap: 10px; background: #fff; border-radius: 12px;
  padding: 10px; align-items: center; cursor: pointer;
  box-shadow: 0 2px 10px rgba(16, 164, 122, 0.08);
  transition: transform .15s;
}
.card:active { transform: scale(0.98); }
.card-img { width: 80px; height: 80px; border-radius: 8px; object-fit: cover; background: var(--color-primary-light); flex-shrink: 0; }
.card-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 6px; }
.card-title { font-size: 14px; font-weight: 600; }
.card-tags { display: flex; flex-wrap: wrap; gap: 4px; }
.tag { font-size: 11px; color: var(--color-primary); background: var(--color-primary-light); padding: 2px 6px; border-radius: 4px; }
.card-price-row { display: flex; align-items: baseline; gap: 6px; }
.card-price { font-size: 16px; font-weight: 700; color: var(--color-primary); }
.card-source { font-size: 10px; color: var(--color-text-placeholder); }

/* 加载动画 */
.typing { display: flex; align-items: center; gap: 6px; }
.dots { display: inline-flex; gap: 3px; }
.dots i { width: 5px; height: 5px; background: var(--color-primary); border-radius: 50%; display: inline-block; animation: blink 1.2s infinite; }
.dots i:nth-child(2) { animation-delay: .2s; }
.dots i:nth-child(3) { animation-delay: .4s; }
@keyframes blink { 0%,80%,100% { opacity: .3; } 40% { opacity: 1; } }

/* 底部输入 */
.input-area { background: #fff; border-top: 1px solid var(--color-border); padding: 10px 12px 8px; }
.input-row { display: flex; gap: 8px; align-items: flex-end; }
.input-box {
  flex: 1; resize: none; border: 1px solid var(--color-border); border-radius: 10px;
  padding: 8px 12px; font-size: 14px; line-height: 1.5; min-height: 40px; max-height: 120px;
  font-family: inherit; background: #f9fafb; outline: none;
}
.input-box:focus { border-color: var(--color-primary); }
.send-btn {
  background: #c8d4cf; color: #fff; border: none; border-radius: 10px;
  padding: 0 16px; height: 40px; font-size: 14px; font-weight: 500; white-space: nowrap;
  display: inline-flex; align-items: center; gap: 4px;
}
.send-ico { display: inline-block; transform: translateY(-1px); }
.send-btn.active { background: var(--color-primary); }
.send-btn.active:active { background: var(--color-primary-dark); }
.input-tip { font-size: 11px; color: var(--color-text-placeholder); margin-top: 6px; text-align: center; }
</style>
