<template>
  <div class="login-container">
    <div class="login-card setup-card">
      <div class="login-header">
        <span class="login-icon">👨‍👩‍👧‍👦</span>
        <h1>设置家庭</h1>
        <p>您还没有加入任何家庭，请选择：</p>
        <div class="user-id-card">
          <span class="id-label">我的ID</span>
          <span class="id-value">{{ displayId }}</span>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="setup-tabs">
        <!-- 创建家庭 -->
        <el-tab-pane label="创建家庭" name="create">
          <el-form :model="createForm" :rules="createRules" ref="createFormRef">
            <el-form-item prop="name">
              <el-input
                v-model="createForm.name"
                placeholder="家庭名称（如：小明的家庭）"
                size="large"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="createLoading"
                @click="handleCreate"
                class="login-btn"
              >
                创建家庭
              </el-button>
            </el-form-item>
          </el-form>

          <div v-if="createdFamily" class="family-created">
            <el-alert
              title="家庭创建成功！"
              type="success"
              :closable="false"
              show-icon
            >
              <p>邀请码：<strong>{{ createdFamily.inviteCode }}</strong></p>
              <p>将此邀请码分享给家人，让他们注册后加入你的家庭。</p>
            </el-alert>
            <el-button type="primary" @click="goHome" style="margin-top:16px;width:100%">
              开始记账
            </el-button>
          </div>
        </el-tab-pane>

        <!-- 加入家庭 -->
        <el-tab-pane label="加入家庭" name="join">
          <el-form :model="joinForm" :rules="joinRules" ref="joinFormRef">
            <el-form-item prop="inviteCode">
              <el-input
                v-model="joinForm.inviteCode"
                placeholder="输入家庭邀请码"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="labelName">
              <el-input
                v-model="joinForm.labelName"
                placeholder="您的身份标签（如：爸爸、妈妈、儿子）"
                size="large"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="joinLoading"
                @click="handleJoin"
                class="login-btn"
              >
                加入家庭
              </el-button>
            </el-form-item>
          </el-form>
          <div class="login-footer">
            <span>请向家庭管理员索要邀请码，并填写您在家中的身份</span>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const activeTab = ref('create')
const displayId = ref(localStorage.getItem('displayId') || '未分配')

// 创建家庭
const createFormRef = ref(null)
const createLoading = ref(false)
const createForm = reactive({ name: '' })
const createRules = {
  name: [{ required: true, message: '请输入家庭名称', trigger: 'blur' }]
}
const createdFamily = ref(null)

const handleCreate = async () => {
  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) return

  createLoading.value = true
  try {
    const res = await request.post('/family/create', { name: createForm.name })
    createdFamily.value = res.data
    // 成功后需要重新登录以获取新的 JWT（含 familyId）
    ElMessage.success('家庭创建成功！请重新登录')
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    setTimeout(() => router.push('/login'), 1500)
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    createLoading.value = false
  }
}

// 加入家庭
const joinFormRef = ref(null)
const joinLoading = ref(false)
const joinForm = reactive({ inviteCode: '', labelName: '' })
const joinRules = {
  inviteCode: [{ required: true, message: '请输入邀请码', trigger: 'blur' }],
  labelName: [{ required: true, message: '请输入您的身份标签', trigger: 'blur' }]
}

const handleJoin = async () => {
  const valid = await joinFormRef.value.validate().catch(() => false)
  if (!valid) return

  joinLoading.value = true
  try {
    await request.post('/family/join', {
      inviteCode: joinForm.inviteCode.trim(),
      labelName: joinForm.labelName.trim()
    })
    ElMessage.success('加入家庭成功！请重新登录获取权限')
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    setTimeout(() => router.push('/login'), 1500)
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    joinLoading.value = false
  }
}

const goHome = () => {
  router.push('/')
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
  width: 420px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  padding: 40px;
}

.setup-card {
  width: 460px;
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
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

.user-id-card {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 8px 18px;
  background: linear-gradient(135deg, #FFF8DC, #FFE4B5);
  border: 1.5px solid #F5DEB3;
  border-radius: 24px;
  font-size: 14px;
}

.user-id-card .id-label {
  color: #999;
  font-weight: 500;
}

.user-id-card .id-value {
  color: #E8960A;
  font-weight: 700;
  font-size: 16px;
  letter-spacing: 1px;
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

.family-created {
  margin-top: 16px;
}

.setup-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

@media (max-width: 480px) {
  .login-card, .setup-card {
    width: 90%;
    padding: 28px;
  }
}
</style>
