<script setup>
import { onMounted, ref } from 'vue'
import { getSummaryAPI } from '@/api/admin'

const loading = ref(false)
const summary = ref({})

const loadSummary = async () => {
  loading.value = true
  try {
    const res = await getSummaryAPI()
    summary.value = res.result
  } finally {
    loading.value = false
  }
}

onMounted(loadSummary)
</script>

<template>
  <div v-loading="loading">
    <div class="metric-grid">
      <div class="metric-card">
        <span>商品数量</span>
        <strong>{{ summary.goodsCount || 0 }}</strong>
      </div>
      <div class="metric-card">
        <span>分类数量</span>
        <strong>{{ summary.categoryCount || 0 }}</strong>
      </div>
      <div class="metric-card">
        <span>订单数量</span>
        <strong>{{ summary.orderCount || 0 }}</strong>
      </div>
      <div class="metric-card">
        <span>销售金额</span>
        <strong>¥{{ Number(summary.salesAmount || 0).toFixed(2) }}</strong>
      </div>
    </div>
    <div class="page-card">
      <h2 class="page-title">运营提示</h2>
      <el-alert title="管理端已连接本地 Spring Boot 后端，可查看商品、分类与订单数据。" type="success" show-icon :closable="false" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 18px;
}

.metric-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;

  span {
    color: #6b7280;
  }

  strong {
    display: block;
    margin-top: 12px;
    font-size: 30px;
  }
}
</style>
