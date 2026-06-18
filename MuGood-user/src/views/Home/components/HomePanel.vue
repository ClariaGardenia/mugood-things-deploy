<script setup>
  // 定义props
  defineProps({
    // 主标题
    title: {
      type: String,
    },
    // 副标题
    subTitle: {
      type: String,
    }
  })
</script>

<template>
  <div class="home-panel">
    <div class="container">
      <div class="head">
         <!-- 主标题和副标题 -->
        <h3>
          {{title}}<small>{{ subTitle }}</small>
        </h3>
      </div>
      <!-- 主体内容区域 -->
      <slot></slot>
    </div>
  </div>
</template>

<style scoped lang='scss'>
.home-panel {
  background: transparent;
  padding-top: 18px;

  .head {
    padding: 46px 0 24px;
    display: flex;
    align-items: flex-end;

    h3 {
      flex: 1;
      font-size: 32px;
      font-weight: 800;
      margin-left: 6px;
      height: 35px;
      line-height: 35px;
      color: #183b35;
      letter-spacing: .5px;

      small {
        font-size: 16px;
        color: $mutedColor;
        margin-left: 20px;
        font-weight: 400;
      }

      &::before {
        content: '';
        display: inline-block;
        width: 8px;
        height: 28px;
        margin-right: 12px;
        border-radius: 999px;
        vertical-align: -4px;
        background: linear-gradient(180deg, $xtxColor, #8ce99a);
      }
    }
  }
}
</style>
