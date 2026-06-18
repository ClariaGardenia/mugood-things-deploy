<script setup>
  import HomeCategory from './components/HomeCategory.vue';
  import HomeBanner from './components/HomeBanner.vue';
  import HomeNew from './components/HomeNew.vue';
  import HomeHot from './components/HomeHot.vue';
  import HomeProduct from './components/HomeProduct.vue';
</script>

<template>
  <div class="container hero-wrap">
    <HomeCategory />
    <HomeBanner />
  </div>
  <HomeNew />
  <HomeHot />
  <HomeProduct />
</template>

<style scoped lang="scss">
.hero-wrap {
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 24px 60px rgba(15, 118, 110, .14);
}
</style>
