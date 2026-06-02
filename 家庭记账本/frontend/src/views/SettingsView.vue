<template>
  <div class="page-container">
    <h2 class="page-title">⚙️ 设置</h2>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- ===== 个人资料 ===== -->
      <el-tab-pane label="👤 个人资料" name="profile">
        <div v-loading="profileLoading" class="profile-section">
          <!-- 用户信息卡片 -->
          <div class="profile-card">
            <div class="profile-card-avatar">
              <img v-if="profileAvatarUrl" :src="profileAvatarUrl" class="profile-card-img" alt="头像" />
              <span v-else class="profile-card-emoji">{{ profileAvatarEmoji }}</span>
            </div>
            <div class="profile-card-body">
              <div class="profile-card-name">{{ profile.username || currentUsername }}</div>
              <div class="profile-card-meta">
                <span class="profile-card-id">🆔 用户ID：<code>#{{ profile.displayId || currentDisplayId }}</code></span>
                <el-tag v-if="isAdmin" type="warning" size="small" effect="dark">👑 管理员</el-tag>
                <el-tag v-else type="info" size="small">👤 成员</el-tag>
              </div>
            </div>
            <div class="profile-card-action">
              <el-button circle size="small" @click="refreshProfile" :loading="profileLoading" title="刷新资料">
                🔄
              </el-button>
            </div>
          </div>

          <!-- 头像设置区 -->
          <div class="avatar-section">
            <div class="section-header">
              <span class="section-title">🎨 更换头像</span>
              <span v-if="currentAvatar && currentAvatar !== 'male' && currentAvatar !== 'female'" class="section-badge">自定义图片</span>
              <span v-else-if="currentAvatar === 'male'" class="section-badge">男生</span>
              <span v-else-if="currentAvatar === 'female'" class="section-badge">女生</span>
              <span v-else class="section-badge default">默认</span>
            </div>

            <!-- 预设头像选择 -->
            <div class="preset-cards">
              <div
                :class="['preset-card', { selected: currentAvatar === 'male' }]"
                @click="selectPreset('male')"
              >
                <div class="preset-avatar-circle male">
                  <span>👨</span>
                </div>
                <span class="preset-name">男生</span>
                <span v-if="currentAvatar === 'male'" class="preset-check">✓</span>
              </div>
              <div
                :class="['preset-card', { selected: currentAvatar === 'female' }]"
                @click="selectPreset('female')"
              >
                <div class="preset-avatar-circle female">
                  <span>👩</span>
                </div>
                <span class="preset-name">女生</span>
                <span v-if="currentAvatar === 'female'" class="preset-check">✓</span>
              </div>
              <div
                :class="['preset-card', { selected: !currentAvatar || currentAvatar === '' }]"
                @click="selectPreset('')"
              >
                <div class="preset-avatar-circle default">
                  <span>👤</span>
                </div>
                <span class="preset-name">默认</span>
                <span v-if="!currentAvatar || currentAvatar === ''" class="preset-check">✓</span>
              </div>
            </div>

            <!-- 自定义上传 -->
            <div class="upload-section">
              <div class="upload-buttons">
                <el-button
                  type="primary"
                  :loading="avatarUploading"
                  @click="triggerUpload"
                  :icon="Upload"
                  size="large"
                >
                  上传本地图片
                </el-button>
                <input
                  ref="fileInput"
                  type="file"
                  accept="image/*"
                  style="display:none"
                  @change="handleFileChange"
                />
                <el-button
                  v-if="isCustomAvatar"
                  type="warning"
                  plain
                  size="large"
                  :loading="avatarUploading"
                  @click="handleResetAvatar"
                >
                  恢复默认头像
                </el-button>
              </div>
              <p class="upload-hint">💡 支持 JPG / PNG / GIF / WebP，文件大小不超过 2MB</p>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- ===== 类别管理 ===== -->
      <el-tab-pane label="📂 类别管理" name="categories">
        <el-radio-group v-model="catTypeFilter" @change="loadCategories" style="margin-bottom:16px">
          <el-radio-button value="EXPENSE">支出类别</el-radio-button>
          <el-radio-button value="INCOME">收入类别</el-radio-button>
        </el-radio-group>

        <el-table :data="categories" stripe empty-text="暂无类别" style="width:100%">
          <el-table-column label="图标" width="60">
            <template #default="{ row }"><span style="font-size:20px">{{ row.icon }}</span></template>
          </el-table-column>
          <el-table-column prop="name" label="名称" width="150" />
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openCatEdit(row)">编辑</el-button>
              <el-popconfirm title="确定删除此类别？" @confirm="handleCatDelete(row.id)">
                <template #reference>
                  <el-button type="danger" link size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>

        <el-button type="primary" @click="openCatAdd" style="margin-top:12px" :icon="Plus">添加类别</el-button>
      </el-tab-pane>

      <!-- ===== 成员管理（新版：身份标签 + 用户绑定） ===== -->
      <el-tab-pane label="👥 成员管理" name="members">
        <!-- 邀请码展示 -->
        <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px">
          <template #title>
            家庭邀请码：<strong>{{ inviteCode || '加载中...' }}</strong>
            <el-button type="primary" link size="small" @click="copyInviteCode" style="margin-left:8px">复制</el-button>
          </template>
          <template #default>
            <span style="font-size:12px;color:#909399">将邀请码发给家人，让他们注册后输入邀请码和身份标签加入你的家庭</span>
          </template>
        </el-alert>

        <!-- 当前用户信息 -->
        <el-alert type="success" :closable="false" show-icon style="margin-bottom:16px">
          <template #title>
            我的信息：用户名 <strong>{{ currentUsername }}</strong> | 用户ID <strong>{{ currentDisplayId }}</strong>
            <span v-if="isAdmin" style="color:var(--accent);margin-left:8px">（管理员）</span>
          </template>
        </el-alert>

        <!-- 成员列表 -->
        <div class="member-list">
          <div v-for="m in members" :key="m.id" class="member-row">
            <div class="member-info">
              <span class="member-label">🏷️ {{ m.name }}</span>
              <span v-if="m.username" class="member-user">
                👤 {{ m.username }} <span class="user-id-tag">#{{ m.displayId }}</span>
              </span>
              <span v-else class="member-empty">空座位</span>
            </div>
            <div class="member-actions">
              <!-- 空座位：仅管理员可邀请/删除 -->
              <template v-if="!m.userId">
                <template v-if="isAdmin">
                  <el-button type="primary" link size="small" @click="openInvite(m)">邀请</el-button>
                  <el-popconfirm title="确定删除此空标签？" @confirm="handleMemberDelete(m.id)">
                    <template #reference>
                      <el-button type="danger" link size="small">删除</el-button>
                    </template>
                  </el-popconfirm>
                </template>
                <span v-else style="font-size:12px;color:#ccc">等待管理员邀请</span>
              </template>
              <!-- 已绑定：当前用户可编辑自己的标签 -->
              <template v-else>
                <template v-if="m.userId === currentUserId">
                  <el-button type="primary" link size="small" @click="openEditLabel(m)">编辑标签</el-button>
                  <span class="is-self" style="font-size:12px;color:var(--accent)">（我）</span>
                </template>
                <template v-else>
                  <!-- 仅管理员可踢出非自己的已绑定用户 -->
                  <template v-if="isAdmin">
                    <el-popconfirm title="确定踢出该用户？踢出后用户将无法查看家庭记账本" @confirm="handleKick(m.id)">
                      <template #reference>
                        <el-button type="warning" link size="small">踢出</el-button>
                      </template>
                    </el-popconfirm>
                  </template>
                  <span v-else style="font-size:12px;color:#ccc">——</span>
                </template>
              </template>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="members.length === 0" style="text-align:center;padding:24px;color:#999">
            暂无成员标签，请联系管理员添加
          </div>
        </div>

        <!-- 添加标签（仅管理员可见） -->
        <div v-if="isAdmin" style="margin-top:16px;display:flex;gap:8px">
          <el-input v-model="newMemberName" placeholder="输入身份标签（如：儿子、女儿）" style="width:260px" @keyup.enter="handleMemberAdd" />
          <el-button type="primary" @click="handleMemberAdd">添加标签</el-button>
        </div>
        <div v-if="!isAdmin" style="margin-top:8px;font-size:12px;color:#999">
          🔒 仅管理员可添加/删除标签和邀请/踢出成员。你可以在自己的标签上点击"编辑标签"修改名称。
        </div>
      </el-tab-pane>

      <!-- ===== 邀请弹窗 ===== -->
      <el-dialog v-model="inviteVisible" title="邀请成员" width="420px" destroy-on-close>
        <el-form :model="inviteForm">
          <el-form-item label="目标标签">
            <el-tag size="large">{{ inviteTarget?.name }}</el-tag>
          </el-form-item>
          <el-form-item label="用户ID">
            <el-input v-model="inviteForm.displayId" placeholder="输入要邀请的用户ID（如 100002）" size="large">
              <template #prefix>#</template>
            </el-input>
            <div style="font-size:12px;color:#999;margin-top:4px">
              让被邀请的用户查看他的用户ID，然后在此输入
            </div>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="inviteVisible = false">取消</el-button>
          <el-button type="primary" :loading="inviting" @click="handleInvite">确认邀请</el-button>
        </template>
      </el-dialog>

      <!-- ===== 编辑标签弹窗 ===== -->
      <el-dialog v-model="editLabelVisible" title="修改身份标签" width="400px" destroy-on-close>
        <el-form :model="editLabelForm">
          <el-form-item label="当前名称">
            <el-tag size="large">{{ editLabelTarget?.name }}</el-tag>
          </el-form-item>
          <el-form-item label="新名称">
            <el-input v-model="editLabelForm.name" placeholder="输入新的标签名称" size="large" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editLabelVisible = false">取消</el-button>
          <el-button type="primary" :loading="editLabelSaving" @click="handleEditLabelSave">保存</el-button>
        </template>
      </el-dialog>

      <!-- ===== AI 配置 ===== -->
      <el-tab-pane label="🤖 AI 配置" name="ai">
        <div v-loading="aiConfigLoading">
          <!-- 状态卡片 -->
          <el-alert
            :type="aiConfig.configured ? 'success' : 'warning'"
            :closable="false"
            show-icon
            style="margin-bottom:16px"
          >
            <template #title>
              {{ aiConfig.configured ? '✅ AI 助手已配置' : '⚠️ AI 助手未配置' }}
            </template>
            <template #default>
              <div style="font-size:13px;color:#606266;margin-top:4px">
                <div>模型：<strong>{{ aiConfig.model || '-' }}</strong></div>
                <div v-if="aiConfig.configured">
                  Key：<code style="font-size:12px;background:#f0f0f0;padding:2px 6px;border-radius:4px">{{ aiConfig.maskedKey }}</code>
                  &nbsp;来源：{{ aiConfig.source === 'file' ? '📄 外部配置文件' : aiConfig.source === 'env' ? '🌐 环境变量' : '-' }}
                </div>
                <div v-else style="margin-top:4px">
                  来源：{{ aiConfig.sourceDetail || '未检测到 API Key' }}
                </div>
              </div>
            </template>
          </el-alert>

          <!-- 配置表单 -->
          <el-card shadow="never">
            <template #header>
              <span style="font-weight:600">{{ aiConfig.source === 'file' ? '修改 API Key' : '设置 API Key' }}</span>
            </template>
            <el-form label-width="100px">
              <el-form-item label="API Key">
                <el-input
                  v-model="aiApiKey"
                  :type="showApiKey ? 'text' : 'password'"
                  placeholder="输入 DashScope API Key（sk-开头）"
                  show-password
                  size="large"
                  @click:show-password="showApiKey = !showApiKey"
                  clearable
                >
                  <template #prefix>🔑</template>
                </el-input>
              </el-form-item>
              <el-form-item label=" ">
                <el-button type="primary" @click="handleAiConfigSave" :loading="aiConfigSaving" size="large">
                  保存配置
                </el-button>
                <el-button
                  v-if="aiConfig.source === 'file'"
                  type="warning"
                  plain
                  @click="handleAiConfigClear"
                  :loading="aiConfigSaving"
                  style="margin-left:8px"
                >
                  清除外部配置
                </el-button>
              </el-form-item>
            </el-form>

            <el-divider />

            <div style="font-size:12px;color:#909399;line-height:1.8">
              <p><strong>💡 如何获取 API Key？</strong></p>
              <ol style="padding-left:18px;margin:4px 0">
                <li>访问 <a href="https://dashscope.console.aliyun.com/" target="_blank" style="color:var(--accent)">阿里云 DashScope 控制台</a></li>
                <li>开通「灵积模型服务」→ 获取 API Key（sk-开头）</li>
                <li>将 Key 粘贴到上方输入框，点击保存</li>
              </ol>
              <p style="margin-top:8px"><strong>⚙️ 配置优先级：</strong></p>
              <p style="margin:2px 0">外部配置文件 &gt; 环境变量 <code>DASHSCOPE_API_KEY</code> &gt; application.yml</p>
              <p style="margin-top:8px"><strong>🚀 服务器部署说明：</strong></p>
              <p style="margin:2px 0">在设置页保存后，JAR 同级目录会生成 <code>ai-config.properties</code> 文件，重启后自动加载。也可直接在服务器上创建该文件写入 <code>api-key=你的Key</code>。</p>
            </div>
          </el-card>
        </div>
      </el-tab-pane>

    </el-tabs>

    <!-- 类别编辑弹窗 -->
    <el-dialog v-model="catDialogVisible" :title="catEditingId ? '编辑类别' : '添加类别'" width="420px" destroy-on-close>
      <el-form :model="catForm" label-width="70px">
        <el-form-item label="名称" required>
          <el-input v-model="catForm.name" placeholder="类别名称" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="catForm.icon" placeholder="emoji图标，如 🍔" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="catForm.sortOrder" :min="0" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="catDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="catSaving" @click="handleCatSave">
          {{ catEditingId ? '保存修改' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import { getCategories, addCategory, updateCategory, deleteCategory } from '../api/category'
import { getMembers, addMember, deleteMember, inviteUser, kickUser, updateMemberName } from '../api/family'
import { getAiConfig, updateAiConfig } from '../api/ai'
import { getProfile, updateAvatar, uploadAvatar as uploadAvatarApi, deleteAvatar } from '../api/user'
import request from '../api/request'

const activeTab = ref('categories')
const catTypeFilter = ref('EXPENSE')

// 角色和当前用户
const isAdmin = computed(() => localStorage.getItem('role') === 'ADMIN')
const currentUserId = computed(() => Number(localStorage.getItem('userId') || '0'))
const currentUsername = computed(() => localStorage.getItem('username') || '')
const currentDisplayId = computed(() => localStorage.getItem('displayId') || '')

// 邀请码
const inviteCode = ref('')

// 类别管理
const categories = ref([])
const catDialogVisible = ref(false)
const catSaving = ref(false)
const catEditingId = ref(null)
const catForm = ref({ name: '', icon: '📦', sortOrder: 0 })

const loadCategories = async () => {
  try {
    const res = await getCategories(catTypeFilter.value)
    categories.value = res.data
  } catch (e) { /* handled */ }
}

const openCatAdd = () => {
  catEditingId.value = null
  catForm.value = { name: '', icon: '📦', sortOrder: 0 }
  catDialogVisible.value = true
}

const openCatEdit = (row) => {
  catEditingId.value = row.id
  catForm.value = { name: row.name, icon: row.icon, sortOrder: row.sortOrder }
  catDialogVisible.value = true
}

const handleCatSave = async () => {
  if (!catForm.value.name.trim()) {
    ElMessage.warning('请输入类别名称')
    return
  }
  catSaving.value = true
  try {
    const data = {
      name: catForm.value.name,
      icon: catForm.value.icon || '📦',
      sortOrder: catForm.value.sortOrder,
      type: catTypeFilter.value
    }
    if (catEditingId.value) {
      await updateCategory(catEditingId.value, data)
      ElMessage.success('修改成功')
    } else {
      await addCategory(data)
      ElMessage.success('添加成功')
    }
    catDialogVisible.value = false
    loadCategories()
  } catch (e) { /* handled */ }
  finally { catSaving.value = false }
}

const handleCatDelete = async (id) => {
  try {
    await deleteCategory(id)
    ElMessage.success('删除成功')
    loadCategories()
  } catch (e) { /* handled */ }
}

// 成员管理
const members = ref([])
const newMemberName = ref('')

const loadMembers = async () => {
  try {
    const res = await getMembers()
    members.value = res.data || []
  } catch (e) { /* handled */ }
}

// 加载邀请码
const loadFamilyInfo = async () => {
  try {
    const res = await request.get('/family/info')
    inviteCode.value = res.data?.inviteCode || ''
  } catch (e) { /* ignored */ }
}

const copyInviteCode = () => {
  if (!inviteCode.value) return
  navigator.clipboard.writeText(inviteCode.value)
    .then(() => ElMessage.success('邀请码已复制'))
    .catch(() => ElMessage.info('请手动复制：' + inviteCode.value))
}

const handleMemberAdd = async () => {
  const name = newMemberName.value.trim()
  if (!name) return
  try {
    await addMember(name)
    ElMessage.success('标签添加成功')
    newMemberName.value = ''
    loadMembers()
  } catch (e) { /* handled */ }
}

const handleMemberDelete = async (id) => {
  try {
    await deleteMember(id)
    ElMessage.success('标签已删除')
    loadMembers()
  } catch (e) { /* handled */ }
}

// 邀请
const inviteVisible = ref(false)
const inviteTarget = ref(null)
const inviteForm = ref({ displayId: '' })
const inviting = ref(false)

const openInvite = (member) => {
  inviteTarget.value = member
  inviteForm.value = { displayId: '' }
  inviteVisible.value = true
}

const handleInvite = async () => {
  if (!inviteForm.value.displayId) {
    ElMessage.warning('请输入用户ID')
    return
  }
  if (!inviteTarget.value?.id) return
  inviting.value = true
  try {
    await inviteUser(inviteTarget.value.id, Number(inviteForm.value.displayId))
    ElMessage.success('邀请成功')
    inviteVisible.value = false
    loadMembers()
  } catch (e) { /* handled */ }
  finally { inviting.value = false }
}

// 踢出
const handleKick = async (memberId) => {
  try {
    await kickUser(memberId)
    ElMessage.success('已踢出用户')
    loadMembers()
  } catch (e) { /* handled */ }
}

// 编辑自己的标签
const editLabelVisible = ref(false)
const editLabelTarget = ref(null)
const editLabelForm = ref({ name: '' })
const editLabelSaving = ref(false)

const openEditLabel = (member) => {
  editLabelTarget.value = member
  editLabelForm.value = { name: member.name }
  editLabelVisible.value = true
}

const handleEditLabelSave = async () => {
  const newName = editLabelForm.value.name.trim()
  if (!newName) {
    ElMessage.warning('请输入新的标签名称')
    return
  }
  if (newName === editLabelTarget.value?.name) {
    editLabelVisible.value = false
    return
  }
  editLabelSaving.value = true
  try {
    await updateMemberName(editLabelTarget.value.id, newName)
    ElMessage.success('标签名称已更新')
    editLabelVisible.value = false
    loadMembers()
  } catch (e) { /* handled */ }
  finally { editLabelSaving.value = false }
}

// AI 配置
const aiConfig = ref({ configured: false, model: '', source: 'none', maskedKey: '', sourceDetail: '' })
const aiApiKey = ref('')
const aiConfigSaving = ref(false)
const aiConfigLoading = ref(false)
const showApiKey = ref(false)

const loadAiConfig = async () => {
  aiConfigLoading.value = true
  try {
    const res = await getAiConfig()
    aiConfig.value = res.data || {}
  } catch (e) { /* handled */ }
  finally { aiConfigLoading.value = false }
}

const handleAiConfigSave = async () => {
  aiConfigSaving.value = true
  try {
    await updateAiConfig(aiApiKey.value)
    ElMessage.success(aiApiKey.value.trim() ? 'AI 配置已保存，立即生效' : '已清除外部配置，回退到环境变量')
    aiApiKey.value = ''
    await loadAiConfig()
  } catch (e) { /* handled */ }
  finally { aiConfigSaving.value = false }
}

const handleAiConfigClear = async () => {
  aiApiKey.value = ''
  await handleAiConfigSave()
}

// ===== 个人资料 =====
const profile = ref({ username: '', displayId: '', avatar: '', role: '' })
const profileLoading = ref(false)
const currentAvatar = ref(localStorage.getItem('avatar') || '')
const avatarUploading = ref(false)
const fileInput = ref(null)

const isCustomAvatar = computed(() => {
  const av = currentAvatar.value
  return av && av !== 'male' && av !== 'female'
})

const profileAvatarUrl = computed(() => {
  const av = currentAvatar.value
  if (!av || av === 'male' || av === 'female') return null
  return '/uploads/avatars/' + av
})

const profileAvatarEmoji = computed(() => {
  const av = currentAvatar.value
  if (av === 'male') return '👨'
  if (av === 'female') return '👩'
  return '👤'
})

const loadProfile = async () => {
  profileLoading.value = true
  try {
    const res = await getProfile()
    if (res.data) {
      profile.value = res.data
      // 同步头像到 localStorage
      const serverAvatar = res.data.avatar || ''
      if (serverAvatar !== currentAvatar.value) {
        currentAvatar.value = serverAvatar
        if (serverAvatar) {
          localStorage.setItem('avatar', serverAvatar)
        } else {
          localStorage.removeItem('avatar')
        }
      }
    }
  } catch (e) { /* handled */ }
  finally { profileLoading.value = false }
}

const refreshProfile = () => {
  loadProfile()
}

const syncAvatarLocal = (val) => {
  currentAvatar.value = val
  if (val) {
    localStorage.setItem('avatar', val)
  } else {
    localStorage.removeItem('avatar')
  }
}

const selectPreset = async (avatar) => {
  if (avatar === currentAvatar.value) return
  // 如果之前是自定义头像，先删除服务器文件
  if (isCustomAvatar.value) {
    try { await deleteAvatar() } catch (e) { /* ignore */ }
  }
  try {
    const val = avatar || null
    await updateAvatar(val)
    syncAvatarLocal(val || '')
    ElMessage.success('头像已更新')
  } catch (e) { /* handled */ }
}

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileChange = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return

  // 校验类型
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/bmp', 'image/svg+xml']
  if (file.type && !validTypes.includes(file.type)) {
    ElMessage.warning('不支持的图片格式，请选择 JPG / PNG / GIF / WebP 图片')
    e.target.value = ''
    return
  }

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 2MB')
    e.target.value = ''
    return
  }

  avatarUploading.value = true
  try {
    const res = await uploadAvatarApi(file)
    const filename = res.data?.avatar
    if (filename) {
      syncAvatarLocal(filename)
      ElMessage.success('头像上传成功')
    }
  } catch (e) { /* handled */ }
  finally {
    avatarUploading.value = false
    e.target.value = ''
  }
}

