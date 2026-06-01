import request from '@/utils/request'

// 创建订单
export const createOrder = (order, cartIds) => {
  return request({
    url: '/order/create',
    method: 'post',
    data: order,
    params: { cartIds }
  })
}

// 订单列表
export const getOrderList = (params) => {
  return request({
    url: '/order/list',
    method: 'get',
    params
  })
}

// 订单详情
export const getOrderDetail = (id) => {
  return request({
    url: `/order/${id}`,
    method: 'get'
  })
}

// 订单明细
export const getOrderItems = (orderId) => {
  return request({
    url: `/order/items/${orderId}`,
    method: 'get'
  })
}

// 支付订单
export const payOrder = (id) => {
  return request({
    url: `/order/pay/${id}`,
    method: 'post'
  })
}

// 取消订单
export const cancelOrder = (id) => {
  return request({
    url: `/order/cancel/${id}`,
    method: 'put'
  })
}

// 确认收货
export const receiveOrder = (id) => {
  return request({
    url: `/order/receive/${id}`,
    method: 'put'
  })
}

