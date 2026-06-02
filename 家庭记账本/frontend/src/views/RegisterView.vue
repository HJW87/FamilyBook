<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <span class="login-icon">🏠</span>
        <h1>注册账号</h1>
        <p>创建您的家庭记账本账号</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="handleRegister">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码（至少6位）"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleRegister"
            class="login-btn"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span>已有账号？<router-link to="/login">去登录</router-link></span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import request from '../api/request'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await request.post('/auth/register', {
      username: form.username,
      password: form.password
    })
    const displayId = res.data?.displayId
    if (displayId) {
      ElMessage.success(`注册成功！您的用户ID是 ${displayId}，请牢记`)
    } else {
      ElMessage.success('注册成功！请登录')
    }
    router.push('/login')
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFF8DC 0%, #F5DEB3 50%, #FFE4B5 100%);
}

.login-card {
  width: 400px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 8px;
}

.login-header h1 {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.login-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
}

.login-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 13px;
  color: #999;
}

.login-footer a {
  color: var(--accent, #FFA500);
  text-decoration: none;
}

@media (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 28px;
  }
}
</style>
