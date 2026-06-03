<template>
  <div class="page-container">
    <h2 class="page-title">🤖 AI记账助手</h2>
    <p class="page-subtitle">用自然语言记账和查询，试试输入"今天午餐花了36块"</p>

    <div class="chat-card">
      <!-- 快捷提示 -->
      <div class="quick-prompts" v-if="messages.length === 0">
        <span class="prompt-title">💡 试试这些：</span>
        <div class="prompt-chips">
          <span
            v-for="p in quickPrompts"
            :key="p"
            class="prompt-chip"
            @click="sendQuick(p)"
          >{{ p }}</span>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="chat-messages" ref="chatContainer">
        <div v-if="messages.length === 0" class="chat-empty">
          <span class="empty-icon">💬</span>
          <p>输入你想要记账或查询的内容，AI助手会自动帮你处理</p>
        </div>

        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          :class="['message-row', msg.role === 'user' ? 'message-user' : 'message-ai']"
        >
          <!-- AI 头像 -->
          <div v-if="msg.role === 'ai'" class="message-avatar">🤖</div>

          <div class="message-bubble-wrapper">
            <!-- 用户消息 -->
            <div v-if="msg.role === 'user'" class="message-bubble bubble-user">
              {{ msg.content }}
            </div>

            <!-- AI 消息 -->
            <div v-else class="message-bubble bubble-ai">
              <!-- 纯文本 / 错误 -->
              <div v-if="msg.intent === 'chat' || msg.intent === 'error'" class="ai-text">
                {{ msg.content }}
              </div>

              <!-- 添加记录确认卡片 -->
              <div v-else-if="msg.intent === 'add_record' && msg.data?.record" class="ai-record-card">
                <div class="record-card-header">
                  <span :class="['type-badge', msg.data.record.type === 'INCOME' ? 'type-income' : 'type-expense']">
                    {{ msg.data.record.type === 'INCOME' ? '收入' : '支出' }}
                  </span>
                  <span class="record-icon">{{ msg.data.record.categoryIcon }}</span>
                  <span class="record-category">{{ msg.data.record.categoryName }}</span>
                </div>
                <div class="record-amount">¥ {{ msg.data.record.amount.toFixed(2) }}</div>
                <div class="record-meta">
                  <span>👤 {{ msg.data.record.familyMember }}</span>
                  <span>📅 {{ msg.data.record.recordDate }}</span>
                  <span v-if="msg.data.record.note">📝 {{ msg.data.record.note }}</span>
                </div>
              </div>

              <!-- 查询统计卡片 -->
              <div v-else-if="msg.intent === 'query' && msg.data" class="ai-stats-card">
                <!-- 自然语言回复（始终显示） -->
                <div v-if="msg.content" class="ai-text query-nl-text">{{ msg.content }}</div>
                <!-- 汇总 -->
                <div v-if="msg.data.summary" class="stats-summary-row">
                  <div class="stat-item">
                    <span class="stat-label">收入</span>
                    <span class="stat-value income-value">¥ {{ msg.data.summary.totalIncome.toFixed(2) }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">支出</span>
                    <span class="stat-value expense-value">¥ {{ msg.data.summary.totalExpense.toFixed(2) }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">结余</span>
                    <span :class="['stat-value', msg.data.summary.balance >= 0 ? 'income-value' : 'expense-value']">
                      ¥ {{ msg.data.summary.balance.toFixed(2) }}
                    </span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">笔数</span>
                    <span class="stat-value">{{ msg.data.summary.recordCount }}</span>
                  </div>
                </div>

                <!-- 按类别 -->
                <div v-if="msg.data.categories" class="stats-category-list">
                  <div v-for="cat in msg.data.categories.slice(0, 8)" :key="cat.categoryId" class="cat-row">
                    <span>{{ cat.icon }} {{ cat.categoryName }}</span>
                    <span class="cat-amount">¥ {{ cat.total.toFixed(2) }}</span>
                    <span class="cat-pct">({{ cat.percentage.toFixed(1) }}%)</span>
                  </div>
                </div>

                <!-- 月度趋势 -->
                <div v-if="msg.data.trends" class="stats-trend-list">
                  <div v-for="t in msg.data.trends" :key="t.month" class="trend-row">
                    <span class="trend-month">{{ t.month }}</span>
                    <span class="trend-income">收 ¥{{ t.income.toFixed(0) }}</span>
                    <span class="trend-expense">支 ¥{{ t.expense.toFixed(0) }}</span>
                    <span :class="['trend-balance', (t.income - t.expense) >= 0 ? 'income-value' : 'expense-value']">
                      ¥{{ (t.income - t.expense).toFixed(0) }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- 文本回复（当有 content 但没有结构化数据时） -->
              <div v-else class="ai-text">{{ msg.content }}</div>
            </div>
          </div>

          <!-- 用户头像 -->
          <div v-if="msg.role === 'user'" class="message-avatar user-avatar-chat">
            <img v-if="userAvatarUrl" :src="userAvatarUrl" class="user-avatar-img" />
            <span v-else>{{ userAvatarEmoji }}</span>
          </div>
        </div>

        <!-- 加载中 -->
        <div v-if="loading" class="message-row message-ai">
          <div class="message-avatar">🤖</div>
          <div class="message-bubble bubble-ai typing-indicator">
            <span class="dot"></span><span class="dot"></span><span class="dot"></span>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="chat-input-area">
        <div class="input-row">
          <button
            v-if="voiceSupported"
            :class="['voice-btn', { 'voice-listening': isListening }]"
            @click="toggleVoice"
            :disabled="loading"
            :title="isListening ? '点击停止录音' : '语音输入'"
          >
            <span :class="{ 'pulse-mic': isListening }">🎤</span>
          </button>
          <el-input
            ref="voiceInputRef"
            v-model="inputMessage"
            :placeholder="isListening ? '正在聆听...' : '例如：今天午餐花了36块'"
            size="large"
            @keyup.enter="handleSend"
            :disabled="loading"
            clearable
            class="voice-input"
          >
            <template #append>
              <el-button
                type="primary"
                :icon="loading ? null : Promotion"
                @click="handleSend"
                :loading="loading"
                style="width:70px"
              >
                {{ loading ? '' : '发送' }}
              </el-button>
            </template>
          </el-input>
        </div>
        <p v-if="!voiceSupported" class="voice-hint">💡 使用 Chrome 或 Edge 浏览器可启用语音输入</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'
import { sendMessage } from '../api/ai'

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const chatContainer = ref(null)
const voiceInputRef = ref(null)

// 用户头像
const avatar = ref(localStorage.getItem('avatar') || '')
const userAvatarUrl = computed(() => {
  if (!avatar.value || avatar.value === 'male' || avatar.value === 'female') return null
  return '/uploads/avatars/' + avatar.value
})
const userAvatarEmoji = computed(() => {
  if (avatar.value === 'male') return '👨'
  if (avatar.value === 'female') return '👩'
  return '👤'
})

// ===== 语音输入 =====
const isListening = ref(false)
const voiceSupported = ref(false)
let recognition = null

const createRecognition = () => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  if (!SpeechRecognition) return null

  const rec = new SpeechRecognition()
  rec.lang = 'zh-CN'
  rec.interimResults = true
  rec.continuous = false

  rec.onresult = (event) => {
    let transcript = ''
    for (let i = event.resultIndex; i < event.results.length; i++) {
      // 只收集最终结果，忽略中间结果，避免重复插入
      if (event.results[i].isFinal) {
        transcript += event.results[i][0].transcript
      }
    }
    if (!transcript) return

    const inputEl = voiceInputRef.value?.$el?.querySelector('input') || voiceInputRef.value?.input
    if (inputEl) {
      const start = inputEl.selectionStart ?? inputMessage.value.length
      const end = inputEl.selectionEnd ?? inputMessage.value.length
      const before = inputMessage.value.slice(0, start)
      const after = inputMessage.value.slice(end)
      inputMessage.value = before + transcript + after
      nextTick(() => {
        const pos = start + transcript.length
        inputEl.setSelectionRange(pos, pos)
        inputEl.focus()
      })
    } else {
      inputMessage.value += transcript
    }
  }

  rec.onend = () => {
    isListening.value = false
    recognition = null
  }

  rec.onerror = (event) => {
    isListening.value = false
    recognition = null
    if (event.error === 'not-allowed') {
      ElMessage.warning('麦克风权限被拒绝，请在浏览器设置中允许访问麦克风')
    } else if (event.error === 'network') {
      ElMessage.warning('语音识别需要网络连接')
    }
  }

  return rec
}

const toggleVoice = () => {
  if (isListening.value) {
    if (recognition) {
      recognition.abort()
      recognition = null
    }
    isListening.value = false
    return
  }

  const rec = createRecognition()
  if (!rec) {
    ElMessage.warning('当前浏览器不支持语音识别，请使用 Chrome 或 Edge')
    return
  }
  recognition = rec
  try {
    rec.start()
    isListening.value = true
  } catch (e) {
    isListening.value = false
    recognition = null
  }
}

const quickPrompts = [
  '今天午餐花了36块',
  '这个月吃饭花了多少',
  '我们家这个月交通花了多少',
  '本月收支汇总',
  '打车50元',
  '发工资了8000'
]

const sendQuick = (msg) => {
  inputMessage.value = msg
  handleSend()
}

const handleSend = async () => {
  const msg = inputMessage.value.trim()
  if (!msg || loading.value) return
  inputMessage.value = ''

  messages.value.push({ role: 'user', content: msg })
  scrollToBottom()

  loading.value = true
  try {
    const res = await sendMessage(msg)
    messages.value.push({
      role: 'ai',
      content: res.data?.content || '收到！',
      intent: res.data?.intent || 'chat',
      data: res.data?.data || null
    })
  } catch (e) {
    messages.value.push({
      role: 'ai',
      content: '抱歉，AI服务暂时不可用，请稍后重试。',
      intent: 'error',
      data: null
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

onMounted(() => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  voiceSupported.value = !!SpeechRecognition
})

onUnmounted(() => {
  if (recognition) {
    recognition.abort()
    recognition = null
  }
})
</script>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 4px;
}
.page-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 20px;
}

/* 聊天卡片 */
.chat-card {
  background: var(--white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow);
  display: flex;
  flex-direction: column;
  height: calc(100vh - 210px);
  min-height: 450px;
  max-width: 750px;
  margin: 0 auto;
  border: 1px solid var(--border-light);
  overflow: hidden;
}

/* 快捷提示 */
.quick-prompts {
  padding: 14px 20px;
  border-bottom: 1px solid var(--border-light);
  background: linear-gradient(135deg, #FFFDF7, #FFF8DC);
}
.prompt-title {
  font-size: 12px;
  color: var(--text-secondary);
  margin-right: 8px;
  font-weight: 500;
}
.prompt-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}
.prompt-chip {
  display: inline-block;
  padding: 5px 14px;
  background: var(--white);
  border: 1.5px solid var(--accent-light);
  border-radius: 20px;
  font-size: 12px;
  color: var(--accent);
  cursor: pointer;
  transition: all var(--transition);
  font-weight: 500;
}
.prompt-chip:hover {
  background: var(--accent);
  color: white;
  border-color: var(--accent);
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  background: #fcfcf9;
}
.chat-empty {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}
.empty-icon {
  font-size: 56px;
  display: block;
  margin-bottom: 16px;
}

/* 消息行 */
.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  max-width: 85%;
}
.message-user {
  align-self: flex-end;
  flex-direction: row-reverse;
}
.message-ai {
  align-self: flex-start;
}

.message-avatar {
  font-size: 32px;
  flex-shrink: 0;
  line-height: 1;
  margin-top: 2px;
}

.user-avatar-chat {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  background: #f0ede0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.user-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.message-bubble-wrapper {
  min-width: 0;
}

/* 消息气泡 */
.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.65;
  word-break: break-word;
}
.bubble-user {
  background: linear-gradient(135deg, #FFA500, #FFB733);
  color: white;
  border-bottom-right-radius: 6px;
  box-shadow: 0 2px 8px rgba(255,165,0,0.25);
}
.bubble-ai {
  background: var(--white);
  color: var(--text-primary);
  border-bottom-left-radius: 6px;
  box-shadow: var(--shadow-sm);
  border: 1px solid #eee;
}

.ai-text {
  white-space: pre-wrap;
  font-size: 14px;
}
.query-nl-text {
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

/* 加载动画 */
.typing-indicator {
  display: flex;
  gap: 5px;
  padding: 16px 20px;
}
.dot {
  width: 9px;
  height: 9px;
  background: #ddd;
  border-radius: 50%;
  animation: bounce 1.4s infinite;
}
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-9px); }
}

