import request from '@/utils/request'

// 添加购物车
export const addCart = (userId, productId, quantity) => {
  return request({
    url: '/cart/add',
    method: 'post',
    params: { userId, productId, quantity }
  })
}

// 购物车列表
export const getCartList = (userId) => {
  return request({
    url: '/cart/list',
    method: 'get',
    params: { userId }
  })
}

// 更新购物车
export const updateCart = (id, quantity) => {
  return request({
    url: '/cart/update',
    method: 'put',
    params: { id, quantity }
  })
}

// 删除购物车
export const deleteCart = (id) => {
  return request({
    url: `/cart/delete/${id}`,
    method: 'delete'
  })
}

