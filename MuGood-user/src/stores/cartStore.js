// 封装购物车模块
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { useUserStore } from './userStore'
import { insertCartAPI, findNewCartListAPI, delCartAPI } from '@/apis/cart'

export const useCartStore = defineStore(
  'cart',
  () => {
    const userStore = useUserStore()

    // 是否登录
    const isLogin = computed(() => userStore.userInfo.token)

    // 购物车列表
    const cartList = ref([])

    // 把 selected 统一转换成 true / false
    const formatCartItem = (item) => {
      return {
        ...item,
        selected: item.selected === true || item.selected === 1
      }
    }

    // 获取最新购物车列表
    const updateNewList = async () => {
      const res = await findNewCartListAPI()

      cartList.value = res.result.map(item => formatCartItem(item))
    }

    // 添加购物车
    const addCart = async (goods) => {
      const { skuId, count } = goods

      if (isLogin.value) {
        // 登录之后的加入购物车逻辑
        await insertCartAPI({ skuId, count })
        await updateNewList()
      } else {
        // 本地购物车逻辑
        const item = cartList.value.find((item) => goods.skuId === item.skuId)

        if (item) {
          item.count += count || 1
        } else {
          cartList.value.push({
            ...goods,
            selected: goods.selected === true || goods.selected === 1
          })
        }
      }

      console.log('cartList', cartList.value)
    }

    // 删除购物车
    const delCart = async (skuId) => {
      if (isLogin.value) {
        await delCartAPI([skuId])
        await updateNewList()
      } else {
        const index = cartList.value.findIndex((item) => skuId === item.skuId)

        if (index !== -1) {
          cartList.value.splice(index, 1)
        }
      }
    }

    // 清除购物车
    const clearCart = () => {
      cartList.value = []
    }

    // 单选功能
    const singleCheck = (skuId, selected) => {
      const item = cartList.value.find((item) => skuId === item.skuId)

      if (item) {
        item.selected = selected
      }
    }

    // 全选功能
    const allCheck = (selected) => {
      cartList.value.forEach((item) => {
        item.selected = selected
      })
    }

    // 总数量：所有商品 count 之和
    const allCount = computed(() => {
      return cartList.value.reduce((preValue, item) => {
        return preValue + item.count
      }, 0)
    })

    // 总价：所有商品 count * price 之和
    const allPrice = computed(() => {
      return cartList.value.reduce((preValue, item) => {
        return preValue + item.count * item.price
      }, 0)
    })

    // 已选择数量
    const selectedCount = computed(() => {
      return cartList.value
        .filter((item) => item.selected)
        .reduce((preValue, item) => {
          return preValue + item.count
        }, 0)
    })

    // 已选择商品价格合计
    const selectedPrice = computed(() => {
      return cartList.value
        .filter((item) => item.selected)
        .reduce((preValue, item) => {
          return preValue + item.count * item.price
        }, 0)
    })

    // 是否全选
    const isAll = computed(() => {
      return cartList.value.length > 0 && cartList.value.every((item) => item.selected)
    })

    return {
      cartList,
      allCount,
      allPrice,
      isAll,
      selectedCount,
      selectedPrice,
      clearCart,
      addCart,
      delCart,
      singleCheck,
      allCheck,
      updateNewList
    }
  },
  {
    persist: true
  }
)