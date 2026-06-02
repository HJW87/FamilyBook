<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <span class="login-icon">🏠</span>
        <h1>家庭记账本</h1>
        <p>请输入账号密码登录</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="handleLogin">
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
            placeholder="密码"
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
            @click="handleLogin"
            class="login-btn"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span><router-link to="/register">还没有账号？去注册</router-link></span>
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
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await request.post('/auth/login', {
      username: form.username,
      password: form.password
    })
    // 保存 token 和用户信息
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('username', res.data.username)
    if (res.data.displayId) {
      localStorage.setItem('displayId', res.data.displayId)
    }
    // 保存家庭信息
    if (res.data.familyId) {
      localStorage.setItem('familyId', res.data.familyId)
    } else {
      localStorage.removeItem('familyId')
    }
    if (res.data.userId) {
      localStorage.setItem('userId', res.data.userId)
    }
    if (res.data.role) {
      localStorage.setItem('role', res.data.role)
    }
    if (res.data.memberId) {
      localStorage.setItem('memberId', res.data.memberId)
    }
    if (res.data.avatar) {
      localStorage.setItem('avatar', res.data.avatar)
    } else {
      localStorage.removeItem('avatar')
    }

    ElMessage.success('登录成功')

    // 如果没有家庭，跳转到家庭设置页
    if (!res.data.familyId) {
      router.push('/family-setup')
    } else {
      router.push('/')
    }
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
  font-size: 12px;
  color: #bbb;
}

@media (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 28px;
  }
}
</style>
