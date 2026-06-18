<script setup>
  import HomePanel from './HomePanel.vue';
  import { findNewAPI } from '@/apis/home';
  import { onMounted, ref } from 'vue';

  // 获取数据
  const newList = ref([]);
  const getNewList = async () => {
    const res = await findNewAPI();    
    newList.value = res.result;
  }
  onMounted(() => { getNewList(); })
</script>

<template>
  <HomePanel title="新鲜好物" sub-title="新鲜出炉 品质靠谱"> 
    <ul class="goods-list">
    <li v-for="item in newList" :key="item.id">
      <RouterLink :to="`/detail/${item.id}`">
        <img :src="item.picture" alt="" />
        <p class="name">{{ item.name }}</p>
        <p class="price">&yen;{{ item.price }}</p>
      </RouterLink>
    </li>
  </ul>
  </HomePanel>  
</template>


<style scoped lang='scss'>
.goods-list {
  display: flex;
  height: 406px;
  gap: 16px;

  li {
    flex: 1;
    height: 406px;
    overflow: hidden;
    border-radius: 18px;
    background: linear-gradient(180deg, #f1fff8 0%, #ffffff 70%);
    border: 1px solid rgba(18, 184, 134, .10);
    box-shadow: 0 10px 26px rgba(15, 118, 110, .07);
    transition: all .28s ease;

    &:hover {
      transform: translate3d(0, -7px, 0);
      box-shadow: 0 20px 44px rgba(15, 118, 110, .16);
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
      text-overflow: ellipsis;
      overflow: hidden;
      white-space: nowrap;
    }

    .price {
      color: $priceColor;
      font-weight: 800;
    }
  }
}
</style>
