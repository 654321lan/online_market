<template>
  <div 
    v-if="isVisible"
    class="floating-chat" 
    :class="{ 
      'minimized': isMinimized,
      'dragging': isDragging,
      'resizing': isResizing
    }"
    :style="{ left: position.x + 'px', top: position.y + 'px', width: size.width + 'px', height: size.height + 'px' }"
  >
    <el-card>
      <template #header>
        <div class="chat-header" @mousedown="startDrag">
          <div class="header-title">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ shopName || '客服聊天' }}</span>
          </div>
          <div class="header-actions">
            <el-button @click="toggleMinimize" circle size="small">
              <el-icon v-if="!isMinimized"><Minus /></el-icon>
              <el-icon v-else><Plus /></el-icon>
            </el-button>
            <el-button @click="closeChat" circle size="small">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="chat-content">
        <div v-if="!currentSession && !pendingSession" class="no-session">
          <p>点击发送第一条消息开始聊天...</p>
        </div>
        <div v-else>
          <div class="messages-area" ref="messagesArea">
            <div v-if="messages.length === 0" class="no-messages">
              <p>暂无消息，开始聊天吧</p>
            </div>
            <div v-else>
              <div v-for="message in messages" :key="message.id" :class="['message', getMessageClass(message)]">
                <div class="message-content">{{ message.content }}</div>
                <div class="message-time">{{ formatTime(message.createTime) }}</div>
              </div>
            </div>
          </div>
          
          <div v-if="currentSession || pendingSession" class="input-area">
            <div v-if="sessionTypeRef === 2" class="quick-questions">
              <div class="quick-question-title">快捷问题：</div>
              <div class="quick-question-buttons">
                <el-button 
                  v-for="question in quickQuestions" 
                  :key="question.type"
                  size="small" 
                  type="primary"
                  plain
                  @click="sendQuickQuestion(question)"
                >
                  {{ question.label }}
                </el-button>
              </div>
            </div>
            
            <el-input
              v-model="messageInput"
              type="textarea"
              :rows="1"
              placeholder="输入消息..."
              @keyup.ctrl.enter="sendMessage"
            />
            <div class="input-actions">
              <el-button type="primary" @click="sendMessage" :loading="sending">发送 (Ctrl+Enter)</el-button>
            </div>
          </div>
        </div>
      </div>
      <div class="resize-handle" @mousedown="startResize"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Minus, Plus, Close } from '@element-plus/icons-vue'
import axios from 'axios'
import { useUserStore } from '@/stores/user'
import eventBus from '@/utils/eventBus'

const API_BASE = 'http://localhost:8080/api/chat'
const userStore = useUserStore()

const isVisible = ref(false)
const isMinimized = ref(false)
const isDragging = ref(false)
const isResizing = ref(false)
const position = reactive({ x: window.innerWidth - 370, y: window.innerHeight - 530 })
const dragOffset = reactive({ x: 0, y: 0 })
const size = reactive({ width: 380, height: 520 })
const resizeOffset = reactive({ x: 0, y: 0 })

const MIN_WIDTH = 300
const MAX_WIDTH = 600
const MIN_HEIGHT = 400
const MAX_HEIGHT = 800

const currentSession = ref(null)
const pendingSession = ref(null)
const sessionTypeRef = ref(1)
const messages = ref([])
const messageInput = ref('')
const sending = ref(false)
const messagesArea = ref(null)
const shopName = ref('')

// 轮询相关变量
const pollingInterval = ref(null)
const isPolling = ref(false)
const lastMessageCount = ref(0)

const quickQuestions = ref([
  { type: 'size', label: '尺码建议', content: '请问这个商品的尺码怎么选择？' },
  { type: 'material', label: '材质说明', content: '请问这个商品是什么材质的？' },
  { type: 'shipping', label: '发货时间', content: '请问什么时候发货？' },
  { type: 'policy', label: '退换货政策', content: '请问退换货政策是什么？' }
])

onMounted(() => {
  eventBus.on('open-floating-chat', async (data) => {
    console.log('收到打开悬浮聊天事件:', data)
    isVisible.value = true
    isMinimized.value = false
    
    if (data.shopName) {
      shopName.value = data.shopName
      console.log('设置店铺名称:', shopName.value)
    } else {
      shopName.value = `店铺 ${data.merchantId}`
      console.log('设置默认店铺名称:', shopName.value)
    }
    
    // 检查是否已有会话
    await checkExistingSession(data)
  })
  
  eventBus.on('close-floating-chat', () => {
    stopPolling()
    isVisible.value = false
    isMinimized.value = false
    shopName.value = ''
    currentSession.value = null
    pendingSession.value = null
    sessionTypeRef.value = 1
    messages.value = []
    lastMessageCount.value = 0
  })
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
})

