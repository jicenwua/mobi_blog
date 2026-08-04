<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/store/user'

const visible = defineModel({ type: Boolean, default: false })

const userStore = useUserStore()
const loading = ref(false)
const formRef = ref(null)

const form = reactive({
  email: '',
  password: '',
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
}

function resetForm() {
  form.email = ''
  form.password = ''
  formRef.value?.clearValidate()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = await login({
      email: form.email.trim(),
      password: form.password,
    })
    userStore.setAuth(data)
    ElMessage.success('登录成功')
    visible.value = false
    resetForm()
  } catch {
    // 错误提示由 request 拦截器处理
  } finally {
    loading.value = false
  }
}

function handleClose() {
  resetForm()
}
</script>

<template>
  <el-dialog
    v-model="visible"
    title="登录"
    width="420px"
    destroy-on-close
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="64px"
      @submit.prevent="handleSubmit"
    >
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" clearable />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="请输入密码"
          show-password
          @keyup.enter="handleSubmit"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        登录
      </el-button>
    </template>
  </el-dialog>
</template>
