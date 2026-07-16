import { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { getUserChatSessions, userSendMessage, userCloseSession, getUserSessionDetail } from '../api'
import { useAuthStore } from '../store/auth'
import { useToast } from '../components/Toast'
import type { ChatSessionDTO, ChatMessageDTO } from '../api/types'

export default function Chat() {
  const navigate = useNavigate()
  const { isLoggedIn } = useAuthStore()
  const { toast } = useToast()

  const [sessions, setSessions] = useState<ChatSessionDTO[]>([])
  const [activeSession, setActiveSession] = useState<ChatSessionDTO | null>(null)
  const [messages, setMessages] = useState<ChatMessageDTO[]>([])
  const [inputText, setInputText] = useState('')
  const [loading, setLoading] = useState(true)
  const [sending, setSending] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!isLoggedIn) { navigate('/login'); return }
    loadSessions()
  }, [isLoggedIn])

  const loadSessions = async () => {
    try {
      setLoading(true)
      const data = await getUserChatSessions()
      setSessions(data || [])
      if (data?.length && !activeSession) {
        selectSession(data[0])
      }
    } catch {
      // 忽略
    } finally {
      setLoading(false)
    }
  }

  const selectSession = async (session: ChatSessionDTO) => {
    setActiveSession(session)
    try {
      const detail = await getUserSessionDetail(session.id)
      setMessages(detail as any as ChatMessageDTO[] || [])
    } catch {
      setMessages([])
    }
    setTimeout(() => messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' }), 100)
  }

  const sendMessage = async () => {
    if (!inputText.trim() || !activeSession) return
    try {
      setSending(true)
      const msg = await userSendMessage(activeSession.id, { content: inputText.trim() })
      setMessages(prev => [...prev, msg])
      setInputText('')
      setTimeout(() => messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' }), 100)
    } catch {
      toast('发送失败', 'error')
    } finally {
      setSending(false)
    }
  }

  const closeSession = async () => {
    if (!activeSession) return
    try {
      await userCloseSession(activeSession.id)
      toast('会话已关闭', 'success')
      setSessions(prev => prev.filter(s => s.id !== activeSession.id))
      setActiveSession(null)
      setMessages([])
    } catch {
      toast('操作失败', 'error')
    }
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage()
    }
  }

  if (!isLoggedIn) return null

  return (
    <div className="bg-white min-h-screen flex flex-col h-screen">
      {/* Header */}
      <div className="bg-white px-4 py-3 flex items-center sticky top-12 z-10 border-b border-gray-100">
        <button onClick={() => navigate(-1)} className="mr-3">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <h1 className="text-base font-bold">客服中心</h1>
      </div>

      {!activeSession ? (
        // Session List
        <div className="flex-1 overflow-y-auto">
          {sessions.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full text-gray-400">
              <div className="text-5xl mb-4">💬</div>
              <p>暂无会话</p>
              <p className="text-xs mt-1">在商品详情页点击"客服"开始对话</p>
            </div>
          ) : (
            <div className="divide-y divide-gray-50">
              {sessions.map(s => (
                <button
                  key={s.id}
                  onClick={() => selectSession(s)}
                  className="w-full p-4 text-left hover:bg-gray-50 transition-colors"
                >
                  <div className="flex items-center justify-between">
                    <p className="text-sm font-medium">会话 #{s.id}</p>
                    <span className={`text-xs px-2 py-0.5 rounded ${
                      s.status === 1 ? 'bg-green-50 text-green-500' : 'bg-gray-100 text-gray-400'
                    }`}>
                      {s.status === 1 ? '进行中' : '已关闭'}
                    </span>
                  </div>
                  <p className="text-xs text-gray-400 mt-1 line-clamp-1">{s.lastMessage || '暂无消息'}</p>
                  <p className="text-xs text-gray-300 mt-0.5">{s.lastMessageTime}</p>
                </button>
              ))}
            </div>
          )}
        </div>
      ) : (
        // Chat Detail
        <div className="flex flex-col flex-1">
          {/* Session info */}
          <div className="bg-gray-50 px-4 py-2 flex items-center justify-between border-b border-gray-100">
            <span className="text-sm text-gray-500">会话 #{activeSession.id}</span>
            <button onClick={closeSession} className="text-xs text-red-400">关闭会话</button>
          </div>

          {/* Messages */}
          <div className="flex-1 overflow-y-auto p-4 space-y-3 bg-gray-50">
            {messages.length === 0 && (
              <p className="text-center text-gray-400 text-sm py-8">等待客服接入...</p>
            )}
            {messages.map(msg => {
              const isMine = msg.senderType === 'USER'
              return (
                <div key={msg.id} className={`flex ${isMine ? 'justify-end' : 'justify-start'}`}>
                  <div className={`max-w-[75%] px-4 py-2.5 rounded-2xl text-sm ${
                    isMine
                      ? 'bg-taobao-500 text-white rounded-br-md'
                      : 'bg-white text-gray-700 rounded-bl-md shadow-sm'
                  }`}>
                    <p>{msg.content}</p>
                    <span className={`text-xs mt-1 block ${isMine ? 'text-white/60' : 'text-gray-400'}`}>
                      {msg.createTime}
                    </span>
                  </div>
                </div>
              )
            })}
            <div ref={messagesEndRef} />
          </div>

          {/* Input */}
          <div className="border-t border-gray-100 p-3 flex items-end gap-2 bg-white safe-bottom">
            <textarea
              value={inputText}
              onChange={e => setInputText(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="输入消息..."
              rows={1}
              className="flex-1 resize-none border border-gray-200 rounded-2xl px-4 py-2.5 text-sm focus:outline-none focus:border-taobao-500 max-h-24"
            />
            <button
              onClick={sendMessage}
              disabled={sending || !inputText.trim()}
              className={`shrink-0 w-10 h-10 rounded-full flex items-center justify-center transition-colors ${
                inputText.trim()
                  ? 'bg-taobao-500 text-white'
                  : 'bg-gray-100 text-gray-400'
              }`}
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
              </svg>
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
