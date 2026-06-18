<script setup>
  import LayoutNav from './components/LayoutNav.vue'
  import LayoutHeader from './components/LayoutHeader.vue'
  import LayoutFooter from './components/LayoutFooter.vue'
  import LayoutFixed from './components/LayoutFixed.vue'
  import CustomerServiceFloat from '@/components/CustomerServiceFloat/index.vue'

  // 触发获取导航列表的action
  import { useCategoryStore } from '@/stores/categoryStore' 
  import { onMounted } from 'vue'
  const categoryStore = useCategoryStore()
  onMounted (() => { categoryStore.getCategory() })
</script>

<template>
  <LayoutFixed />
  <LayoutNav />
  <LayoutHeader />
  <RouterView :key="$route.fullPath"/>
  <LayoutFooter />
  <CustomerServiceFloat />
</template>
