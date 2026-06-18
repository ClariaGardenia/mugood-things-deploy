<script setup>
  import HomePanel from './HomePanel.vue'
  import { getHotAPI } from '@/apis/home'
  import { onMounted, ref } from 'vue'
  const hotList = ref([])
  const getHotList = async () => {
    const res = await getHotAPI()
    hotList.value = res.result
  }
  onMounted(() => {getHotList()})
  

</script>

<template>
  <HomePanel title="人气推荐" sub-title="人气爆款 不容错过">
      <ul class="goods-list">
        <li v-for="item in hotList" :key="item.id">
          <RouterLink :to="`/detail/${item.id}`">
            <img v-img-lazy="item.picture" alt="">
            <p class="name">{{ item.title }}</p>
            <p class="desc">{{ item.alt }}</p>
          </RouterLink>
        </li>
      </ul>
  </HomePanel>
</template>

<style scoped lang='scss'>
.goods-list {
  display: flex;
  height: 426px;
  gap: 16px;

  li {
    flex: 1;
    height: 406px;
    overflow: hidden;
    border-radius: 18px;
    background: #fff;
    border: 1px solid rgba(18, 184, 134, .08);
    box-shadow: 0 10px 26px rgba(15, 118, 110, .06);
    transition: all .28s ease;

    &:hover {
      transform: translate3d(0, -7px, 0);
      box-shadow: 0 20px 44px rgba(15, 118, 110, .15);
    }

    img {
      width: 100%;
      height: 306px;
      object-fit: cover;
    }

    p {
      font-size: 22px;
      padding-top: 12px;
      text-align: center;
    }

    .desc {
      color: $mutedColor;
      font-size: 18px;
    }
  }
}
</style>
