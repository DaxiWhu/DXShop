import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getVerifyCode, login, register } from '../api'
import { useAuthStore } from '../store/auth'
import { useToast } from '../components/Toast'

export default function Login() {
  const navigate = useNavigate()
  const { toast } = useToast()
  const authLogin = useAuthStore(s => s.login)
  const [phone, setPhone] = useState('')
  const [code, setCode] = useState('')
  const [codeSent, setCodeSent] = useState(false)
  const [countdown, setCountdown] = useState(0)
  const [loading, setLoading] = useState(false)

  const sendCode = async () => {
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      toast('请输入正确的手机号', 'error')
      return
    }
    try {
      setLoading(true)
      await getVerifyCode(phone)
      toast('验证码已发送', 'success')
      setCodeSent(true)
      setCountdown(60)
      const timer = setInterval(() => {
        setCountdown(prev => {
          if (prev <= 1) {
            clearInterval(timer)
            return 0
          }
          return prev - 1
        })
      }, 1000)
    } catch {
      toast('发送失败，请稍后再试', 'error')
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async () => {
    if (!phone || !code) {
      toast('请填写手机号和验证码', 'error')
      return
    }
    try {
      setLoading(true)
      // 先尝试登录
      try {
        const res = await login({ phone, verifyCode: code })
        authLogin(res.token, res.userId, res.nickname, res.avatarUrl)
        toast('登录成功', 'success')
        navigate('/')
      } catch {
        // 登录失败尝试注册
        await register({ phone, verifyCode: code })
        const res2 = await login({ phone, verifyCode: code })
        authLogin(res2.token, res2.userId, res2.nickname, res2.avatarUrl)
        toast('注册成功', 'success')
        navigate('/')
      }
    } catch {
      toast('操作失败，请检查验证码', 'error')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-white flex flex-col">
      <div className="px-6 pt-20">
        <div className="text-center mb-10">
          <div className="text-5xl mb-4">🛒</div>
          <h1 className="text-2xl font-bold text-gray-800">大溪商城</h1>
          <p className="text-gray-400 text-sm mt-2">手机号快捷登录</p>
        </div>

        <div className="space-y-4">
          <div>
            <label className="block text-sm text-gray-500 mb-1">手机号</label>
            <input
              type="tel"
              maxLength={11}
              value={phone}
              onChange={e => setPhone(e.target.value.replace(/\D/g, ''))}
              placeholder="请输入手机号"
              className="w-full h-12 px-4 rounded-xl border border-gray-200 focus:border-taobao-500 focus:outline-none text-base"
            />
          </div>

          <div>
            <label className="block text-sm text-gray-500 mb-1">验证码</label>
            <div className="flex gap-3">
              <input
                type="text"
                maxLength={6}
                value={code}
                onChange={e => setCode(e.target.value.replace(/\D/g, ''))}
                placeholder="请输入验证码"
                className="flex-1 h-12 px-4 rounded-xl border border-gray-200 focus:border-taobao-500 focus:outline-none text-base"
              />
              <button
                onClick={sendCode}
                disabled={countdown > 0 || loading}
                className={`shrink-0 h-12 px-4 rounded-xl text-sm font-medium transition-colors ${
                  countdown > 0
                    ? 'bg-gray-100 text-gray-400'
                    : 'bg-taobao-50 text-taobao-500 hover:bg-taobao-100'
                }`}
              >
                {countdown > 0 ? `${countdown}s` : codeSent ? '重新发送' : '获取验证码'}
              </button>
            </div>
          </div>

          <button
            onClick={handleSubmit}
            disabled={loading}
            className="btn-taobao w-full h-12 text-base mt-6"
          >
            {loading ? '处理中...' : '登录 / 注册'}
          </button>
        </div>

        <p className="text-center text-xs text-gray-400 mt-8">
          登录即表示同意《用户协议》和《隐私政策》
        </p>
      </div>
    </div>
  )
}
