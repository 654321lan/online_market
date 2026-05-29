import request from '@/utils/request'

// 商家注册
export const merchantRegister = (username, password, shopName) => {
  return request({
    url: '/merchant/register',
    method: 'post',
    params: { username, password, shopName }
  })
}

// 商家登录
export const merchantLogin = (username, password) => {
  return request({
    url: '/merchant/login',
    method: 'post',
    params: { username, password }
  })
}

// 商家信息
export const getMerchantInfo = (id) => {
  return request({
    url: `/merchant/info/${id}`,
    method: 'get'
  })
}

// 更新商家信息
export const updateMerchant = (merchant) => {
  return request({
    url: '/merchant/update',
    method: 'put',
    data: merchant
  })
}

// 发布商品
export const addProduct = (product) => {
  return request({
    url: '/merchant/product/add',
    method: 'post',
    data: product
  })
}

// 商家商品列表
export const getMerchantProducts = (params) => {
  return request({
    url: '/merchant/product/list',
    method: 'get',
    params
  })
}

// 商家商品详情
export const getMerchantProductDetail = (id) => {
  return request({
    url: `/merchant/product/${id}`,
    method: 'get'
  })
}

// 更新商品
export const updateProduct = (product) => {
  return request({
    url: '/merchant/product/update',
    method: 'put',
    data: product
  })
}

// 商品上下架
export const updateProductStatus = (id, status) => {
  return request({
    url: `/merchant/product/status/${id}`,
    method: 'put',
    params: { status }
  })
}

// 删除商品
export const deleteProduct = (id) => {
  return request({
    url: `/merchant/product/delete/${id}`,
    method: 'delete'
  })
}

// 商家订单列表
export const getMerchantOrders = (params) => {
  return request({
    url: '/merchant/order/list',
    method: 'get',
    params
  })
}

// 商家订单详情
export const getMerchantOrderDetail = (id) => {
  return request({
    url: `/merchant/order/${id}`,
    method: 'get'
  })
}

// 商家订单明细
export const getMerchantOrderItems = (orderId) => {
  return request({
    url: `/merchant/order/items/${orderId}`,
    method: 'get'
  })
}

// 订单发货
export const shipOrder = (id, trackingNo, expressCompany) => {
  return request({
    url: `/merchant/order/ship/${id}`,
    method: 'put',
    params: { trackingNo, expressCompany }
  })
}

// 销售统计
export const getSalesStatistics = (merchantId) => {
  return request({
    url: '/merchant/statistics/sales',
    method: 'get',
    params: { merchantId }
  })
}

// 商品统计
export const getProductStatistics = (merchantId) => {
  return request({
    url: '/merchant/statistics/product',
    method: 'get',
    params: { merchantId }
  })
}

