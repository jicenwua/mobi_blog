<script setup>
import { computed, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import {
  login,
  register,
  resetPassword,
  sendResetCode,
  sendVerificationCode,
} from '@/api/auth'
import { useUserStore } from '@/store/user'
import { useSeasonTheme } from '@/composables/useSeasonTheme'

useSeasonTheme()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const VALID_TABS = ['login', 'register', 'forgot']
const activeTab = ref('login')
const loading = ref(false)
const codeSending = ref(false)
const codeCountdown = ref(0)
let countdownTimer = null

const loginFormRef = ref(null)
const registerFormRef = ref(null)
const forgotFormRef = ref(null)

const loginForm = reactive({
  email: '',
  password: '',
})

const registerForm = reactive({
  email: '',
  nickName: '',
  password: '',
  confirmPassword: '',
  verificationCode: '',
})

const forgotForm = reactive({
  email: '',
  verificationCode: '',
  password: '',
  confirmPassword: '',
})

const tabTitle = computed(() => {
  const titles = {
    login: '登录',
    register: '注册',
    forgot: '忘记密码',
  }
  return titles[activeTab.value]
})

const codeButtonText = computed(() => {
  if (codeCountdown.value > 0) return `${codeCountdown.value}s 后重发`
  return '发送验证码'
})

const loginRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
}

const registerRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  nickName: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 50, message: '昵称不能超过 50 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为 6 位', trigger: 'blur' },
  ],
}

const forgotRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为 6 位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== forgotForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

watch(
  () => route.query.tab,
  (tab) => {
    if (typeof tab === 'string' && VALID_TABS.includes(tab)) {
      activeTab.value = tab
    } else {
      activeTab.value = 'login'
    }
  },
  { immediate: true },
)

function switchTab(tab) {
  if (activeTab.value === tab) return
  router.replace({ query: { ...route.query, tab } })
}

function goBack() {
  const redirect = route.query.redirect
  if (typeof redirect === 'string' && redirect.startsWith('/')) {
    router.push(redirect)
  } else {
    router.push('/')
  }
}

