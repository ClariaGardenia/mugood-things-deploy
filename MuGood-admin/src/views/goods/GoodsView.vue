<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createGoodsAPI,
  deleteGoodsAPI,
  getAdminOptionsAPI,
  getGoodsDetailAPI,
  getGoodsPageAPI,
  uploadImageAPI,
  updateGoodsAPI,
  updateGoodsStatusAPI
} from '@/api/admin'

const loading = ref(false)
const detailLoading = ref(false)
const goods = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const imageUploading = ref(false)
const options = ref({ leafCategories: [], brands: [] })
const query = reactive({ page: 1, pageSize: 10, keyword: '' })
const form = reactive(createEmptyForm())

function createEmptyForm() {
  return {
    id: null,
    name: '',
    desc: '',
    categoryId: '',
    brandId: '',
    price: 0,
    oldPrice: 0,
    inventory: 0,
    picture: '',
    isNew: 0,
    isHot: 0,
    status: 1,
    mainPictures: [],
    detailPictures: [],
    properties: [],
    specs: [],
    skus: []
  }
}

const pictureItem = () => ({ url: '', sortOrder: 1 })
const propertyItem = () => ({ name: '', value: '', sortOrder: 1 })
const specItem = () => ({ name: '', sortOrder: 1, values: [{ name: '', picture: '', sortOrder: 1 }] })
const skuItem = () => ({ skuCode: '', price: form.price, oldPrice: form.oldPrice, inventory: form.inventory, picture: form.picture, status: 1, specs: [] })

const normalizeSort = (list) => list.map((item, index) => ({ ...item, sortOrder: item.sortOrder || index + 1 }))

const resetForm = () => {
  Object.assign(form, createEmptyForm(), {
    categoryId: options.value.leafCategories[0]?.id || '',
    brandId: options.value.brands[0]?.id || '',
    mainPictures: [pictureItem()],
    detailPictures: [pictureItem()],
    properties: [propertyItem()],
    specs: [specItem()],
    skus: [skuItem()]
  })
}

const loadOptions = async () => {
  const res = await getAdminOptionsAPI()
  options.value = res.result || { leafCategories: [], brands: [] }
}

const loadGoods = async () => {
  loading.value = true
  try {
    const res = await getGoodsPageAPI(query)
    goods.value = res.result?.items || []
    total.value = res.result?.counts || 0
  } finally {
    loading.value = false
  }
}

const search = () => {
  query.page = 1
  loadGoods()
}

const uploadPicture = (setter) => {
  return async ({ file }) => {
    imageUploading.value = true
    try {
      const res = await uploadImageAPI(file)
      setter(res.result.url)
      ElMessage.success('图片上传成功')
    } finally {
      imageUploading.value = false
    }
  }
}

