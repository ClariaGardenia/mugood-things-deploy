<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  account: 'admin',
  password: '123456'
})

const submit = async () => {
  if (!form.account || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  await authStore.login(form)
  ElMessage.success('登录成功')
  router.replace('/dashboard')
}
</script>

<template>
  <div class="login-page">
    <section class="login-panel">
      <div class="intro">
        <strong>MU GOOD</strong>
        <span>商城管理端</span>
      </div>
      <el-form class="form" @submit.prevent>
        <el-form-item>
          <el-input v-model="form.account" size="large" placeholder="账号" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" size="large" placeholder="密码" type="password" show-password />
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" @click="submit">登录</el-button>
      </el-form>
    </section>
  </div>
</template>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #0f172a, #134e4a);
}

.login-panel {
  width: 380px;
  background: #fff;
  border-radius: 8px;
  padding: 34px;
  box-shadow: 0 24px 70px rgba(0, 0, 0, .28);
}

.intro {
  margin-bottom: 28px;

  strong {
    display: block;
    font-size: 28px;
  }

  span {
    color: #6b7280;
    margin-top: 6px;
    display: block;
  }
}

.login-btn {
  width: 100%;
}
</style>
