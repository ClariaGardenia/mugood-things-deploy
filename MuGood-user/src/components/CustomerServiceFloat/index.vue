<script setup>
import { nextTick, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { streamCustomerAgentAPI } from '@/apis/agent'
import agentBotImage from '@/assets/images/agent-bot.png'

const HISTORY_KEY = 'mugood-agent-history'
const GOODS_KEY = 'mugood-agent-goods'
const SESSION_KEY = 'mugood-agent-session-id'
const defaultMessages = [
  {
    role: 'assistant',
    content: '你好，我是 MuGood 智能客服。告诉我身高、性别、场景或预算，我会从商品库里帮你挑。'
  }
]

const router = useRouter()
const visible = ref(false)
const loading = ref(false)
const input = ref('')
const panelRef = ref(null)
const bodyRef = ref(null)
const sessionId = ref(loadSessionId())
const messages = ref(normalizeMessages(loadJson(HISTORY_KEY, defaultMessages)))
const goods = ref(loadJson(GOODS_KEY, []))

watch(messages, value => {
  persistMessages(value)
  scrollToBottom()
}, { deep: true })

watch(goods, value => {
  localStorage.setItem(GOODS_KEY, JSON.stringify(value))
  scrollToBottom()
}, { deep: true })

const togglePanel = async () => {
  visible.value = !visible.value
  if (visible.value) {
    await nextTick()
    panelRef.value?.querySelector('textarea')?.focus()
    scrollToBottom()
  }
}

const closePanel = () => {
  visible.value = false
}

const send = async () => {
  const text = input.value.trim()
  if (!text || loading.value) return

  goods.value = []
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  await nextTick()
  scrollToBottom()

  messages.value.push({ role: 'assistant', content: '' })
  const assistantIndex = messages.value.length - 1

  try {
    await streamCustomerAgentAPI(text, sessionId.value, {
      async onToken(token) {
        await typeText(assistantIndex, token)
      },
      async onError(message) {
        await typeText(assistantIndex, message)
      },
      async onGoods(items) {
        goods.value = items
      },
      async onDone() {
        if (!messages.value[assistantIndex]?.content) {
          updateMessageContent(assistantIndex, '没有收到模型输出，请检查后端模型配置。')
        }
      }
    })
  } catch (error) {
    messages.value.splice(assistantIndex, 1)
    ElMessage.error('智能客服请求失败，请查看后端日志')
  } finally {
    persistMessages(messages.value)
    loading.value = false
    scrollToBottom()
  }
}

const clearHistory = () => {
  messages.value = [...defaultMessages]
  goods.value = []
  sessionId.value = createSessionId()
  localStorage.setItem(SESSION_KEY, sessionId.value)
}

const goDetail = (item) => {
  closePanel()
  router.push(item.detailPath || `/detail/${item.id}`)
}

function updateMessageContent(index, text) {
  const target = messages.value[index]
  if (!target) return
  messages.value.splice(index, 1, {
    ...target,
    content: `${target.content || ''}${text}`
  })
}

async function typeText(index, text) {
  if (!text) return

  for (let offset = 0; offset < text.length; offset += 2) {
    updateMessageContent(index, text.slice(offset, offset + 2))
    scrollToBottom()
    await sleep(24)
  }
}

function sleep(timeout) {
  return new Promise(resolve => window.setTimeout(resolve, timeout))
}

function scrollToBottom() {
  nextTick(() => {
    if (bodyRef.value) {
      bodyRef.value.scrollTop = bodyRef.value.scrollHeight
    }
  })
}

function loadJson(key, fallback) {
  try {
    const value = JSON.parse(localStorage.getItem(key) || 'null')
    return value || fallback
  } catch (error) {
    return fallback
  }
}

function normalizeMessages(value) {
  if (!Array.isArray(value)) return [...defaultMessages]

  const normalized = value.filter(item => {
    if (!item || !item.role) return false
    if (item.role === 'assistant') return Boolean(item.content?.trim())
    return Boolean(item.content?.trim())
  })

  return normalized.length ? normalized : [...defaultMessages]
}

function persistMessages(value) {
  localStorage.setItem(HISTORY_KEY, JSON.stringify(normalizeMessages(value)))
}

function loadSessionId() {
  const current = localStorage.getItem(SESSION_KEY)
  if (current) return current
  const created = createSessionId()
  localStorage.setItem(SESSION_KEY, created)
  return created
}

function createSessionId() {
  return `agent-${Date.now()}-${Math.random().toString(16).slice(2)}`
}
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="agent-mask" @click="closePanel"></div>
    <button class="agent-float" type="button" @click.stop="togglePanel" aria-label="智能客服">
      <img :src="agentBotImage" alt="智能客服">
    </button>
    <section v-if="visible" ref="panelRef" class="agent-panel" @click.stop>
      <header class="agent-header">
        <div>
          <strong>智能客服</strong>
          <p>商品推荐 · 搭配建议 · 快速购买</p>
        </div>
        <button type="button" @click="clearHistory">清空</button>
        <button type="button" @click="closePanel">×</button>
      </header>

      <div ref="bodyRef" class="agent-body">
        <div
          v-for="(message, index) in messages"
          :key="index"
          class="agent-message"
          :class="message.role"
        >
          {{ message.content }}
          <span v-if="loading && index === messages.length - 1" class="typing">▍</span>
        </div>

        <div v-if="goods.length" class="agent-goods">
          <button
            v-for="item in goods"
            :key="item.id"
            type="button"
            class="agent-goods-card"
            @click="goDetail(item)"
          >
            <img :src="item.picture" :alt="item.name">
            <span class="name">{{ item.name }}</span>
            <span class="desc">{{ item.desc }}</span>
            <span class="price">¥{{ item.price }}</span>
          </button>
        </div>
      </div>

      <footer class="agent-input">
        <el-input
          v-model="input"
          type="textarea"
          :rows="2"
          resize="none"
          placeholder="请输入您的要求；例如：本人男，170，帮我选一个短袖"
          @keydown.enter.exact.prevent="send"
        />
        <el-button type="primary" :loading="loading" @click="send">发送</el-button>
      </footer>
    </section>
  </Teleport>
</template>

<style scoped lang="scss">
.agent-mask {
  position: fixed;
  inset: 0;
  z-index: 998;
  background: transparent;
}

.agent-float {
  position: fixed;
  right: 28px;
  bottom: 88px;
  z-index: 1000;
  width: 72px;
  height: 72px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 12px 30px rgb(39 186 155 / 35%);
  cursor: pointer;
  transition: transform .18s ease, box-shadow .18s ease;

  &:hover {
    transform: translateY(-2px) scale(1.04);
    box-shadow: 0 16px 36px rgb(39 186 155 / 42%);
  }

  img {
    width: 100%;
    height: 100%;
    display: block;
    object-fit: cover;
  }
}

.agent-panel {
  position: fixed;
  right: 28px;
  bottom: 158px;
  z-index: 1000;
  width: 460px;
  max-width: calc(100vw - 32px);
  height: 600px;
  max-height: calc(100vh - 190px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 20px 60px rgb(0 0 0 / 18%);
}

.agent-header {
  height: 72px;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  background: #1f7f71;

  div {
    flex: 1;
  }

  strong {
    display: block;
    font-size: 18px;
  }

  p {
    margin-top: 4px;
    font-size: 12px;
    color: rgb(255 255 255 / 78%);
  }

  button {
    height: 32px;
    border: 0;
    border-radius: 16px;
    color: #fff;
    background: rgb(255 255 255 / 16%);
    padding: 0 10px;
    cursor: pointer;
  }
}

.agent-body {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: #f6f8f7;
}

.agent-message {
  width: fit-content;
  max-width: 88%;
  margin-bottom: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  line-height: 1.6;
  white-space: pre-wrap;

  &.assistant {
    color: #333;
    background: #fff;
  }

  &.user {
    margin-left: auto;
    color: #fff;
    background: #27ba9b;
  }
}

.typing {
  color: #27ba9b;
}

.agent-goods {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
  margin-top: 10px;
}

.agent-goods-card {
  display: grid;
  grid-template-columns: 72px 1fr;
  grid-template-rows: auto auto auto;
  column-gap: 10px;
  padding: 10px;
  border: 1px solid #e8eeee;
  border-radius: 8px;
  background: #fff;
  text-align: left;
  cursor: pointer;

  img {
    grid-row: 1 / 4;
    width: 72px;
    height: 72px;
    object-fit: cover;
    border-radius: 6px;
    background: #f2f2f2;
  }

  .name {
    color: #222;
    font-weight: 600;
  }

  .desc {
    margin-top: 4px;
    overflow: hidden;
    color: #777;
    font-size: 12px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .price {
    margin-top: 8px;
    color: #cf4444;
    font-weight: 700;
  }
}

.agent-input {
  padding: 12px;
  display: flex;
  gap: 10px;
  border-top: 1px solid #eef1f0;
  background: #fff;

  .el-button {
    align-self: stretch;
  }
}

@media (max-width: 520px) {
  .agent-float {
    right: 18px;
    bottom: 72px;
  }

  .agent-panel {
    right: 12px;
    bottom: 140px;
    height: 520px;
  }
}
</style>
