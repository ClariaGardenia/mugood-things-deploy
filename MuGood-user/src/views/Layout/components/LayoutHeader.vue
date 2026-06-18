<script setup>
  import { useCategoryStore } from '@/stores/categoryStore'
  import handerCart from './handerCart.vue';
  // 使用pinia中的数据
  const categoryStore = useCategoryStore()
</script>

<template>
  <header class='app-header'>
    <div class="container">
      <h1 class="logo">
        <RouterLink to="/">沐好物</RouterLink>
      </h1>
      <ul class="app-header-nav">
        <li class="home">
          <RouterLink to="/">首页</RouterLink>
        </li>
        <li class="home" v-for="item in categoryStore.categoryList" :key="item.id">
          <RouterLink active-class="active" :to="`/category/${item.id}`">{{ item.name }}</RouterLink>
        </li>
      </ul>
      <div class="search">
        <i class="iconfont icon-search"></i>
        <input type="text" placeholder="搜一搜">
      </div>
      <!-- 头部购物车 -->
      <handerCart />
    </div>
  </header>
</template>


<style scoped lang='scss'>
.app-header {
  position: relative;
  z-index: 1000;
  background: rgba(255, 255, 255, .94);
  backdrop-filter: blur(12px);
  box-shadow: 0 12px 36px rgba(15, 118, 110, .08);

  .container {
    position: relative;
    display: flex;
    align-items: center;
    height: 118px;
  }

  .logo {
    width: 200px;

    a {
      display: block;
      height: 132px;
      width: 100%;
      text-indent: -9999px;
      background: url('@/assets/images/logo.png') no-repeat center 18px / contain;
    }
  }

  .app-header-nav {
    width: 820px;
    display: flex;
    padding-left: 40px;
    position: relative;
    z-index: 998;
  
    li {
      margin-right: 40px;
      width: 38px;
      text-align: center;
  
      a {
        font-size: 16px;
        line-height: 36px;
        height: 36px;
        padding: 0 2px;
        display: inline-block;
        border-bottom: 2px solid transparent;
  
        &:hover {
          color: $xtxColor;
          border-bottom-color: $xtxColor;
        }
      }
  
      .active {
        color: $xtxColor;
        border-bottom-color: $xtxColor;
        font-weight: 700;
      }
    }
  }

  .search {
    width: 190px;
    height: 38px;
    position: relative;
    border: 1px solid #d8eee8;
    border-radius: 999px;
    background: #f7fffb;
    line-height: 38px;

    .icon-search {
      font-size: 18px;
      margin-left: 13px;
      color: $xtxColor;
    }

    input {
      width: 145px;
      padding-left: 5px;
      color: #666;
      background: transparent;
    }
  }

  .cart {
    width: 50px;

    .curr {
      height: 32px;
      line-height: 32px;
      text-align: center;
      position: relative;
      display: block;

      .icon-cart {
        font-size: 22px;
      }

      em {
        font-style: normal;
        position: absolute;
        right: 0;
        top: 0;
        padding: 1px 6px;
        line-height: 1;
        background: $helpColor;
        color: #fff;
        font-size: 12px;
        border-radius: 10px;
        font-family: Arial;
      }
    }
  }
}
</style>
