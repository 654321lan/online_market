import request from '@/utils/request'

// 申请退换货
export const applyRefund = (refund) => {
  return request({
    url: '/refund/apply',
    method: 'post',
    data: refund
  })
}

// 我的退换货列表
export const getRefundList = (params) => {
  return request({
    url: '/refund/list',
    method: 'get',
    params
  })
}

// 退换货详情
export const getRefundDetail = (id) => {
  return request({
    url: `/refund/${id}`,
    method: 'get'
  })
}

// 商家退换货列表
export const getMerchantRefundList = (params) => {
  return request({
    url: '/merchant/refund/list',
    method: 'get',
    params
  })
}

// 商家同意退换货
export const approveRefund = (id, reply) => {
  return request({
    url: `/merchant/refund/approve/${id}`,
    method: 'put',
    params: { reply }
  })
}

// 商家拒绝退换货
export const rejectRefund = (id, reply) => {
  return request({
    url: `/merchant/refund/reject/${id}`,
    method: 'put',
    params: { reply }
  })
}

// 商家完成退换货
export const completeRefund = (id) => {
  return request({
    url: `/merchant/refund/complete/${id}`,
    method: 'put'
  })
}

// 获取有进行中退换货的订单ID列表
export const getActiveRefundOrderIds = (userId) => {
  return request({
    url: '/refund/active-orders',
    method: 'get',
    params: { userId }
  })
}
