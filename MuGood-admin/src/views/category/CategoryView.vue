<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createCategoryAPI, uploadImageAPI, deleteCategoryAPI, getAdminOptionsAPI, getCategoryListAPI, updateCategoryAPI } from '@/api/admin'

const loading = ref(false)
const imageUploading = ref(false)
const dialogVisible = ref(false)
const categories = ref([])
const parentCategories = ref([])
const form = reactive({ id: null, parentId: '', name: '', picture: 'category-admin', saleInfo: '', sortOrder: 0, status: 1 })

const topCategories = computed(() => categories.value.filter(item => item.level === 1))
const availableParentCategories = computed(() => topCategories.value.filter(item => item.id !== form.id))
const categoryTree = computed(() => {
  const categoryMap = new Map()
  categories.value.forEach(item => categoryMap.set(item.id, { ...item, children: [] }))

  const tree = []
  categoryMap.forEach(item => {
    if (item.parentId && categoryMap.has(item.parentId)) {
      categoryMap.get(item.parentId).children.push(item)
    } else {
      tree.push(item)
    }
  })

  const sortByOrder = (list) => {
    list.sort((prev, next) => Number(prev.sortOrder || 0) - Number(next.sortOrder || 0))
    list.forEach(item => {
      if (item.children?.length) sortByOrder(item.children)
      else delete item.children
    })
    return list
  }

  return sortByOrder(tree)
})

const resetForm = () => Object.assign(form, { id: null, parentId: '', name: '', picture: 'category-admin', saleInfo: '', sortOrder: 0, status: 1 })

const loadCategories = async () => {
  loading.value = true
  try {
    const res = await getCategoryListAPI()
    categories.value = res.result
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  const res = await getAdminOptionsAPI()
  parentCategories.value = res.result.parentCategories
}

const openCreate = () => { resetForm(); dialogVisible.value = true }
const openCreateChild = (row) => {
  resetForm()
  form.parentId = row.id
  dialogVisible.value = true
}
const openEdit = (row) => { Object.assign(form, row, { parentId: row.parentId || '' }); dialogVisible.value = true }

const submit = async () => {
  if (!form.name) {
    ElMessage.warning('请填写分类名称')
    return
  }
  if (form.id) {
    await updateCategoryAPI(form.id, form)
    ElMessage.success('分类已更新')
  } else {
    await createCategoryAPI(form)
    ElMessage.success('分类已新增')
  }
  dialogVisible.value = false
  await loadCategories()
  await loadOptions()
}
const uploadPicture = async ({ file }) => {
  imageUploading.value = true
  try {
    const res = await uploadImageAPI(file)
    form.picture = res.result.url
    ElMessage.success('图片上传成功')
  } finally {
    imageUploading.value = false
  }
}

const removeCategory = async (row) => {
  await ElMessageBox.confirm(`确认停用分类「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteCategoryAPI(row.id)
  ElMessage.success('分类已停用')
  loadCategories()
}

onMounted(async () => { await loadCategories(); await loadOptions() })
</script>

<template>
  <div class="page-card">
    <div class="toolbar">
      <h2 class="page-title">分类管理</h2>
      <el-button type="success" @click="openCreate">新增分类</el-button>
    </div>
    <el-table
      v-loading="loading"
      :data="categoryTree"
      row-key="id"
      default-expand-all
      :tree-props="{ children: 'children' }"
    >
      <el-table-column label="分类" min-width="260">
        <template #default="{ row }">
          <div class="category-cell" :class="{ parent: row.level === 1, child: row.level === 2 }">
            <img :src="row.picture" alt="">
            <div>
              <strong>{{ row.name }}</strong>
              <p>{{ row.parentName || '一级分类' }}</p>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="层级" width="110">
        <template #default="{ row }">
          <el-tag :type="row.level === 1 ? 'primary' : 'success'">{{ row.level === 1 ? '一级分类' : '二级分类' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="230">
        <template #default="{ row }">
          <el-button v-if="row.level === 1" link type="success" @click="openCreateChild(row)">新增子分类</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="removeCategory(row)">停用</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="上级分类"><el-select v-model="form.parentId" clearable placeholder="不选则为一级分类"><el-option v-for="item in availableParentCategories" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="分类名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="图片标识">
          <div class="upload-box">
            <el-upload accept="image/*" :show-file-list="false" :http-request="uploadPicture">
              <el-button :loading="imageUploading" type="primary">上传图片</el-button>
            </el-upload>
            <img v-if="form.picture" class="form-preview" :src="form.picture" alt="">
          </div>
        </el-form-item>
        <el-form-item label="销售文案"><el-input v-model="form.saleInfo" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.category-cell { display: flex; align-items: center; gap: 12px; }
.category-cell.parent strong { font-size: 15px; }
.category-cell.child img { width: 44px; height: 44px; }
.category-cell img { width: 52px; height: 52px; border-radius: 6px; object-fit: cover; }
.category-cell p { margin: 6px 0 0; color: #6b7280; }
.upload-box { width: 100%; display: grid; gap: 10px; }
.form-preview { width: 120px; height: 120px; border-radius: 8px; object-fit: cover; border: 1px solid #e5e7eb; }
:deep(.el-table__placeholder) { width: 24px; }
:deep(.el-table__indent) { padding-left: 20px; }
</style>
