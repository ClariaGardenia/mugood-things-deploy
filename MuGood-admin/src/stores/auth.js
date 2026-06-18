import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { loginAdminAPI } from '@/api/admin'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('fresh-rabbit-admin-user') || 'null'))

  const isLogin = computed(() => Boolean(user.value))

  const login = async (form) => {
    const res = await loginAdminAPI(form)
    user.value = res.result
    localStorage.setItem('fresh-rabbit-admin-user', JSON.stringify(user.value))
  }

  const logout = () => {
    user.value = null
    localStorage.removeItem('fresh-rabbit-admin-user')
  }

  return { user, isLogin, login, logout }
})
