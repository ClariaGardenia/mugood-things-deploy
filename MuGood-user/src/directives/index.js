import { useIntersectionObserver } from '@vueuse/core'

// 定义懒加载插件
export const lazyPlugin = {
  install(app) {
    // 懒加载指令逻辑
    app.directive('img-lazy', {
      mounted(el,briding) { // el:指令绑定的元素 briding:指令对象 
        // console.log(el,briding.value); // briding.value:指令绑定的元素值 图片url
        useIntersectionObserver(
          el,
          ([{isIntersecting}]) => {
            // console.log(isIntersecting);
            if (isIntersecting) {
              // 进入视口区域
              el.src = briding.value
            }
          },
        )
      }
    })
  }
}