/* 记录确认卡片 */
.ai-record-card {
  min-width: 200px;
}
.record-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.type-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  color: white;
  font-weight: 600;
}
.type-income { background: #67c23a; }
.type-expense { background: #f56c6c; }
.record-icon { font-size: 18px; }
.record-category { font-weight: 600; font-size: 15px; }
.record-amount {
  font-size: 24px;
  font-weight: 700;
  color: var(--accent, #FFA500);
  margin-bottom: 6px;
}
.record-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: #999;
}

/* 统计卡片 */
.stats-summary-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  min-width: 220px;
}
.stat-item {
  text-align: center;
  padding: 8px;
  background: white;
  border-radius: 8px;
  border: 1px solid #eee;
}
.stat-label { font-size: 11px; color: #999; display: block; }
.stat-value { font-size: 16px; font-weight: 700; }
.income-value { color: #67c23a; }
.expense-value { color: #f56c6c; }

.stats-category-list {
  min-width: 220px;
}
.cat-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  font-size: 13px;
  border-bottom: 1px solid #f0f0f0;
}
.cat-amount { font-weight: 600; margin-left: auto; }
.cat-pct { font-size: 11px; color: #999; }

.stats-trend-list {
  min-width: 260px;
}
.trend-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 3px 0;
  font-size: 12px;
}
.trend-month { font-weight: 600; width: 60px; }
.trend-income { color: #67c23a; }
.trend-expense { color: #f56c6c; }
.trend-balance { margin-left: auto; font-weight: 600; }

/* 输入区 */
.chat-input-area {
  padding: 14px 20px;
  border-top: 1px solid var(--border-light);
  background: var(--white);
}

.input-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.voice-btn {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  border: 2px solid #e8e5dc;
  background: #fafaf8;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition);
  flex-shrink: 0;
}
.voice-btn:hover {
  border-color: var(--accent);
  background: #FFF8DC;
  box-shadow: 0 2px 8px rgba(255,165,0,0.15);
}
.voice-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.voice-listening {
  border-color: #f56c6c;
  background: #fef0f0;
  animation: voice-pulse 1.5s infinite;
}
@keyframes voice-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.4); }
  50% { box-shadow: 0 0 0 14px rgba(245, 108, 108, 0); }
}

.pulse-mic {
  display: inline-block;
  animation: mic-bounce 0.5s infinite alternate;
}
@keyframes mic-bounce {
  from { transform: scale(1); }
  to { transform: scale(1.3); }
}

.voice-input {
  flex: 1;
}

.voice-hint {
  font-size: 11px;
  color: #ccc;
  margin: 8px 0 0 56px;
}

/* 响应式 */
@media (max-width: 768px) {
  .chat-card {
    height: calc(100vh - 150px);
    min-height: 350px;
    border-radius: 0;
    box-shadow: none;
    border: none;
  }
  .message-row { max-width: 90%; }
  .chat-messages { padding: 14px; }
  .chat-input-area { padding: 10px 14px; }
  .stats-summary-row { grid-template-columns: 1fr 1fr; }
}
</style>