function startCountdown() {
  codeCountdown.value = 60
  countdownTimer = window.setInterval(() => {
    codeCountdown.value -= 1
    if (codeCountdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

async function handleSendRegisterCode() {
  const valid = await registerFormRef.value?.validateField('email').catch(() => false)
  if (!valid) return

  codeSending.value = true
  try {
    await sendVerificationCode({ email: registerForm.email.trim() })
    ElMessage.success('验证码已发送，请查收邮件')
    startCountdown()
  } catch {
    // 错误提示由 request 拦截器处理
  } finally {
    codeSending.value = false
  }
}

async function handleSendResetCode() {
  const valid = await forgotFormRef.value?.validateField('email').catch(() => false)
  if (!valid) return

  codeSending.value = true
  try {
    await sendResetCode({ email: forgotForm.email.trim() })
    ElMessage.success('验证码已发送，请查收邮件')
    startCountdown()
  } catch {
    // 错误提示由 request 拦截器处理
  } finally {
    codeSending.value = false
  }
}

async function handleLogin() {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = await login({
      email: loginForm.email.trim(),
      password: loginForm.password,
    })
    userStore.setAuth(data)
    ElMessage.success('登录成功')
    goBack()
  } catch {
    // 错误提示由 request 拦截器处理
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await register({
      email: registerForm.email.trim(),
      nickName: registerForm.nickName.trim(),
      password: registerForm.password,
      verificationCode: registerForm.verificationCode,
    })
    ElMessage.success('注册成功，请登录')
    switchTab('login')
    loginForm.email = registerForm.email.trim()
    loginForm.password = ''
    registerForm.nickName = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
    registerForm.verificationCode = ''
    registerFormRef.value?.clearValidate()
  } catch {
    // 错误提示由 request 拦截器处理
  } finally {
    loading.value = false
  }
}

async function handleResetPassword() {
  const valid = await forgotFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await resetPassword({
      email: forgotForm.email.trim(),
      verificationCode: forgotForm.verificationCode,
      password: forgotForm.password,
    })
    ElMessage.success('密码重置成功，请登录')
    switchTab('login')
    loginForm.email = forgotForm.email.trim()
    loginForm.password = ''
    forgotForm.verificationCode = ''
    forgotForm.password = ''
    forgotForm.confirmPassword = ''
    forgotFormRef.value?.clearValidate()
  } catch {
    // 错误提示由 request 拦截器处理
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<template>
  <div class="auth-page">
    <div class="auth-page__backdrop" />

    <div class="auth-card">
      <div class="auth-card__header">
        <h1 class="auth-card__title">{{ tabTitle }}</h1>
        <button type="button" class="auth-card__close" aria-label="返回" @click="goBack">
          <el-icon :size="18"><Close /></el-icon>
        </button>
      </div>

      <!-- 登录 -->
      <el-form
        v-if="activeTab === 'login'"
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="80px"
        class="auth-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="loginForm.email" placeholder="请输入邮箱" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <div class="auth-form__links">
          <button type="button" class="auth-link" @click="switchTab('forgot')">
            忘记密码？
          </button>
        </div>

        <div class="auth-form__actions">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" :loading="loading" @click="handleLogin">
            登录
          </el-button>
        </div>

        <p class="auth-form__footer">
          还没有账号？
          <button type="button" class="auth-link" @click="switchTab('register')">
            立即注册
          </button>
        </p>
      </el-form>

      <!-- 注册 -->
      <el-form
        v-else-if="activeTab === 'register'"
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="80px"
        class="auth-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" clearable />
        </el-form-item>
        <el-form-item label="昵称" prop="nickName">
          <el-input v-model="registerForm.nickName" placeholder="请输入昵称" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="验证码" prop="verificationCode">
          <div class="auth-form__code-row">
            <el-input
              v-model="registerForm.verificationCode"
              placeholder="请输入验证码"
              maxlength="6"
            />
            <el-button
              :disabled="codeCountdown > 0"
              :loading="codeSending"
              @click="handleSendRegisterCode"
            >
              {{ codeButtonText }}
            </el-button>
          </div>
        </el-form-item>

        <div class="auth-form__actions">
          <el-button @click="switchTab('login')">返回登录</el-button>
          <el-button type="primary" :loading="loading" @click="handleRegister">
            注册
          </el-button>
        </div>
      </el-form>

      <!-- 忘记密码 -->
      <el-form
        v-else
        ref="forgotFormRef"
        :model="forgotForm"
        :rules="forgotRules"
        label-width="80px"
        class="auth-form"
        @submit.prevent="handleResetPassword"
      >
        <p class="auth-form__hint">请输入注册邮箱，我们将发送验证码以重置密码。</p>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="forgotForm.email" placeholder="请输入邮箱" clearable />
        </el-form-item>
        <el-form-item label="验证码" prop="verificationCode">
          <div class="auth-form__code-row">
            <el-input
              v-model="forgotForm.verificationCode"
              placeholder="请输入验证码"
              maxlength="6"
            />
            <el-button
              :disabled="codeCountdown > 0"
              :loading="codeSending"
              @click="handleSendResetCode"
            >
              {{ codeButtonText }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input
            v-model="forgotForm.password"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="forgotForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            @keyup.enter="handleResetPassword"
          />
        </el-form-item>

        <div class="auth-form__actions">
          <el-button @click="switchTab('login')">返回登录</el-button>
          <el-button type="primary" :loading="loading" @click="handleResetPassword">
            重置密码
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  overflow: hidden;
}

.auth-page__backdrop {
  position: fixed;
  inset: 0;
  background:
    linear-gradient(var(--blog-grid-line) 1px, transparent 1px),
    linear-gradient(90deg, var(--blog-grid-line) 1px, transparent 1px),
    radial-gradient(ellipse at 30% 20%, var(--blog-primary-light) 0%, transparent 50%),
    radial-gradient(ellipse at 70% 80%, var(--blog-primary-light) 0%, transparent 50%);
  background-size: 20px 20px, 20px 20px, auto, auto;
  background-color: var(--blog-bg);
  filter: blur(0);
  z-index: 0;
}

.auth-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 460px;
  padding: 28px 32px 32px;
  border-radius: 12px;
  background: var(--blog-card-bg);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.auth-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.auth-card__title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--blog-text);
}

.auth-card__close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--blog-text-secondary);
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.auth-card__close:hover {
  background: var(--blog-primary-light);
  color: var(--blog-text);
}

.auth-form :deep(.el-form-item__label) {
  color: var(--blog-text);
  white-space: nowrap;
}

.auth-form :deep(.el-form-item__label::before) {
  color: #f56c6c;
}

.auth-form__hint {
  margin: 0 0 16px;
  padding-left: 80px;
  font-size: 13px;
  color: var(--blog-text-secondary);
  line-height: 1.6;
}

.auth-form__code-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.auth-form__code-row .el-input {
  flex: 1;
}

.auth-form__code-row .el-button {
  flex-shrink: 0;
  min-width: 108px;
}

.auth-form__links {
  display: flex;
  justify-content: flex-end;
  margin: -4px 0 16px;
  padding-right: 4px;
}

.auth-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

.auth-form__footer {
  margin: 20px 0 0;
  text-align: center;
  font-size: 14px;
  color: var(--blog-text-secondary);
}

.auth-link {
  padding: 0;
  border: none;
  background: transparent;
  color: var(--blog-primary);
  font-size: inherit;
  cursor: pointer;
  transition: color 0.2s ease;
}

.auth-link:hover {
  color: var(--blog-primary-hover);
}

@media (max-width: 480px) {
  .auth-card {
    padding: 24px 20px 28px;
  }

  .auth-form :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  .auth-form__hint {
    padding-left: 0;
  }

  .auth-form__code-row {
    flex-direction: column;
  }

  .auth-form__code-row .el-button {
    width: 100%;
  }
}
</style>
