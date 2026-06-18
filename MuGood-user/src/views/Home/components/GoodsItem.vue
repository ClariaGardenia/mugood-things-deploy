<script setup>
  defineProps({
    goods:{
      type:Object,
      default: () => {}
    }
  })
</script>

<template>
  <RouterLink :to="`/detail/${goods.id}`" class="goods-item">
    <img v-img-lazy="goods.picture" alt="" />
    <p class="name ellipsis">{{ goods.name }}</p>
    <p class="desc ellipsis">{{ goods.desc }}</p>
    <p class="price">&yen;{{ goods.price }}</p>
  </RouterLink>
</template>

<style scoped lang="scss">
    .goods-item {
      display: block;
      width: 220px;
      padding: 20px 30px;
      text-align: center;
      border-radius: 16px;
      background: #fff;
      border: 1px solid rgba(18, 184, 134, .08);
      transition: all .28s ease;

      &:hover {
        transform: translate3d(0, -6px, 0);
        box-shadow: 0 18px 34px rgba(15, 118, 110, .16);
      }

      img {
        width: 160px;
        height: 160px;
        border-radius: 14px;
        object-fit: cover;
      }

      p {
        padding-top: 10px;
      }

      .name {
        font-size: 16px;
        font-weight: 700;
      }

      .desc {
        color: $mutedColor;
        height: 29px;
      }

      .price {
        color: $priceColor;
        font-size: 20px;
        font-weight: 800;
      }
    }
</style>