const startDrag = (e) => {
  if (isMinimized.value) return
  isDragging.value = true
  dragOffset.x = e.clientX - position.x
  dragOffset.y = e.clientY - position.y
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}

const onDrag = (e) => {
  if (!isDragging.value) return
  let newX = e.clientX - dragOffset.x
  let newY = e.clientY - dragOffset.y
  const maxX = window.innerWidth - 350
  const maxY = window.innerHeight - 100
  newX = Math.max(0, Math.min(newX, maxX))
  newY = Math.max(0, Math.min(newY, maxY))
  position.x = newX
  position.y = newY
}

const stopDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

const startResize = (e) => {
  if (isMinimized.value) return
  isResizing.value = true
  resizeOffset.x = e.clientX - size.width
  resizeOffset.y = e.clientY - size.height
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
}

const onResize = (e) => {
  if (!isResizing.value) return
  let newWidth = e.clientX - resizeOffset.x
  let newHeight = e.clientY - resizeOffset.y
  
  newWidth = Math.max(MIN_WIDTH, Math.min(newWidth, MAX_WIDTH))
  newHeight = Math.max(MIN_HEIGHT, Math.min(newHeight, MAX_HEIGHT))
  
  size.width = newWidth
  size.height = newHeight
}

const stopResize = () => {
  isResizing.value = false
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
}

const toggleMinimize = () => {
  isMinimized.value = !isMinimized.value
}

const closeChat = () => {
  eventBus.emit('close-floating-chat')
}

// 启动轮询
const startPolling = () => {
  if (isPolling.value || !currentSession.value) return
  
  isPolling.value = true
  console.log('启动轮询')
  
  pollingInterval.value = setInterval(async () => {
    if (currentSession.value) {
      await loadMessages(currentSession.value.id)
    }
  }, 3000) // 每3秒轮询一次
}

// 停止轮询
const stopPolling = () => {
  if (pollingInterval.value) {
    clearInterval(pollingInterval.value)
    pollingInterval.value = null
    console.log('停止轮询')
  }
  isPolling.value = false
}

const sendMessage = async () => {
  if (!messageInput.value.trim()) return
  sending.value = true
  
  // 启动轮询
  startPolling()
  
  try {
    let response
    
    // 如果已有会话，直接发送消息
    if (currentSession.value) {
      response = await axios.post(`${API_BASE}/message/send`, {
        sessionId: currentSession.value.id,
        senderType: 1,
        senderId: userStore.userInfo?.id,
        receiverType: 2,
        receiverId: currentSession.value.merchantId,
        content: messageInput.value,
        messageType: 1,
        merchantId: currentSession.value.merchantId,
        productId: currentSession.value.productId,
        sessionType: currentSession.value.sessionType
      })
    } else if (pendingSession.value) {
      // 如果没有会话，但有pendingSession，创建新会话
      response = await axios.post(`${API_BASE}/message/send-with-session`, {
        senderType: 1,
        senderId: userStore.userInfo?.id,
        receiverType: 2,
        receiverId: pendingSession.value.merchantId,
        content: messageInput.value,
        messageType: 1,
        merchantId: pendingSession.value.merchantId,
        productId: pendingSession.value.productId,
        sessionType: pendingSession.value.sessionType
      })
      
      if (response.data.code === 200) {
        const newSessionId = response.data.data.sessionId
        currentSession.value = {
          id: newSessionId,
          merchantId: pendingSession.value.merchantId,
          productId: pendingSession.value.productId,
          sessionType: pendingSession.value.sessionType
        }
        pendingSession.value = null
      }
    }
    
    if (response.data.code === 200) {
      messageInput.value = ''
      const sessionId = currentSession.value.id
      await loadMessages(sessionId)
      lastMessageCount.value = messages.value.length
    }
  } catch (error) {
    ElMessage.error('发送消息失败')
  } finally {
    sending.value = false
  }
}

const loadMessages = async (sessionId) => {
  try {
    const response = await axios.get(`${API_BASE}/messages/${sessionId}`)
    if (response.data.code === 200) {
      const newMessages = response.data.data
      
      // 检查是否有新消息
      if (newMessages.length > lastMessageCount.value) {
        messages.value = newMessages
        lastMessageCount.value = newMessages.length
        
        nextTick(() => {
          if (messagesArea.value) {
            messagesArea.value.scrollTop = messagesArea.value.scrollHeight
          }
        })
      }
      
      // 标记消息为已读
      await markSessionAsRead(sessionId)
    }
  } catch (error) {
    console.error('加载消息失败:', error)
  }
}

const getMessageClass = (message) => {
  return message.senderType === 1 ? 'user-message' : 'merchant-message'
}

