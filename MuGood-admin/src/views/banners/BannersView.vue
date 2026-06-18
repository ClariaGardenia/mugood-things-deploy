<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createBannerAPI, deleteBannerAPI, getBannerListAPI, updateBannerAPI, uploadImageAPI } from '@/api/admin'

const loading = ref(false)
const imageUploading = ref(false)
const dialogVisible = ref(false)
const banners = ref([])
const form = reactive({ id: null, title: '', imgUrl: '', hrefUrl: '/', distributionSite: 1, sortOrder: 0, status: 1 })

const resetForm = () => Object.assign(form, { id: null, title: '', imgUrl: '', hrefUrl: '/', distributionSite: 1, sortOrder: 0, status: 1 })

const loadBanners = async () => {
  loading.value = true
  try {
    const res = await getBannerListAPI()
    banners.value = res.result || []
  } finally {
    loading.value = false
  }
}

const openCreate = () => { resetForm(); dialogVisible.value = true }
const openEdit = (row) => { Object.assign(form, row); dialogVisible.value = true }

const uploadPicture = async ({ file }) => {
  imageUploading.value = true
  try {
    const res = await uploadImageAPI(file)
    form.imgUrl = res.result.url
    ElMessage.success('图片上传成功')
  } finally {
    imageUploading.value = false
  }
}

const submit = async () => {
  if (!form.title || !form.imgUrl) {
    ElMessage.warning('请填写轮播标题并上传图片')
    return
  }
  if (form.id) {
    await updateBannerAPI(form.id, form)
    ElMessage.success('轮播已更新')
  } else {
    await createBannerAPI(form)
    ElMessage.success('轮播已新增')
  }
  dialogVisible.value = false
  loadBanners()
}

const removeBanner = async (row) => {
  await ElMessageBox.confirm(`确认停用轮播「${row.title}」吗？`, '提示', { type: 'warning' })
  await deleteBannerAPI(row.id)
  ElMessage.success('轮播已停用')
  loadBanners()
}

onMounted(loadBanners)
</script>

<template>
  <div class="page-card">
    <div class="toolbar">
      <h2 class="page-title">轮播管理</h2>
      <el-button type="success" @click="openCreate">新增轮播</el-button>
    </div>
    <el-table v-loading="loading" :data="banners" row-key="id">
      <el-table-column prop="id" label="编号" />
      <el-table-column label="图片" width="150"><template #default="{ row }"><img class="banner-img" :src="row.imgUrl" alt=""></template></el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="hrefUrl" label="跳转地址" min-width="160" />
      <el-table-column prop="distributionSite" label="投放位置" width="100" />
      <el-table-column prop="sortOrder" label="排序" width="90" />
      <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="150"><template #default="{ row }"><el-button link type="primary" @click="openEdit(row)">编辑</el-button><el-button link type="danger" @click="removeBanner(row)">停用</el-button></template></el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑轮播' : '新增轮播'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="轮播图片">
          <div class="upload-box">
            <el-upload accept="image/*" :show-file-list="false" :http-request="uploadPicture">
              <el-button :loading="imageUploading" type="primary">上传图片</el-button>
            </el-upload>
            <img v-if="form.imgUrl" class="form-preview" :src="form.imgUrl" alt="">
          </div>
        </el-form-item>
        <el-form-item label="跳转地址"><el-input v-model="form.hrefUrl" /></el-form-item>
        <el-form-item label="投放位置"><el-input-number v-model="form.distributionSite" :min="1" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.banner-img { width: 120px; height: 58px; border-radius: 6px; object-fit: cover; }
.upload-box { width: 100%; display: grid; gap: 10px; }
.form-preview { width: 180px; height: 88px; border-radius: 8px; object-fit: cover; border: 1px solid #e5e7eb; }
</style>
