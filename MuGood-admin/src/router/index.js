import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Layout from '@/views/layout/AdminLayout.vue'
import Login from '@/views/login/LoginView.vue'
import Dashboard from '@/views/dashboard/DashboardView.vue'
import Goods from '@/views/goods/GoodsView.vue'
import Category from '@/views/category/CategoryView.vue'
import Orders from '@/views/orders/OrdersView.vue'
import Banners from '@/views/banners/BannersView.vue'
import Users from '@/views/users/UsersView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      component: Login
    },
    {
      path: '/',
      component: Layout,
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', component: Dashboard, meta: { title: '数据概览' } },
        { path: 'goods', component: Goods, meta: { title: '商品管理' } },
        { path: 'category', component: Category, meta: { title: '分类管理' } },
        { path: 'orders', component: Orders, meta: { title: '订单管理' } },
        { path: 'banners', component: Banners, meta: { title: '轮播管理' } },
        { path: 'users', component: Users, meta: { title: '用户管理' } }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (to.path !== '/login' && !authStore.isLogin) {
    return '/login'
  }
  if (to.path === '/login' && authStore.isLogin) {
    return '/dashboard'
  }
})

export default router
