import request from '@/utils/request'

// 添加地址
export const addAddress = (address) => {
  return request({
    url: '/address/add',
    method: 'post',
    data: address
  })
}

// 地址列表
export const getAddressList = (userId) => {
  return request({
    url: '/address/list',
    method: 'get',
    params: { userId }
  })
}

// 更新地址
export const updateAddress = (address) => {
  return request({
    url: '/address/update',
    method: 'put',
    data: address
  })
}

// 删除地址
export const deleteAddress = (id) => {
  return request({
    url: `/address/delete/${id}`,
    method: 'delete'
  })
}

// 设置默认地址
export const setDefaultAddress = (id, userId) => {
  return request({
    url: `/address/default/${id}`,
    method: 'put',
    params: { userId }
  })
}

