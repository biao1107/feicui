import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 首页聊天记录 (PRD: 启用 local storage, 强刷新才清空).
 * 消息结构: { role: 'user'|'ai', text, cards?: [] }
 */
export const useChatStore = defineStore('chat', () => {
  const messages = ref(JSON.parse(localStorage.getItem('fc_chat') || '[]'))

  function push(msg) {
    messages.value.push(msg)
    persist()
  }

  function clear() {
    messages.value = []
    localStorage.removeItem('fc_chat')
  }

  function persist() {
    localStorage.setItem('fc_chat', JSON.stringify(messages.value))
  }

  return { messages, push, clear }
})