const handleResetAvatar = async () => {
  avatarUploading.value = true
  try {
    await deleteAvatar()
    syncAvatarLocal('')
    ElMessage.success('已恢复默认头像')
  } catch (e) { /* handled */ }
  finally { avatarUploading.value = false }
}

onMounted(() => {
  loadProfile()
  loadCategories()
  loadMembers()
  loadFamilyInfo()
  loadAiConfig()
})
</script>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 20px;
}

/* ===== 个人资料 ===== */
.profile-section {
  max-width: 560px;
}

/* 用户信息卡片 */
.profile-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: linear-gradient(135deg, #FFFDF7 0%, #FFF8DC 50%, #FFF3D6 100%);
  border-radius: 16px;
  border: 1px solid #f0dfb8;
  margin-bottom: 28px;
  position: relative;
  overflow: hidden;
}

.profile-card::before {
  content: '';
  position: absolute;
  top: -40px;
  right: -40px;
  width: 120px;
  height: 120px;
  background: rgba(255, 165, 0, 0.06);
  border-radius: 50%;
  pointer-events: none;
}

.profile-card-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  background: #f0ede0;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid var(--accent);
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(255, 165, 0, 0.18);
}

.profile-card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-card-emoji {
  font-size: 44px;
  line-height: 1;
}

