<script setup>
  import { useUserStore } from '@/stores/userStore';
  import router from '@/router';
import { RouterLink } from 'vue-router';

  const userStore = useUserStore();

  const confirm = () => {
    // 清除用户信息 触发action
    userStore.clearUserInfo();
    // 跳转到登录页
    router.push('/login');
  }
</script>

<template>
  <nav class="app-topnav">
    <div class="container">
      <ul>
        <!-- 多模版渲染 区分登录状态和非登录状态 -->
        <template v-if="userStore.userInfo.token">
          <li><a href="javascript:;"><i class="iconfont icon-user"></i>{{userStore.userInfo.account}}</a></li>
          <li>
            <el-popconfirm @confirm="confirm" title="确认退出吗?" confirm-button-text="确认" cancel-button-text="取消">
              <template #reference>
                <a href="javascript:;">退出登录</a>
              </template>
            </el-popconfirm>
          </li>
          <li><RouterLink to="/member/order">我的订单</RouterLink></li>
          <li><RouterLink to="/member">会员中心</RouterLink></li>
        </template>
        <template v-else>
          <li><a href="javascript:;" @click="$router.push('/login')">请先登录</a></li>
          <li><a href="javascript:;">帮助中心</a></li>
          <li><a href="javascript:;">关于我们</a></li>
        </template>
      </ul>
    </div>
  </nav>
</template>


<style scoped lang="scss">
.app-topnav {
  background: linear-gradient(90deg, #12372f 0%, #102a27 54%, #243226 100%);
  box-shadow: 0 8px 26px rgba(15, 23, 42, .12);
  ul {
    display: flex;
    height: 53px;
    justify-content: flex-end;
    align-items: center;
    li {
      a {
        padding: 8px 15px;
        color: rgba(255, 255, 255, .78);
        line-height: 1;
        display: inline-block;
        border-radius: 999px;

        i {
          font-size: 14px;
          margin-right: 2px;
        }

        &:hover {
          color: #fff;
          background: rgba(255, 255, 255, .10);
        }
      }

      ~li {
        a {
          border-left: 1px solid rgba(255, 255, 255, .16);
          border-radius: 0;
        }
      }
    }
  }
}
</style>
