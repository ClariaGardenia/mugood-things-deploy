<script setup>
  import HomePanel from './HomePanel.vue'
  import { getGoodsAPI } from '@/apis/home';
  import { onMounted, ref } from 'vue';
  import GoodsItem from './GoodsItem.vue';

  const goodsProduct = ref([]);
  const getGoods = async () => {
    const res = await getGoodsAPI();
    goodsProduct.value = res.result
  }
  onMounted(() => { getGoods() })
</script>

<template>
  <div class="home-product">
    <HomePanel :title="cate.name" v-for="cate in goodsProduct" :key="cate.id">
      <div class="box">
        <RouterLink class="cover" :to="`/category/${cate.id}`">
          <img v-img-lazy="cate.picture" />
        </RouterLink>
        <ul class="goods-list">
          <li v-for="goods in cate.goods" :key="goods.id">
            <GoodsItem :goods="goods" />
          </li>
        </ul>
      </div>
    </HomePanel>
  </div>
</template>

<style scoped lang='scss'>
.home-product {
  background: transparent;
  margin-top: 20px;
  .sub {
    margin-bottom: 2px;

    a {
      padding: 2px 12px;
      font-size: 16px;
      border-radius: 4px;

      &:hover {
        background: $xtxColor;
        color: #fff;
      }

      &:last-child {
        margin-right: 80px;
      }
    }
  }

  .box {
    display: flex;
    padding: 16px;
    border-radius: 22px;
    background: rgba(255, 255, 255, .94);
    border: 1px solid rgba(18, 184, 134, .08);
    box-shadow: $cardShadow;

    .cover {
      width: 240px;
      height: 610px;
      margin-right: 16px;
      position: relative;
      overflow: hidden;
      border-radius: 18px;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform .35s ease;
      }

      &:hover img {
        transform: scale(1.04);
      }

      .label {
        width: 188px;
        height: 66px;
        display: flex;
        font-size: 18px;
        color: #fff;
        line-height: 66px;
        font-weight: normal;
        position: absolute;
        left: 18px;
        top: 50%;
        transform: translate3d(0, -50%, 0);
        border-radius: 999px;
        overflow: hidden;
        box-shadow: 0 12px 24px rgba(0, 0, 0, .18);

        span {
          text-align: center;

          &:first-child {
            width: 76px;
            background: rgba(18, 184, 134, .95);
          }

          &:last-child {
            flex: 1;
            background: rgba(17, 24, 39, .82);
          }
        }
      }
    }

    .goods-list {
      width: 954px;
      display: flex;
      flex-wrap: wrap;

      li {
        width: 230px;
        height: 300px;
        margin-right: 9px;
        margin-bottom: 10px;

        &:nth-last-child(-n + 4) {
          margin-bottom: 0;
        }

        &:nth-child(4n) {
          margin-right: 0;
        }
      }
    }


  }
}
</style>
