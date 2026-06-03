import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterView.vue'),
    meta: { title: '注册', noAuth: true }
  },
  {
    path: '/family-setup',
    name: 'FamilySetup',
    component: () => import('../views/FamilySetupView.vue'),
    meta: { title: '设置家庭' }
  },
  {
    path: '/',
    redirect: '/record-add'
  },
  {
    path: '/record-add',
    name: 'RecordAdd',
    component: () => import('../views/RecordAdd.vue'),
    meta: { title: '记账' }
  },
  {
    path: '/record-list',
    name: 'RecordList',
    component: () => import('../views/RecordList.vue'),
    meta: { title: '明细' }
  },
  {
    path: '/stats',
    name: 'StatsView',
    component: () => import('../views/StatsView.vue'),
    meta: { title: '统计' }
  },
  {
    path: '/settings',
    name: 'SettingsView',
    component: () => import('../views/SettingsView.vue'),
    meta: { title: '设置' }
  },
  {
    path: '/ai-chat',
    name: 'AiChat',
    component: () => import('../views/AiChatView.vue'),
    meta: { title: 'AI助手' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// ===== 路由守卫：检查登录状态 + 家庭状态 =====
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  // 访问免登录页面：已登录则跳首页
  if (to.meta.noAuth) {
    if (token) {
      next('/')
    } else {
      next()
    }
    return
  }

  // 访问需要登录的页面
  if (!token) {
    next('/login')
    return
  }

  // 已登录但未设置家庭 → 跳转家庭设置（/family-setup 本身除外）
  const familyId = localStorage.getItem('familyId')
  if (!familyId && to.path !== '/family-setup') {
    next('/family-setup')
    return
  }

  next()
})

export default router