.profile-card-body {
  flex: 1;
  min-width: 0;
}

.profile-card-name {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.profile-card-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.profile-card-id {
  font-size: 13px;
  color: var(--text-secondary);
}

.profile-card-id code {
  font-family: 'SF Mono', 'Consolas', monospace;
  font-size: 13px;
  background: rgba(0,0,0,0.05);
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 600;
}

.profile-card-action {
  flex-shrink: 0;
}

/* 头像设置区 */
.avatar-section {
  background: var(--white);
  border-radius: 16px;
  border: 1px solid var(--border-light);
  padding: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}

.section-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.section-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 10px;
  background: var(--accent-light);
  color: var(--accent);
}

.section-badge.default {
  background: #f5f5f5;
  color: #999;
}

/* 预设头像卡片 */
.preset-cards {
  display: flex;
  gap: 14px;
  margin-bottom: 22px;
}

.preset-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 18px 12px 14px;
  border-radius: 14px;
  border: 2px solid #eee;
  cursor: pointer;
  transition: all 0.2s;
  background: #fafafa;
  position: relative;
}

.preset-card:hover {
  border-color: var(--accent-light);
  background: #FFFDF7;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
}

.preset-card.selected {
  border-color: var(--accent);
  background: #FFF8E1;
  box-shadow: 0 2px 10px rgba(255, 165, 0, 0.12);
}

