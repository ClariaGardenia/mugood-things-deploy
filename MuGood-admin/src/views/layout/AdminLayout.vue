<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { DataLine, Goods, Grid, Picture, Tickets, User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const pageTitle = computed(() => route.meta.title || 'Fresh Rabbit Admin')

const logout = () => {
  authStore.logout()
  router.replace('/login')
}
</script>

<template>
  <div class="admin-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">FR</span>
        <div>
          <strong>MU GOOD</strong>
          <small>Admin Console</small>
        </div>
      </div>
      <el-menu router :default-active="route.path" class="side-menu">
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/goods">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/category">
          <el-icon><Grid /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><Tickets /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/banners">
          <el-icon><Picture /></el-icon>
          <span>轮播管理</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </aside>
    <section class="main">
      <header class="topbar">
        <h1>{{ pageTitle }}</h1>
        <div class="account">
          <span>{{ authStore.user?.nickname }}</span>
          <el-button text @click="logout">退出</el-button>
        </div>
      </header>
      <main class="content">
        <RouterView />
      </main>
    </section>
  </div>
</template>

<style scoped lang="scss">
.admin-shell {
  min-height: 100vh;
  display: flex;
}

.sidebar {
  width: 236px;
  background: #111827;
  color: #fff;
}

.brand {
  height: 72px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;

  small {
    display: block;
    color: #9ca3af;
    margin-top: 4px;
  }
}

.brand-mark {
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: #10b981;
  font-weight: 800;
}

.side-menu {
  border-right: 0;
  background: transparent;

  :deep(.el-menu-item) {
    color: #d1d5db;
  }

  :deep(.el-menu-item.is-active),
  :deep(.el-menu-item:hover) {
    color: #fff;
    background: rgba(16, 185, 129, .2);
  }
}

.main {
  flex: 1;
  min-width: 0;
}

.topbar {
  height: 72px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;

  h1 {
    margin: 0;
    font-size: 22px;
  }
}

.account {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #6b7280;
}

.content {
  padding: 24px;
}
</style>
