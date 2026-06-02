<template>
  <!-- 桌面端：顶部导航 -->
  <header class="navbar-desktop">
    <div class="navbar-inner">
      <div class="navbar-brand" @click="$router.push('/')">
        <span class="brand-icon">💰</span>
        <span class="brand-text">家庭记账本</span>
      </div>
      <nav class="navbar-menu">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          active-class="menu-item-active"
        >
          <span class="menu-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>
      <div class="navbar-user">
        <div class="user-avatar">
          <img v-if="avatarUrl" :src="avatarUrl" class="avatar-img" alt="avatar" />
          <span v-else class="avatar-emoji">{{ avatarEmoji }}</span>
        </div>
        <span class="user-tag">{{ username }} <span v-if="displayId" style="font-family:monospace;font-size:11px;opacity:0.7">#{{ displayId }}</span></span>
        <el-button type="danger" link size="small" @click="handleLogout">退出</el-button>
      </div>
    </div>
  </header>

  <!-- 移动端：底部导航 -->
  <footer class="navbar-mobile">
    <router-link
      v-for="item in menuItems"
      :key="item.path"
      :to="item.path"
      class="mobile-menu-item"
      active-class="mobile-menu-item-active"
    >
      <span class="mobile-icon">{{ item.icon }}</span>
      <span class="mobile-label">{{ item.label }}</span>
    </router-link>
  </footer>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const username = ref('')
const displayId = ref('')
const avatar = ref('')

const avatarUrl = computed(() => {
  if (!avatar.value) return null
  if (avatar.value === 'male') return null  // use emoji
  if (avatar.value === 'female') return null // use emoji
  // custom uploaded image
  return '/uploads/avatars/' + avatar.value
})

const avatarEmoji = computed(() => {
  if (!avatar.value) return '👤'
  if (avatar.value === 'male') return '👨'
  if (avatar.value === 'female') return '👩'
  return null  // custom image, use <img>
})

const menuItems = [
  { path: '/record-add', label: '记账', icon: '✏️' },
  { path: '/record-list', label: '明细', icon: '📋' },
  { path: '/stats', label: '统计', icon: '📊' },
  { path: '/ai-chat', label: 'AI助手', icon: '🤖' },
  { path: '/settings', label: '设置', icon: '⚙️' }
]

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('familyId')
  localStorage.removeItem('userId')
  localStorage.removeItem('role')
  localStorage.removeItem('displayId')
  localStorage.removeItem('memberId')
  localStorage.removeItem('avatar')
  ElMessage.success('已退出登录')
  router.push('/login')
}

onMounted(() => {
  username.value = localStorage.getItem('username') || '用户'
  displayId.value = localStorage.getItem('displayId') || ''
  avatar.value = localStorage.getItem('avatar') || ''
})
</script>

<style scoped>
/* === 桌面端顶部导航 === */
.navbar-desktop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  box-shadow: 0 1px 0 rgba(0,0,0,0.06), 0 4px 20px rgba(0,0,0,0.04);
  z-index: 1000;
}

.navbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}

.brand-icon {
  font-size: 30px;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.10));
}

.brand-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.5px;
}

.navbar-menu {
  display: flex;
  gap: 2px;
  background: #f5f3eb;
  padding: 4px;
  border-radius: 14px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 18px;
  border-radius: 11px;
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all var(--transition);
}

.menu-item:hover {
  background: var(--white);
  color: var(--accent);
  box-shadow: var(--shadow-sm);
}

.menu-item-active {
  background: var(--accent) !important;
  color: var(--white) !important;
  box-shadow: 0 2px 8px rgba(255, 165, 0, 0.30);
}

.menu-icon {
  font-size: 16px;
}

/* === 用户区 === */
.navbar-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  background: #f0ede0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 2px solid var(--border-light);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-emoji {
  font-size: 20px;
  line-height: 1;
}

.user-tag {
  font-size: 13px;
  color: var(--text-secondary);
  background: var(--primary-bg);
  padding: 5px 12px;
  border-radius: 20px;
  border: 1px solid var(--border-light);
}

/* === 移动端底部导航 === */
.navbar-mobile {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 64px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  box-shadow: 0 -1px 0 rgba(0,0,0,0.06), 0 -4px 20px rgba(0,0,0,0.04);
  z-index: 1000;
  justify-content: space-around;
  align-items: center;
  padding: 0 8px;
}

.mobile-menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 11px;
  padding: 6px 14px;
  border-radius: 12px;
  transition: all var(--transition);
  font-weight: 500;
}

.mobile-menu-item-active {
  color: var(--accent);
}

.mobile-icon {
  font-size: 22px;
  transition: transform var(--transition);
}

.mobile-menu-item-active .mobile-icon {
  transform: scale(1.15);
}

.mobile-label {
  font-size: 11px;
}

/* === 响应式 === */
@media (max-width: 768px) {
  .navbar-desktop {
    display: none;
  }

  .navbar-mobile {
    display: flex;
  }
}
</style>