const getSessionTitle = (session) => {
  return `店铺 ${session.merchantId}`
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadShopName = async (merchantId) => {
  try {
    const response = await axios.get(`http://localhost:8080/api/merchant/${merchantId}`)
    if (response.data.code === 200) {
      shopName.value = response.data.data.shopName || `店铺 ${merchantId}`
    }
  } catch (error) {
    shopName.value = `店铺 ${merchantId}`
  }
}

const sendQuickQuestion = async (question) => {
  messageInput.value = question.content
  await sendMessage()
}

const checkExistingSession = async (data) => {
  try {
    sessionTypeRef.value = data.sessionType || 1
    
    // 如果已有会话，且是同一个商家和类型，不需要重新检查
    if (currentSession.value && 
        currentSession.value.merchantId === data.merchantId && 
        currentSession.value.sessionType === (data.sessionType || 1)) {
      console.log('已有会话，无需重新检查')
      return
    }
    
    const response = await axios.post(`${API_BASE}/session/check`, {
      userId: userStore.userInfo?.id,
      merchantId: data.merchantId,
      sessionType: data.sessionType || 1,
      productId: data.productId
    })
    
    if (response.data.code === 200 && response.data.data) {
      const existingSession = response.data.data
      currentSession.value = existingSession
      pendingSession.value = null
      await loadMessages(existingSession.id)
      lastMessageCount.value = messages.value.length
      await markSessionAsRead(existingSession.id)
      
      // 启动轮询
      startPolling()
    } else {
      // 只有在没有会话时才设置pendingSession
      if (!currentSession.value) {
        pendingSession.value = {
          merchantId: data.merchantId,
          productId: data.productId,
          sessionType: data.sessionType || 1
        }
      }
    }
  } catch (error) {
    console.log('未找到已有会话，等待用户发送消息时创建')
    sessionTypeRef.value = data.sessionType || 1
    // 只有在没有会话时才设置pendingSession
    if (!currentSession.value) {
      pendingSession.value = {
        merchantId: data.merchantId,
        productId: data.productId,
        sessionType: data.sessionType || 1
      }
    }
  }
}

const markSessionAsRead = async (sessionId) => {
  try {
    const response = await axios.put(`${API_BASE}/session/${sessionId}/user-read`)
    console.log('标记会话已读:', response.data)
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}
</script>

<style scoped>
.floating-chat {
  position: fixed;
  z-index: 999;
  cursor: move;
  transition: box-shadow 0.3s ease, opacity 0.3s ease;
}

.floating-chat:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.25);
}

.floating-chat.dragging {
  opacity: 0.9;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
  transform: scale(1.02);
}

.floating-chat.resizing {
  opacity: 0.9;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
}

.resize-handle {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 20px;
  height: 20px;
  cursor: nwse-resize;
  background: linear-gradient(135deg, transparent 50%, #409eff 50%);
  border-radius: 0 0 4px 0;
  z-index: 10;
}

.resize-handle:hover {
  background: linear-gradient(135deg, transparent 50%, #66b1ff 50%);
}

.floating-chat.minimized {
  height: auto !important;
  max-height: none !important;
}

.floating-chat.minimized .chat-content {
  display: none;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: move;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: white;
  font-size: 15px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.header-actions .el-button {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.15);
  color: white;
  padding: 0;
}

.header-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.7);
}

.header-actions .el-button .el-icon {
  font-size: 16px;
}

.floating-chat :deep(.el-card) {
  margin: 0;
  border: none;
}

.floating-chat :deep(.el-card__header) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border-bottom: none;
  padding: 12px 16px;
}

.floating-chat :deep(.el-card__body) {
  padding: 12px;
}

.chat-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  min-height: 200px;
  max-height: 300px;
}

.message {
  margin-bottom: 12px;
}

.user-message {
  text-align: right;
}

.merchant-message {
  text-align: left;
}

.message-content {
  display: inline-block;
  max-width: 70%;
  padding: 8px 12px;
  border-radius: 8px;
  word-wrap: break-word;
}

.user-message .message-content {
  background: #409eff;
  color: white;
}

.merchant-message .message-content {
  background: white;
  border: 1px solid #e4e7ed;
}

.message-time {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
}

.input-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
}

.input-actions .el-button {
  padding: 6px 16px;
  font-size: 13px;
}

.input-area :deep(.el-textarea__inner) {
  min-height: 32px !important;
  padding: 6px 8px !important;
  font-size: 13px;
}

.quick-questions {
  margin-bottom: 0;
}

.quick-question-title {
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
  font-weight: 500;
}

.quick-question-buttons {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.quick-question-buttons .el-button {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 16px;
}

.no-session,
.no-messages {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}
</style>