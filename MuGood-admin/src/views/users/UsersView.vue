<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserPageAPI, updateUserPasswordAPI, updateUserStatusAPI } from '@/api/admin'

const loading = ref(false)
const users = ref([])
const total = ref(0)
const passwordDialogVisible = ref(false)
const query = reactive({ page: 1, pageSize: 10, keyword: '' })
const passwordForm = reactive({ id: null, account: '', password: '', confirmPassword: '' })

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getUserPageAPI(query)
    users.value = res.result.items
    total.value = res.result.counts
  } finally {
    loading.value = false
  }
}

const search = () => { query.page = 1; loadUsers() }

const openPasswordDialog = (row) => {
  Object.assign(passwordForm, {
    id: row.id,
    account: row.account,
    password: '',
    confirmPassword: ''
  })
  passwordDialogVisible.value = true
}

const submitPassword = async () => {
  if (!passwordForm.password || passwordForm.password.length < 6 || passwordForm.password.length > 32) {
    ElMessage.warning('密码长度需为 6 到 32 位')
    return
  }
  if (passwordForm.password !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  await updateUserPasswordAPI(passwordForm.id, passwordForm.password)
  ElMessage.success('用户密码已更新')
  passwordDialogVisible.value = false
}

const toggleStatus = async (row) => {
  const nextStatus = row.status === 1 ? 0 : 1
  await updateUserStatusAPI(row.id, nextStatus)
  ElMessage.success(nextStatus === 1 ? '用户已启用' : '用户已禁用')
  loadUsers()
}

onMounted(loadUsers)
</script>

<template>
  <div class="page-card">
    <div class="toolbar">
      <h2 class="page-title">用户管理</h2>
      <div class="actions"><el-input v-model="query.keyword" clearable placeholder="账号/昵称/手机号" @keyup.enter="search" /><el-button type="primary" @click="search">查询</el-button></div>
    </div>
    <el-table v-loading="loading" :data="users" row-key="id">
      <el-table-column prop="id" label="编号" />
      <el-table-column prop="account" label="账号" min-width="150" />
      <el-table-column prop="nickname" label="昵称" min-width="140" />
      <el-table-column prop="mobile" label="手机号" width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template></el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="openPasswordDialog(row)">修改密码</el-button>
          <el-button link type="primary" @click="toggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager"><el-pagination v-model:current-page="query.page" :page-size="query.pageSize" background layout="prev, pager, next, total" :total="total" @current-change="loadUsers" /></div>

    <el-dialog v-model="passwordDialogVisible" title="修改用户密码" width="420px">
      <el-form label-width="90px">
        <el-form-item label="账号">
          <el-input v-model="passwordForm.account" disabled />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.password" type="password" show-password maxlength="32" placeholder="请输入 6-32 位新密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password maxlength="32" placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPassword">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.actions { width: 360px; display: flex; gap: 10px; }
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
