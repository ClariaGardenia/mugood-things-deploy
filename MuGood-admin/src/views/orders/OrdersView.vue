<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrderPageAPI, updateOrderStateAPI } from '@/api/admin'

const loading = ref(false)
const orders = ref([])
const total = ref(0)
const query = reactive({
  page: 1,
  pageSize: 10,
  orderState: 0
})

const stateMap = {
  1: '待付款',
  2: '待发货',
  3: '待收货',
  4: '待评价',
  5: '已完成',
  6: '已取消'
}

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getOrderPageAPI(query)
    orders.value = res.result.items
    total.value = res.result.counts
  } finally {
    loading.value = false
  }
}

const filterOrders = () => {
  query.page = 1
  loadOrders()
}

const nextActions = (state) => {
  if (state === 1) return [{ label: '标记已付款', value: 2 }]
  if (state === 2) return [{ label: '发货', value: 3 }]
  if (state === 3) return [{ label: '确认收货', value: 5 }]
  return []
}

const updateState = async (row, state) => {
  await updateOrderStateAPI(row.id, state)
  ElMessage.success('订单状态已更新')
  loadOrders()
}

onMounted(loadOrders)
</script>

<template>
  <div class="page-card">
    <div class="toolbar">
      <h2 class="page-title">订单管理</h2>
      <el-select v-model="query.orderState" style="width: 150px" @change="filterOrders">
        <el-option label="全部订单" :value="0" />
        <el-option label="待付款" :value="1" />
        <el-option label="待发货" :value="2" />
        <el-option label="待收货" :value="3" />
        <el-option label="待评价" :value="4" />
        <el-option label="已完成" :value="5" />
        <el-option label="已取消" :value="6" />
      </el-select>
    </div>
    <el-table v-loading="loading" :data="orders" row-key="id">
      <el-table-column prop="id" label="编号" />
      <el-table-column prop="orderNo" label="订单号" min-width="170" />
      <el-table-column prop="receiver" label="收货人" width="110" />
      <el-table-column prop="contact" label="电话" width="140" />
      <el-table-column prop="goodsCount" label="商品数" width="90" />
      <el-table-column label="实付金额" width="120">
        <template #default="{ row }">¥{{ Number(row.payMoney).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag>{{ stateMap[row.orderState] || '未知' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="180" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button v-for="item in nextActions(row.orderState)" :key="item.value" link type="primary" @click="updateState(row, item.value)">{{ item.label }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination
        v-model:current-page="query.page"
        :page-size="query.pageSize"
        background
        layout="prev, pager, next, total"
        :total="total"
        @current-change="loadOrders"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
