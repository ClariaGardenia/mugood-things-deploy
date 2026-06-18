// 封装倒计时函数
import { ref, computed, onUnmounted } from "vue"
import dayjs from "dayjs"

export const useCountDown = () => {
  // 1. 响应式数据
  let tiemr = null
  const time = ref(0)
  // 格式化时间
  const formatTime = computed(() => dayjs.unix(time.value).format("mm分ss秒" ))
  // 2. 开启倒计时函数
  const start = (currentTime) => {
    // 开始倒计时逻辑000)
    time.value = currentTime
    tiemr = setInterval(() => {
      time.value-- 
    }, 1000)
  }
  // 组件销毁是清除定时器
  onUnmounted(() => {
    tiemr&& clearInterval(tiemr)
  })

  return {
    formatTime,
    start
  }
}