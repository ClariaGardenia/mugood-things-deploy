import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login/index.vue'
import Layout from '@/views/Layout/index.vue'
import Home from '@/views/Home/index.vue'
import Category from '@/views/Category/index.vue'
import SubCategory from '@/views/SubCategory/index.vue'
import Detail from '@/views/Detail/index.vue'
import CartList from '@/views/CartList/index.vue'
import Checkout from '@/views/Checkout/index.vue'
import Pay from '@/views/Pay/index.vue'
import PayBack from '@/views/Pay/PayBack.vue'
import Member from '@/views/Member/index.vue'
import UserInfo from '@/views/Member/components/UserInfo.vue'
import UserOrder from '@/views/Member/components/UserOrder.vue'
import { useUserStore } from '@/stores/userStore'

const authRoutes = ['/cartList', '/checkout', '/pay', '/paycallback', '/member']

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: Layout,
      children: [
        {
          path: '',
          component: Home
        },
        {
          path: 'category/:id',
          component: Category
        },
        {
          path: 'category/sub/:id',
          component: SubCategory
        },
        {
          path: 'detail/:id',
          component: Detail
        },
        {
          path: 'datail/:id',
          redirect: to => `/detail/${to.params.id}`
        },
        {
          path: 'cartList',
          component: CartList,
          meta: { requiresAuth: true }
        },
        {
          path: 'checkout',
          component: Checkout,
          meta: { requiresAuth: true }
        },
        {
          path: 'pay',
          component: Pay,
          meta: { requiresAuth: true }
        },
        {
          path: 'paycallback',
          component: PayBack,
          meta: { requiresAuth: true }
        },
        {
          path: 'member',
          component: Member,
          meta: { requiresAuth: true },
          children: [
            {
              path: '',
              component: UserInfo,
              meta: { requiresAuth: true }
            },
            {
              path: 'order',
              component: UserOrder,
              meta: { requiresAuth: true }
            }
          ]
        }
      ]
    },
    {
      path: '/login',
      component: Login
    },
  ],
  // 路由行为配置
  scrollBehavior() {
    return {
      top: 0
    }
  }
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  const isLogin = Boolean(userStore.userInfo?.token)
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
    || authRoutes.some(path => to.path === path || to.path.startsWith(`${path}/`))

  if (requiresAuth && !isLogin) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    }
  }

  if (to.path === '/login' && isLogin) {
    return to.query.redirect || '/'
  }
})

export default router
