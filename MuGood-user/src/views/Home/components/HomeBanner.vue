<script setup>
  import { getBannerAPI } from '@/apis/home';
  import { ref,onMounted } from 'vue';

  const bannerList = ref([]);

  const getBanner = async () => {
    const res = await getBannerAPI();
    bannerList.value = res.result
  };
  onMounted(() => getBanner())
</script>

<template>
  <div class="home-banner">
    <el-carousel height="500px" arrow="never">
      <el-carousel-item v-for="item in bannerList" :key="item.id">
        <img :src="item.imgUrl" alt="">
      </el-carousel-item>
    </el-carousel>
  </div>
</template>



<style scoped lang='scss'>
.home-banner {
  width: 1240px;
  height: 500px;
  position: absolute;
  left: 0;
  top: 0;
  z-index: 98;
  border-radius: 24px;
  overflow: hidden;

  img {
    width: 100%;
    height: 500px;
    object-fit: cover;
  }

  :deep(.el-carousel__indicator .el-carousel__button) {
    width: 22px;
    height: 5px;
    border-radius: 999px;
    background: rgba(255, 255, 255, .85);
  }

  :deep(.el-carousel__indicator.is-active .el-carousel__button) {
    width: 34px;
    background: $xtxColor;
  }
}
</style>