.preset-avatar-circle {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  background: #f0ede0;
}

.preset-avatar-circle.male {
  background: linear-gradient(135deg, #E3F2FD, #BBDEFB);
}

.preset-avatar-circle.female {
  background: linear-gradient(135deg, #FCE4EC, #F8BBD0);
}

.preset-avatar-circle.default {
  background: #f0ede0;
}

.preset-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.preset-card.selected .preset-name {
  color: var(--accent);
}

.preset-check {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--accent);
  color: white;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

/* 上传区 */
.upload-section {
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.upload-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.upload-hint {
  font-size: 12px;
  color: #bbb;
  margin: 10px 0 0;
}

/* 成员列表 */
.member-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.member-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: var(--white);
  border-radius: 14px;
  border: 1px solid var(--border-light);
  transition: all var(--transition);
}

.member-row:hover {
  border-color: var(--accent-light);
  box-shadow: var(--shadow-sm);
}

.member-info {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
}

.member-label {
  font-weight: 700;
  font-size: 16px;
  color: var(--text-primary);
}

.member-user {
  font-size: 13px;
  color: var(--accent);
  background: var(--accent-light);
  padding: 3px 12px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.user-id-tag {
  font-size: 11px;
  color: #999;
  font-family: monospace;
}

.member-empty {
  font-size: 12px;
  color: #ccc;
  font-style: italic;
  background: #fafafa;
  padding: 2px 10px;
  border-radius: 8px;
}

.member-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-shrink: 0;
}

.is-self {
  font-size: 12px;
  color: var(--accent);
  font-weight: 600;
  background: var(--accent-light);
  padding: 2px 8px;
  border-radius: 8px;
}

.data-card {
  text-align: center;
  padding: 24px 16px;
  background: var(--primary-light);
  border-radius: 12px;
  border: 1px solid var(--border-light);
  margin-bottom: 16px;
}

.data-icon {
  font-size: 36px;
  margin-bottom: 8px;
}

.data-card h3 {
  font-size: 16px;
  margin-bottom: 6px;
}

.data-card p {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

@media (max-width: 768px) {
  .member-row {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  .member-actions {
    align-self: flex-end;
  }
}
</style>
