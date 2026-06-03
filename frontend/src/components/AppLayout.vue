<template>
  <div class="app-layout">
    <NavBar />
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import NavBar from './NavBar.vue'
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  background-color: var(--primary-bg);
}

.main-content {
  padding-top: 64px;
  min-height: calc(100vh - 64px);
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 移动端：底部导航留白 */
@media (max-width: 768px) {
  .main-content {
    padding-top: 0;
    padding-bottom: 60px;
  }
}
</style>