const setMainPicture = (url) => {
  form.picture = url
  if (!form.mainPictures.length) form.mainPictures.push(pictureItem())
  form.mainPictures[0].url = url
  form.skus.forEach(sku => {
    if (!sku.picture) sku.picture = url
  })
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = async (row) => {
  detailLoading.value = true
  dialogVisible.value = true
  try {
    const res = await getGoodsDetailAPI(row.id)
    const detail = res.result || {}
    Object.assign(form, createEmptyForm(), {
      id: detail.id,
      name: detail.name,
      desc: detail.desc,
      categoryId: detail.categoryId || '',
      brandId: detail.brandId || '',
      price: detail.price,
      oldPrice: detail.oldPrice,
      inventory: detail.inventory,
      picture: detail.picture,
      isNew: detail.isNew || 0,
      isHot: detail.isHot || 0,
      status: detail.status,
      mainPictures: detail.mainPictures?.length ? detail.mainPictures : [{ url: detail.picture, sortOrder: 1 }],
      detailPictures: detail.detailPictures?.length ? detail.detailPictures : [pictureItem()],
      properties: detail.properties?.length ? detail.properties : [propertyItem()],
      specs: detail.specs?.length ? detail.specs : [specItem()],
      skus: detail.skus?.length ? detail.skus : [skuItem()]
    })
  } finally {
    detailLoading.value = false
  }
}

const addMainPicture = () => form.mainPictures.push({ ...pictureItem(), sortOrder: form.mainPictures.length + 1 })
const addDetailPicture = () => form.detailPictures.push({ ...pictureItem(), sortOrder: form.detailPictures.length + 1 })
const addProperty = () => form.properties.push({ ...propertyItem(), sortOrder: form.properties.length + 1 })
const addSpec = () => form.specs.push({ ...specItem(), sortOrder: form.specs.length + 1 })
const addSpecValue = (spec) => spec.values.push({ name: '', picture: '', sortOrder: spec.values.length + 1 })
const addSku = () => form.skus.push(skuItem())

const removeAt = (list, index, fallbackFactory) => {
  list.splice(index, 1)
  if (!list.length && fallbackFactory) list.push(fallbackFactory())
}

const generateSkus = () => {
  const validSpecs = form.specs
    .filter(spec => spec.name && spec.values.some(value => value.name))
    .map(spec => ({ name: spec.name, values: spec.values.filter(value => value.name) }))

  if (!validSpecs.length) {
    form.skus = [skuItem()]
    return
  }

  const groups = validSpecs.reduce((result, spec) => {
    if (!result.length) {
      return spec.values.map(value => [{ name: spec.name, valueName: value.name, picture: value.picture }])
    }
    return result.flatMap(row => spec.values.map(value => [...row, { name: spec.name, valueName: value.name, picture: value.picture }]))
  }, [])

  form.skus = groups.map((specs, index) => ({
    skuCode: `GOODS-${form.id || 'NEW'}-${index + 1}`,
    price: form.price,
    oldPrice: form.oldPrice,
    inventory: form.inventory,
    picture: specs.find(item => item.picture)?.picture || form.picture,
    status: 1,
    specs
  }))
}

const payload = () => ({
  ...form,
  mainPictures: normalizeSort(form.mainPictures.filter(item => item.url)),
  detailPictures: normalizeSort(form.detailPictures.filter(item => item.url)),
  properties: normalizeSort(form.properties.filter(item => item.name && item.value)),
  specs: normalizeSort(form.specs.filter(item => item.name).map(spec => ({
    ...spec,
    values: normalizeSort((spec.values || []).filter(value => value.name))
  }))),
  skus: form.skus.filter(item => item.inventory !== '' && item.price !== '').map(item => ({
    ...item,
    specs: (item.specs || []).filter(spec => spec.name && spec.valueName)
  }))
})

const submit = async () => {
  const data = payload()
  if (!data.name || !data.categoryId || !data.brandId || !data.picture) {
    ElMessage.warning('请填写商品名称、分类、品牌并上传主图')
    return
  }
  if (!data.mainPictures.length || !data.detailPictures.length) {
    ElMessage.warning('请至少维护一张轮播图和一张详情图')
    return
  }
  if (data.id) {
    await updateGoodsAPI(data.id, data)
    ElMessage.success('商品已更新')
  } else {
    await createGoodsAPI(data)
    ElMessage.success('商品已新增')
  }
  dialogVisible.value = false
  loadGoods()
}

const toggleStatus = async (row) => {
  const nextStatus = row.status === 1 ? 0 : 1
  await updateGoodsStatusAPI(row.id, nextStatus)
  ElMessage.success(nextStatus === 1 ? '商品已上架' : '商品已下架')
  loadGoods()
}

const removeGoods = async (row) => {
  await ElMessageBox.confirm(`确认下架商品「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteGoodsAPI(row.id)
  ElMessage.success('商品已下架')
  loadGoods()
}

onMounted(async () => {
  await loadOptions()
  resetForm()
  loadGoods()
})
</script>

<template>
  <div class="page-card">
    <div class="toolbar">
      <h2 class="page-title">商品管理</h2>
      <div class="actions">
        <el-input v-model="query.keyword" clearable placeholder="搜索商品名称" @keyup.enter="search" />
        <el-button type="primary" @click="search">查询</el-button>
        <el-button type="success" @click="openCreate">新增商品</el-button>
      </div>
    </div>
    <el-table v-loading="loading" :data="goods" row-key="id">
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column label="商品" min-width="320">
        <template #default="{ row }">
          <div class="goods-cell">
            <img :src="row.picture" alt="">
            <div>
              <strong>{{ row.name }}</strong>
              <p>{{ row.desc }}</p>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column prop="brandName" label="品牌" width="130" />
      <el-table-column label="售价" width="110">
        <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="inventory" label="库存" width="90" />
      <el-table-column prop="salesCount" label="销量" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="190" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑详情</el-button>
          <el-button link type="primary" @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
          <el-button link type="danger" @click="removeGoods(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination v-model:current-page="query.page" :page-size="query.pageSize" background layout="prev, pager, next, total" :total="total" @current-change="loadGoods" />
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品完整详情' : '新增商品完整详情'" width="980px">
      <el-form v-loading="detailLoading" label-width="92px">
        <el-tabs>
          <el-tab-pane label="基础信息">
            <div class="form-grid">
              <el-form-item label="商品名称"><el-input v-model="form.name" /></el-form-item>
              <el-form-item label="商品描述"><el-input v-model="form.desc" /></el-form-item>
              <el-form-item label="分类"><el-select v-model="form.categoryId" filterable><el-option v-for="item in options.leafCategories" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
              <el-form-item label="品牌"><el-select v-model="form.brandId" filterable><el-option v-for="item in options.brands" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
              <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
              <el-form-item label="原价"><el-input-number v-model="form.oldPrice" :min="0" :precision="2" /></el-form-item>
              <el-form-item label="总库存"><el-input-number v-model="form.inventory" :min="0" /></el-form-item>
              <el-form-item label="主图">
                <div class="upload-box">
                  <el-upload accept="image/*" :show-file-list="false" :http-request="uploadPicture(setMainPicture)">
                    <el-button :loading="imageUploading" type="primary">上传主图</el-button>
                  </el-upload>
                  <img v-if="form.picture" class="form-preview" :src="form.picture" alt="">
                </div>
              </el-form-item>
            </div>
            <el-form-item label="标签">
              <el-checkbox v-model="form.isNew" :true-value="1" :false-value="0">新品</el-checkbox>
              <el-checkbox v-model="form.isHot" :true-value="1" :false-value="0">热销</el-checkbox>
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane label="图片详情">
            <section class="detail-section">
              <div class="section-head"><strong>商品轮播图</strong><el-button link type="primary" @click="addMainPicture">新增轮播图</el-button></div>
              <div class="image-grid">
                <div v-for="(item, index) in form.mainPictures" :key="index" class="image-card">
                  <img v-if="item.url" :src="item.url" alt="">
                  <div v-else class="image-empty">未上传</div>
                  <div class="image-actions">
                    <el-upload accept="image/*" :show-file-list="false" :http-request="uploadPicture(url => item.url = url)">
                      <el-button :loading="imageUploading" size="small">上传图片</el-button>
                    </el-upload>
                    <el-input-number v-model="item.sortOrder" :min="1" size="small" controls-position="right" />
                    <el-button link type="danger" @click="removeAt(form.mainPictures, index, pictureItem)">删除</el-button>
                  </div>
                </div>
              </div>
            </section>
            <section class="detail-section">
              <div class="section-head"><strong>详情长图</strong><el-button link type="primary" @click="addDetailPicture">新增详情图</el-button></div>
              <div class="image-grid detail">
                <div v-for="(item, index) in form.detailPictures" :key="index" class="image-card">
                  <img v-if="item.url" :src="item.url" alt="">
                  <div v-else class="image-empty">未上传</div>
                  <div class="image-actions">
                    <el-upload accept="image/*" :show-file-list="false" :http-request="uploadPicture(url => item.url = url)">
                      <el-button :loading="imageUploading" size="small">上传图片</el-button>
                    </el-upload>
                    <el-input-number v-model="item.sortOrder" :min="1" size="small" controls-position="right" />
                    <el-button link type="danger" @click="removeAt(form.detailPictures, index, pictureItem)">删除</el-button>
                  </div>
                </div>
              </div>
            </section>
          </el-tab-pane>

          <el-tab-pane label="商品参数">
            <div class="section-head"><strong>详情参数</strong><el-button link type="primary" @click="addProperty">新增参数</el-button></div>
            <div v-for="(item, index) in form.properties" :key="index" class="property-row">
              <span>参数名：<el-input v-model="item.name" placeholder="参数名，如 材质" /></span>
              <span>参数值：<el-input v-model="item.value" placeholder="参数值，如 100%棉"  /></span>
              <span>排序：<el-input-number v-model="item.sortOrder" :min="1" /></span>
              <el-button link type="danger" @click="removeAt(form.properties, index, propertyItem)">删除</el-button>
            </div>
          </el-tab-pane>

          <el-tab-pane label="规格">
            <div class="section-head">
              <div>
                <strong>规格组和值</strong>
                <p class="section-tip">例如：规格组填「颜色」，规格值填「白色、藏青」；规格组填「尺码」，规格值填「M、L」。</p>
              </div>
              <el-button type="primary" plain @click="addSpec">新增规格组</el-button>
            </div>
            <div v-for="(spec, specIndex) in form.specs" :key="specIndex" class="spec-card">
              <div class="spec-head">
                <div class="field spec-name-field">
                  <span>规格组名称</span>
                  <el-input v-model="spec.name" placeholder="如：颜色 / 尺码" />
                </div>
                <div class="field sort-field">
                  <span>排序</span>
                  <el-input-number v-model="spec.sortOrder" :min="1" controls-position="right" />
                </div>
                <div class="row-actions">
                  <el-button type="primary" plain @click="addSpecValue(spec)">新增规格值</el-button>
                  <el-button type="danger" plain @click="removeAt(form.specs, specIndex, specItem)">删除规格组</el-button>
                </div>
              </div>
              <div class="spec-value-header">
                <span>规格值名称</span>
                <span>图片</span>
                <span>排序</span>
                <span>操作</span>
              </div>
              <div v-for="(value, valueIndex) in spec.values" :key="valueIndex" class="spec-value-row">
                <el-input v-model="value.name" placeholder="如：白色 / M" />
                <el-upload accept="image/*" :show-file-list="false" :http-request="uploadPicture(url => value.picture = url)">
                  <el-button :loading="imageUploading" plain>上传图片</el-button>
                </el-upload>
                <el-input-number v-model="value.sortOrder" :min="1" controls-position="right" />
                <div class="preview-slot">
                  <img v-if="value.picture" class="tiny-preview" :src="value.picture" alt="">
                  <span v-else>无图</span>
                </div>
                <el-button link type="danger" @click="removeAt(spec.values, valueIndex, () => ({ name: '', picture: '', sortOrder: 1 }))">删除</el-button>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="SKU库存">
            <div class="section-head">
              <div>
                <strong>SKU 库存</strong>
                <p class="section-tip">每一行代表一个可购买组合，例如「白色 + M」。价格、库存会用于用户端下单。</p>
              </div>
              <div>
                <el-button type="primary" plain @click="generateSkus">根据规格生成 SKU</el-button>
                <el-button type="primary" plain @click="addSku">手动新增 SKU</el-button>
              </div>
            </div>
            <div v-for="(sku, skuIndex) in form.skus" :key="skuIndex" class="sku-card">
              <div class="sku-title">
                <strong>SKU {{ skuIndex + 1 }}</strong>
                <el-button link type="danger" @click="removeAt(form.skus, skuIndex, skuItem)">删除</el-button>
              </div>
              <div class="sku-fields">
                <div class="field code-field">
                  <span>SKU 编码</span>
                  <el-input v-model="sku.skuCode" placeholder="留空则自动生成" />
                </div>
                <div class="field number-field">
                  <span>销售价</span>
                  <el-input-number v-model="sku.price" :min="0" :precision="2" controls-position="right" />
                </div>
                <div class="field number-field">
                  <span>原价</span>
                  <el-input-number v-model="sku.oldPrice" :min="0" :precision="2" controls-position="right" />
                </div>
                <div class="field number-field">
                  <span>库存</span>
                  <el-input-number v-model="sku.inventory" :min="0" controls-position="right" />
                </div>
                <div class="field status-field">
                  <span>状态</span>
                  <el-switch v-model="sku.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
                </div>
              </div>
              <div class="sku-subtitle">规格组合</div>
              <div class="sku-specs">
                <template v-for="spec in form.specs.filter(item => item.name)" :key="spec.name">
                  <div class="field spec-select-field">
                    <span>{{ spec.name }}</span>
                    <el-select
                      :model-value="sku.specs.find(item => item.name === spec.name)?.valueName"
                      :placeholder="`选择${spec.name}`"
                      @change="value => {
                        const exists = sku.specs.find(item => item.name === spec.name)
                        if (exists) exists.valueName = value
                        else sku.specs.push({ name: spec.name, valueName: value })
                      }"
                    >
                      <el-option v-for="value in spec.values.filter(item => item.name)" :key="value.name" :label="value.name" :value="value.name" />
                    </el-select>
                  </div>
                </template>
              </div>
              <div class="sku-image">
                <span>SKU 图片</span>
                <el-upload accept="image/*" :show-file-list="false" :http-request="uploadPicture(url => sku.picture = url)">
                  <el-button :loading="imageUploading" plain>上传图片</el-button>
                </el-upload>
                <img v-if="sku.picture" class="small-preview" :src="sku.picture" alt="">
                <span v-else class="image-placeholder">未上传</span>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存完整商品</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.actions { display: flex; gap: 10px; width: 520px; }
.goods-cell { display: flex; gap: 12px; align-items: center; }
.goods-cell img { width: 64px; height: 64px; border-radius: 6px; object-fit: cover; }
.goods-cell p { margin: 6px 0 0; color: #6b7280; }
.pager { display: flex; justify-content: flex-end; margin-top: 16px; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 18px; }
.upload-box { width: 100%; display: grid; gap: 10px; }
.form-preview { width: 120px; height: 120px; border-radius: 8px; object-fit: cover; border: 1px solid #e5e7eb; }
.detail-section { margin-bottom: 18px; padding: 16px; border: 1px solid #e5e7eb; border-radius: 12px; background: #fafafa; }
.section-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.section-tip { margin: 6px 0 0; color: #8a9099; font-size: 13px; }
.image-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.image-grid.detail { grid-template-columns: repeat(3, 1fr); }
.image-card { padding: 10px; border: 1px solid #e5e7eb; border-radius: 12px; background: #fff; }
.image-card img, .image-empty { width: 100%; height: 130px; border-radius: 8px; object-fit: cover; background: #f3f4f6; }
.image-grid.detail .image-card img, .image-grid.detail .image-empty { height: 190px; }
.image-empty { display: grid; place-items: center; color: #9ca3af; font-size: 13px; border: 1px dashed #d1d5db; }
.image-actions { display: grid; grid-template-columns: 1fr 92px auto; gap: 8px; align-items: center; margin-top: 10px; }
.image-actions :deep(.el-input-number) { width: 92px; }
.image-row, .property-row { display: grid; grid-template-columns: auto auto auto auto; gap: 10px; align-items: center; margin-bottom: 10px; }
.property-row { grid-template-columns: auto auto auto auto; }
.spec-card, .sku-card { padding: 16px; margin-bottom: 14px; border: 1px solid #e5e7eb; border-radius: 12px; background: #fff; box-shadow: 0 6px 18px rgba(15, 23, 42, .04); }
.spec-head { display: grid; grid-template-columns: 260px 120px 1fr; gap: 14px; align-items: end; margin-bottom: 14px; padding-bottom: 14px; border-bottom: 1px dashed #e5e7eb; }
.row-actions { display: flex; justify-content: flex-end; gap: 8px; }
.field { display: grid; gap: 6px; min-width: 0; }
.field > span, .sku-image > span, .sku-subtitle { color: #606266; font-size: 13px; font-weight: 600; }
.sort-field :deep(.el-input-number), .number-field :deep(.el-input-number) { width: 118px; }
.spec-value-header { display: grid; grid-template-columns: 220px 110px 100px 58px 52px; gap: 10px; padding: 0 12px 8px; color: #909399; font-size: 12px; }
.spec-value-row { display: grid; grid-template-columns: 220px 110px 100px 58px 52px; gap: 10px; align-items: center; margin-bottom: 10px; padding: 10px 12px; border-radius: 10px; background: #f9fafb; }
.spec-value-row :deep(.el-input-number) { width: 100px; }
.preview-slot { width: 52px; height: 42px; display: grid; place-items: center; color: #a8abb2; font-size: 12px; }
.sku-title { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.sku-fields { display: grid; grid-template-columns: 260px 118px 118px 118px 110px; gap: 14px; align-items: end; padding-bottom: 14px; border-bottom: 1px dashed #e5e7eb; }
.sku-fields :deep(.el-input-number) { width: 118px; }
.status-field { min-height: 54px; align-content: end; }
.sku-subtitle { margin: 14px 0 8px; }
.sku-specs { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 14px; }
.spec-select-field { width: 160px; }
.sku-image { display: flex; align-items: center; gap: 12px; min-height: 76px; padding: 10px 12px; border-radius: 10px; background: #f9fafb; }
.small-preview { width: 72px; height: 72px; border-radius: 8px; object-fit: cover; border: 1px solid #e5e7eb; }
.tiny-preview { width: 42px; height: 42px; border-radius: 6px; object-fit: cover; border: 1px solid #e5e7eb; }
.image-placeholder { color: #a8abb2; font-size: 13px; }
:deep(.el-dialog__body) { padding-top: 8px; }
:deep(.el-tabs__content) { max-height: 62vh; overflow-y: auto; padding-right: 6px; }
</style>
