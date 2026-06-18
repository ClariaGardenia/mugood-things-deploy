import httpInstance from '@/utils/http'
export const getOrderAPI = (id) => {
  return httpInstance({
    url: `/member/order/${id}`
  })
}

/**
 * 支付宝支付接口（生成支付跳转地址）
 * @param {Object} params
 * @param {Number|String} params.orderId
 * @param {String} params.redirect
 */
export const aliPayAPI = ({ orderId, redirect }) => {
  return httpInstance({
    url: '/pay/aliPay',
    method: 'GET',
    params: {
      orderId,
      redirect
    }
  })
}